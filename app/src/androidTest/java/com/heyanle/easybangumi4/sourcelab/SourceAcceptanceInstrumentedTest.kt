package com.heyanle.easybangumi4.sourcelab

import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Build
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.plugin.source.ISourceController
import com.heyanle.easybangumi4.plugin.source.SourceConfig
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.plugin.source.SourcePreferences
import com.heyanle.easybangumi4.v2.MainV2Activity
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest

/** Reports source failures as evidence; assertion failures are reserved for a broken test harness. */
@RunWith(AndroidJUnit4::class)
class SourceAcceptanceInstrumentedTest {
    @Test
    fun acceptSelectedSources() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext
        val arguments = InstrumentationRegistry.getArguments()
        val reportName = arguments.getString("reportName") ?: "source-acceptance"
        require(reportName.matches(Regex("[a-zA-Z0-9_-]+")))
        val folder = File(app.cacheDir, "source-acceptance/$reportName").apply { mkdirs() }
        val externalInput = arguments.getString("sourceDir").orEmpty()
        if (externalInput.isNotBlank()) {
            val input = File(externalInput).canonicalFile
            require(input.path.startsWith(app.cacheDir.canonicalPath + "/") ||
                input.path.startsWith(requireNotNull(app.getExternalFilesDir(null)).canonicalPath + "/"))
            input.listFiles().orEmpty().filter { it.isFile && it.extension == "js" }
                .forEach { it.copyTo(File(folder, it.name), overwrite = false) }
        } else {
            app.assets.list("").orEmpty().filter { it.endsWith(".js") && !it.startsWith("block-") }
                .forEach { name -> app.assets.open(name).use { source ->
                    File(folder, name).outputStream().use { output -> source.copyTo(output) }
                } }
        }
        val byKey = folder.listFiles().orEmpty().associateBy { file ->
            Regex("(?m)^\\s*//\\s*@key\\s+(\\S+)").find(file.readText())?.groupValues?.get(1)
                ?: error("No source key: ${file.name}")
        }
        assertTrue("Input must contain JS sources", byKey.isNotEmpty())
        val selected = arguments.getString("sourceKeys").orEmpty().split(',')
            .map(String::trim).filter(String::isNotEmpty).toSet().ifEmpty { byKey.keys }
        assertTrue("Missing exact input artifacts: ${selected - byKey.keys}", byKey.keys.containsAll(selected))
        val manualTimeoutMs = arguments.getString("manualTimeoutMs")?.toLongOrNull() ?: 600_000L
        require(manualTimeoutMs > 0)
        val config = LabConfig(selected = selected, manualTimeoutMs = manualTimeoutMs)
        val serial = arguments.getString("deviceSerial").orEmpty()
        val evidenceContext = LabEvidenceContext(
            device = LabDeviceEvidence(Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT, serial,
                serial.isNotBlank() && !serial.startsWith("emulator-") &&
                    !Build.FINGERPRINT.contains("generic") && !Build.MODEL.contains("Emulator")),
            app = LabAppEvidence(app.packageName, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
                BuildConfig.BUILD_TYPE, arguments.getString("gitCommit").orEmpty()),
            repositoryEnvironment = arguments.getString("repositoryEnvironment") ?: "dev",
            network = arguments.getString("network") ?: "device-network",
        )
        val reportFolder = File(app.getExternalFilesDir(null), "source_acceptance").apply { mkdirs() }
        val report = File(reportFolder, "$reportName.json")
        val markdown = File(reportFolder, "$reportName.md")
        val progress = File(reportFolder, "$reportName-progress.json")
        fun writeProgress(phase: String, request: LabVerificationRequest? = null) {
            val value = linkedMapOf<String, Any?>("phase" to phase, "updatedAt" to System.currentTimeMillis(),
                "sourceKey" to request?.sourceKey, "step" to request?.step?.name,
                "attempt" to request?.attempt, "manualTimeoutMs" to manualTimeoutMs)
            val temporary = File(progress.parentFile, progress.name + ".tmp")
            temporary.writeText(Gson().toJson(value))
            check(temporary.renameTo(progress)) { "Cannot write acceptance progress" }
        }
        val results = mutableListOf<LabSourceResult>()
        fun save() {
            val run = LabRun(config = config, items = results.toList(), total = selected.size,
                finishedAt = System.currentTimeMillis(), evidenceContext = evidenceContext)
            report.writeText(run.toEvidenceJson())
            markdown.writeText(run.toMarkdown())
        }
        save()
        val preferences = Inject.get<SourcePreferences>()
        val previous = preferences.configs.getOrDef()
        foregroundSourceLabForDeviceTests()
        val scenario = ActivityScenario.launch(MainV2Activity::class.java)
        var activity: android.app.Activity? = null
        scenario.onActivity { activity = it }
        val verificationHost = SourceLabVerificationHost({ activity }, config.manualTimeoutMs)
        val manualEnabled = arguments.getString("manualVerification") != "false"
        val waitingObserver = launch(Dispatchers.Default) {
            verificationHost.pending.collect { request ->
                writeProgress(if (request == null) "RUNNING" else "WAITING_USER", request)
                if (request != null) println("SOURCE_ACCEPTANCE_WAITING_USER ${request.sourceKey} ${request.step} ${request.attempt}")
            }
        }
        val probe = SourceLabPlayProbe(Inject.get<CartoonMediaSourceFactory>())
        val surfaceReady = CompletableDeferred<Unit>()
        var ownedSurface: Surface? = null
        lateinit var status: TextView
        try {
            scenario.onActivity { activity ->
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val root = FrameLayout(activity).apply { setBackgroundColor(Color.BLACK) }
                val texture = TextureView(activity).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            ownedSurface = Surface(surface)
                            probe.attachSurface(ownedSurface)
                            surfaceReady.complete(Unit)
                        }
                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            probe.attachSurface(null)
                            ownedSurface?.release()
                            ownedSurface = null
                            return true
                        }
                    }
                }
                root.addView(texture, FrameLayout.LayoutParams(960, 540).apply { topMargin = 250 })
                status = TextView(activity).apply { setTextColor(Color.WHITE); textSize = 18f; text = "源验收：等待真实视频画面" }
                root.addView(status, FrameLayout.LayoutParams(-1, 200).apply { topMargin = 50; leftMargin = 24 })
                activity.setContentView(root)
            }
            withTimeout(15_000L) { surfaceReady.await() }
            preferences.configs.set(previous.toMutableMap().apply {
                selected.forEach { key -> this[key] = this[key]?.copy(enable = true) ?: SourceConfig(key, Int.MAX_VALUE, true) }
            })
            // This controller has only the exact artifacts under test: installed higher versions cannot shadow them.
            val controller = SourceController(folder, preferences, innerSourceFileProvider = null)
            withTimeout(60_000L) {
                while (controller.sourceInfo.value !is ISourceController.SourceInfoState.Info ||
                    controller.sourceBundle.value == null) delay(100)
            }
            val bundle = requireNotNull(controller.sourceBundle.value)
            val runner = SourceLabRunner(bundle, probe, evidenceContext,
                verificationHandler = if (manualEnabled) verificationHost else null)
            for (key in selected.sorted()) {
                println("SOURCE_ACCEPTANCE_START $key")
                scenario.onActivity { status.text = "源验收 ${results.size + 1}/${selected.size}\n$key" }
                var result = try {
                    withContext(Dispatchers.IO) { runner.runSource(key, config) }
                } catch (cancelled: LabVerificationCancelledException) {
                    results.add(cancelled.partialResult)
                    save()
                    writeProgress("CANCELLED")
                    throw cancelled
                }
                if (result.sourceSha256.isBlank()) {
                    // Unsupported API/load failures still identify the exact input artifact.
                    // This cannot turn a failed runtime outcome into an approved source.
                    val file = requireNotNull(byKey[key])
                    val tags = Regex("(?m)^\\s*//\\s*@([\\w]+)\\s+([^\\r\\n]+)")
                        .findAll(file.readText()).associate { it.groupValues[1] to it.groupValues[2] }
                    result = result.copy(label = tags["label"] ?: key,
                        versionName = tags["versionName"].orEmpty(),
                        versionCode = tags["versionCode"]?.toIntOrNull() ?: 0,
                        sourceSha256 = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
                            .joinToString("") { "%02x".format(it) })
                }
                results.add(result)
                save()
                println("SOURCE_ACCEPTANCE_RESULT $key ${result.acceptance} ${result.elapsedMs}ms")
                scenario.onActivity { status.text = "源验收 ${results.size}/${selected.size}\n$key ${result.acceptance}" }
            }
            assertTrue("Every selected artifact must have a recorded outcome", results.map { it.key }.toSet() == selected)
            println("SOURCE_ACCEPTANCE_REPORT ${report.absolutePath}")
            writeProgress("FINISHED")
        } finally {
            waitingObserver.cancel()
            withContext(NonCancellable + Dispatchers.Main) {
                verificationHost.close()
                probe.release()
            }
            scenario.close()
            activity = null
            ownedSurface?.release()
            preferences.configs.set(previous)
        }
    }
}
