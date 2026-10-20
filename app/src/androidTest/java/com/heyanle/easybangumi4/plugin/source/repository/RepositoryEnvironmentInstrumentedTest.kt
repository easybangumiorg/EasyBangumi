package com.heyanle.easybangumi4.plugin.source.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.getEnum
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryEnvironmentInstrumentedTest {
    @Test
    fun internalEnvironmentDoesNotChangeUserRepositories() = runBlocking {
        val preferences: RepositoryPreferences by Inject.injectLazy()
        preferences.repositories.initJob.join()
        val originalRepositories = preferences.repositories.getOrDef().toList()
        // Legacy/internal environment selection must not activate a repository in the app.
        val environmentPreference = AndroidPreferenceStore(
            InstrumentationRegistry.getInstrumentation().targetContext,
        ).getEnum("official_repository_environment", OfficialRepositoryEnvironment.RELEASE)
        val originalEnvironment = environmentPreference.get()
        val wasSet = environmentPreference.isSet()
        try {
            OfficialRepositoryEnvironment.entries.forEach { environment ->
                environmentPreference.set(environment)
                val effective = withTimeout(10_000) {
                    preferences.activeRepositories.first { it == activeRepositories(originalRepositories) }
                }
                assertEquals(activeRepositories(originalRepositories), effective)
                assertEquals(effective, preferences.getActiveRepositories())
                val freshPreference = AndroidPreferenceStore(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                ).getEnum("official_repository_environment", OfficialRepositoryEnvironment.RELEASE)
                assertEquals(environment, freshPreference.get())
                assertEquals(originalRepositories, preferences.repositories.getOrDef())
            }
        } finally {
            if (wasSet) environmentPreference.set(originalEnvironment)
            else environmentPreference.delete()
            withTimeout(10_000) {
                preferences.activeRepositories.first { it == activeRepositories(originalRepositories) }
            }
        }
    }
}
