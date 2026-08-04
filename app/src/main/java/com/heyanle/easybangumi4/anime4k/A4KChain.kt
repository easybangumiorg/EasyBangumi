package com.heyanle.easybangumi4.anime4k

/**
 * Anime4K 链构建器。
 *
 * 链结构对照 Anime4K v4 官方推荐链：
 *  - 所有模式都以 Anime4K_Clamp_Highlights.glsl（去环）开头；
 *  - 7 个模式 = 不同 CNN 组合；质量 S/M/L 选择 Restore/Soft/Denoise/Upscale 的文件变体；
 *  - 链尾固定追加 Darken + Thin（两开关均固定开启）；
 *  - Deblur 文件名按 Anime4K v4 官方推荐链对应（Soft 系配 Deblur_Original，其余配 Deblur_DoG）；
 *  - Darken/Thin 变体映射：S→VeryFast，M→Fast，L→HQ。
 *
 * 调度顺序（mpv video.c 实际执行序）：MAIN 组 pass 先跑（文件加载序），
 * PREKERNEL 组后跑 —— gl_video_render_frame 中 MAIN 阶段（line ~3242）在
 * pass_scale_main 的 PREKERNEL 阶段（line ~2732）之前。Clamp 的 de-ring clamp pass
 * 因此实际运行在整条链的最后（与 mpv 行为一致）。
 */
object A4KChain {

    const val DEFAULT_MODE = 1
    const val DEFAULT_QUALITY = "M"
    const val QUALITY_S = "S"
    const val QUALITY_M = "M"
    const val QUALITY_L = "L"
    val QUALITIES = listOf(QUALITY_S, QUALITY_M, QUALITY_L)

    /** 模式显示名（设置页用） */
    val MODE_NAMES = listOf(
        "仅增强",
        "标准 4K",
        "柔和 4K",
        "去噪 4K",
        "精细 4K",
        "柔和精细 4K",
        "极致 4K",
    )

    fun build(
        mode: Int,
        quality: String,
        getFile: (String) -> List<A4KPass>?,
    ): List<A4KPass> {
        val q = if (quality in QUALITIES) quality else DEFAULT_QUALITY
        val out = ArrayList<A4KPass>()
        fun add(name: String) {
            val ps = getFile(name)
            if (ps != null) out.addAll(ps)
        }
        add("Anime4K_Clamp_Highlights.glsl")
        when (mode) {
            0 -> Unit
            1 -> {
                add("Anime4K_Restore_CNN_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
                add("Anime4K_Deblur_DoG.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
            }

            2 -> {
                add("Anime4K_Restore_CNN_Soft_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
                add("Anime4K_Deblur_Original.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
            }

            3 -> {
                add("Anime4K_Upscale_Denoise_CNN_x2_$q.glsl")
                add("Anime4K_Deblur_DoG.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
            }

            4 -> {
                add("Anime4K_Restore_CNN_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
                add("Anime4K_Deblur_DoG.glsl")
                add("Anime4K_Restore_CNN_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
            }

            5 -> {
                add("Anime4K_Restore_CNN_Soft_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
                add("Anime4K_Deblur_Original.glsl")
                add("Anime4K_Restore_CNN_Soft_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
            }

            6 -> {
                add("Anime4K_Upscale_Denoise_CNN_x2_$q.glsl")
                add("Anime4K_Deblur_DoG.glsl")
                add("Anime4K_Restore_CNN_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
            }

            else -> {
                // 非法模式退回标准链
                add("Anime4K_Restore_CNN_$q.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
                add("Anime4K_Deblur_DoG.glsl")
                add("Anime4K_Upscale_CNN_x2_$q.glsl")
            }
        }
        val post = when (q) {
            QUALITY_L -> "HQ"
            QUALITY_S -> "VeryFast"
            else -> "Fast"
        }
        add("Anime4K_Darken_$post.glsl")
        add("Anime4K_Thin_$post.glsl")
        return out
    }

    /**
     * 按 mpv 阶段调度排序：MAIN 组（文件序）→ PREKERNEL 组（文件序）。
     * Kotlin sortedBy 为稳定排序，同组内保持文件序。
     */
    fun schedule(passes: List<A4KPass>): List<A4KPass> {
        return passes.sortedBy { if (it.hookTarget == "PREKERNEL") 1 else 0 }
    }

    /**
     * 缩放策略（WHEN 的 OUTPUT 依据，等价于 mpv 的窗口尺寸）：
     * 输出目标 = 屏幕显示尺寸。scale = 使「视频宽 × scale ≥ 屏宽」的最小 2 的幂（1/2/4）。
     * 例：1080p 视频在 1080 宽的手机 → 1x（屏上本就显示不下原生分辨率，放大无意义且白耗算力）；
     *     480p 视频在 1080 宽的手机 → 2x（原生放不下，放大到 2x 后由呈现层缩放到屏宽）。
     * 链内各 x2 pass 的 //!WHEN 会据此自动决定是否运行。
     */
    fun scaleFor(inputWidth: Int, displayWidth: Int, manualScale: Int = 0): Int {
        // 手动倍率（1/2/4）优先；0 = 自动（按屏幕宽度决定）
        if (manualScale > 0) return manualScale
        if (displayWidth <= 0) return 1
        val ratio = displayWidth.toFloat() / inputWidth
        return when {
            ratio >= 2.4f -> 4
            ratio >= 1.2f -> 2
            else -> 1
        }
    }
}
