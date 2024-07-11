/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.heyanle.easy_transformer.utils;

import static java.lang.annotation.ElementType.TYPE_USE;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

/**
 * Generates a {@link SurfaceTexture} using EGL/GLES functions.
 */
@UnstableApi
public final class EasyEGLSurfaceTexture implements SurfaceTexture.OnFrameAvailableListener, Runnable {

    public interface TextureImageListener {
        boolean onFrameAvailable(SurfaceTexture surfaceTexture);
    }

    private static final int EGL_SURFACE_WIDTH = 1;
    private static final int EGL_SURFACE_HEIGHT = 1;

    private static final int[] EGL_CONFIG_ATTRIBUTES =
            new int[]{
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 0,
                    EGL14.EGL_CONFIG_CAVEAT, EGL14.EGL_NONE,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                    EGL14.EGL_NONE
            };

    private final Handler handler;
    private final int[] textureIdHolder;
    @Nullable
    private final TextureImageListener callback;

    @Nullable
    private EGLDisplay display;
    @Nullable
    private EGLContext context;
    @Nullable
    private EGLSurface surface;
    @Nullable
    private SurfaceTexture texture;

    public EasyEGLSurfaceTexture(Handler handler) {
        this(handler, /* callback= */ null);
    }

    public EasyEGLSurfaceTexture(Handler handler, @Nullable TextureImageListener callback) {
        this.handler = handler;
        this.callback = callback;
        textureIdHolder = new int[1];
    }

    public int getTextureId() {
        return textureIdHolder == null ? -1 : textureIdHolder[0];
    }

    public void init() throws GlUtil.GlException {
        display = getDefaultDisplay();
        EGLConfig config = chooseEGLConfig(display);
        context = createEGLContext(display, config);
        surface = createEGLSurface(display, config, context);
        generateTextureIds(textureIdHolder);
        texture = new SurfaceTexture(textureIdHolder[0]);
        texture.setOnFrameAvailableListener(this);
    }

    /**
     * Releases all allocated resources.
     */
    @SuppressWarnings("nullness:argument")
    public void release() {
        handler.removeCallbacks(this);
        try {
            if (texture != null) {
                texture.release();
                GLES20.glDeleteTextures(1, textureIdHolder, 0);
            }
        } finally {
            if (display != null && !display.equals(EGL14.EGL_NO_DISPLAY)) {
                EGL14.eglMakeCurrent(
                        display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            }
            if (surface != null && !surface.equals(EGL14.EGL_NO_SURFACE)) {
                EGL14.eglDestroySurface(display, surface);
            }
            if (context != null) {
                EGL14.eglDestroyContext(display, context);
            }
            EGL14.eglReleaseThread();
            if (display != null && !display.equals(EGL14.EGL_NO_DISPLAY)) {
                EGL14.eglTerminate(display);
            }
            display = null;
            context = null;
            surface = null;
            texture = null;
        }
    }


    public SurfaceTexture getSurfaceTexture() {
        return Assertions.checkNotNull(texture);
    }

    // SurfaceTexture.OnFrameAvailableListener

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        handler.post(this);
    }

    // Runnable

    @Override
    public void run() {
        // Run on the provided handler thread when a new image frame is available.
        if (texture != null) {
            try {
                if (! dispatchOnFrameAvailable(texture)) {
                    texture.updateTexImage();
                }
            } catch (RuntimeException e) {
                // Ignore
            }
        }
    }

    private boolean dispatchOnFrameAvailable(SurfaceTexture texture) {
        if (callback != null) {
            return callback.onFrameAvailable(texture);
        }
        return false;
    }

    private static EGLDisplay getDefaultDisplay() throws GlUtil.GlException {
        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        GlUtil.checkGlException(display != null, "eglGetDisplay failed");

        int[] version = new int[2];
        boolean eglInitialized =
                EGL14.eglInitialize(display, version, /* majorOffset= */ 0, version, /* minorOffset= */ 1);
        GlUtil.checkGlException(eglInitialized, "eglInitialize failed");
        return display;
    }

    private static EGLConfig chooseEGLConfig(EGLDisplay display) throws GlUtil.GlException {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        boolean success =
                EGL14.eglChooseConfig(
                        display,
                        EGL_CONFIG_ATTRIBUTES,
                        /* attrib_listOffset= */ 0,
                        configs,
                        /* configsOffset= */ 0,
                        /* config_size= */ 1,
                        numConfigs,
                        /* num_configOffset= */ 0);
        GlUtil.checkGlException(
                success && numConfigs[0] > 0 && configs[0] != null,
                Util.formatInvariant(
                        /* format= */ "eglChooseConfig failed: success=%b, numConfigs[0]=%d, configs[0]=%s",
                        success, numConfigs[0], configs[0]));

        return configs[0];
    }

    private static EGLContext createEGLContext(
            EGLDisplay display, EGLConfig config) throws GlUtil.GlException {
        int[] glAttributes;
        glAttributes = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        EGLContext context =
                EGL14.eglCreateContext(
                        display, config, android.opengl.EGL14.EGL_NO_CONTEXT, glAttributes, 0);
        GlUtil.checkGlException(context != null, "eglCreateContext failed");
        return context;
    }

    private static EGLSurface createEGLSurface(
            EGLDisplay display, EGLConfig config, EGLContext context)
            throws GlUtil.GlException {
        EGLSurface surface;
        int[] pbufferAttributes =
                new int[]{
                        EGL14.EGL_WIDTH,
                        EGL_SURFACE_WIDTH,
                        EGL14.EGL_HEIGHT,
                        EGL_SURFACE_HEIGHT,
                        EGL14.EGL_NONE
                };
        surface = EGL14.eglCreatePbufferSurface(display, config, pbufferAttributes, /* offset= */ 0);
        GlUtil.checkGlException(surface != null, "eglCreatePbufferSurface failed");

        boolean eglMadeCurrent =
                EGL14.eglMakeCurrent(display, /* draw= */ surface, /* read= */ surface, context);
        GlUtil.checkGlException(eglMadeCurrent, "eglMakeCurrent failed");
        return surface;
    }

    private static void generateTextureIds(int[] textureIdHolder) throws GlUtil.GlException {
        GLES20.glGenTextures(/* n= */ 1, textureIdHolder, /* offset= */ 0);
        GlUtil.checkGlError();
    }
}
