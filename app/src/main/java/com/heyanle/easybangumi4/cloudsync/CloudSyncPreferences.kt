package com.heyanle.easybangumi4.cloudsync

import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.getEnum
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncConflictStrategy
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncProvider

class CloudSyncPreferences(
    preferenceStore: PreferenceStore,
) {
    val provider = preferenceStore.getEnum("cloud_sync_provider", CloudSyncProvider.NONE)
    val conflictStrategy = preferenceStore.getEnum(
        "cloud_sync_conflict_strategy",
        CloudSyncConflictStrategy.SMART_MERGE,
    )
    val webDavUrl = preferenceStore.getString("cloud_sync_webdav_url", "")
    val webDavUsername = preferenceStore.getString("cloud_sync_webdav_username", "")
    val oneDriveAccountName = preferenceStore.getString("cloud_sync_onedrive_account", "")
    val lastSuccessTime = preferenceStore.getLong("cloud_sync_last_success_time", 0L)
    val lastRemoteVersion = preferenceStore.getString("cloud_sync_last_remote_version", "")
}
