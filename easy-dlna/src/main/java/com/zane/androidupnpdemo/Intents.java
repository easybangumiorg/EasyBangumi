/*
 * Copyright (C) 2014 Kevin Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zane.androidupnpdemo;

import android.content.Intent;

import java.io.Serializable;

public class Intents {
    /**
     * Prefix for all intents created
     */
    public static final String INTENT_PREFIX = "com.zane.androidupnpdemo.";

    /**
     * Prefix for all extra data added to intents
     */
    public static final String INTENT_EXTRA_PREFIX = INTENT_PREFIX + "extra.";

    /**
     * Prefix for all action in intents
     */
    public static final String INTENT_ACTION_PREFIX = INTENT_PREFIX + "action.";

    /**
     * Playing action for MediaPlayer
     */
    public static final String ACTION_PLAYING = INTENT_ACTION_PREFIX + "playing";

    /**
     * Paused playback action for MediaPlayer
     */
    public static final String ACTION_PAUSED_PLAYBACK = INTENT_ACTION_PREFIX + "paused_playback";

    /**
     * Stopped action for MediaPlayer
     */
    public static final String ACTION_STOPPED = INTENT_ACTION_PREFIX + "stopped";

    /**
     * transitioning action for MediaPlayer
     */
    public static final String ACTION_TRANSITIONING = INTENT_ACTION_PREFIX + "transitioning";

    /**
     * Change device action for MediaPlayer
     */
    public static final String ACTION_CHANGE_DEVICE = INTENT_ACTION_PREFIX + "change_device";

    /**
     * Set volume action for MediaPlayer
     */
    public static final String ACTION_SET_VOLUME = INTENT_ACTION_PREFIX + "set_volume";

    /**
     * 主动获取播放进度
     */
    public static final String ACTION_GET_POSITION = INTENT_ACTION_PREFIX + "get_position";
    /**
     * 远程设备回传播放进度
     */
    public static final String ACTION_POSITION_CALLBACK = INTENT_ACTION_PREFIX + "position_callback";
    /**
     * 音量回传
     */
    public static final String ACTION_VOLUME_CALLBACK = INTENT_ACTION_PREFIX + "volume_callback";
    /**
     * 播放进度回传值
     */
    public static final String EXTRA_POSITION = INTENT_ACTION_PREFIX + "extra_position";
    /**
     * 音量回传值
     */
    public static final String EXTRA_VOLUME = INTENT_ACTION_PREFIX + "extra_volume";
    /**
     * 投屏端播放完成
     */
    public static final String ACTION_PLAY_COMPLETE = INTENT_ACTION_PREFIX + "play_complete";

    /**
     * Update the lastChange value action for MediaPlayer
     */
    public static final String ACTION_UPDATE_LAST_CHANGE = INTENT_ACTION_PREFIX + "update_last_change";

    /**
     * Builder for generating an intent configured with extra data.
     */
    public static class Builder {

        private final Intent intent;

        /**
         * Create builder with suffix
         *
         * @param actionSuffix
         */
        public Builder(String actionSuffix) {
            intent = new Intent(INTENT_PREFIX + actionSuffix);
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, String value) {
            intent.putExtra(fieldName, value);
            return this;
        }

        /**
         * Add extra field data values to intent being built up
         *
         * @param fieldName
         * @param values
         * @return this builder
         */
        public Builder add(String fieldName, CharSequence[] values) {
            intent.putExtra(fieldName, values);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, int value) {
            intent.putExtra(fieldName, value);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param values
         * @return this builder
         */
        public Builder add(String fieldName, int[] values) {
            intent.putExtra(fieldName, values);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param values
         * @return this builder
         */
        public Builder add(String fieldName, boolean[] values) {
            intent.putExtra(fieldName, values);
            return this;
        }

        /**
         * Add extra field data value to intent being built up
         *
         * @param fieldName
         * @param value
         * @return this builder
         */
        public Builder add(String fieldName, Serializable value) {
            intent.putExtra(fieldName, value);
            return this;
        }

        /**
         * Get built intent
         *
         * @return intent
         */
        public Intent toIntent() {
            return intent;
        }
    }
}
