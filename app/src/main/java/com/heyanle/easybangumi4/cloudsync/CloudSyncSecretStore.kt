package com.heyanle.easybangumi4.cloudsync

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.UUID

/** Stores WebDAV credentials encrypted by a non-exportable Android Keystore key. */
class CloudSyncSecretStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun setWebDavPassword(password: String) {
        if (password.isEmpty()) {
            preferences.edit().remove(KEY_WEBDAV_PASSWORD).apply()
            return
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encrypted
        preferences.edit().putString(
            KEY_WEBDAV_PASSWORD,
            Base64.encodeToString(payload, Base64.NO_WRAP),
        ).apply()
    }

    fun webDavPassword(): String = runCatching {
        val encoded = preferences.getString(KEY_WEBDAV_PASSWORD, null) ?: return ""
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        require(payload.size > IV_SIZE_BYTES)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            encryptionKey(),
            GCMParameterSpec(GCM_TAG_BITS, payload.copyOfRange(0, IV_SIZE_BYTES)),
        )
        cipher.doFinal(payload.copyOfRange(IV_SIZE_BYTES, payload.size)).toString(Charsets.UTF_8)
    }.getOrDefault("")

    fun clearWebDavPassword() {
        preferences.edit().remove(KEY_WEBDAV_PASSWORD).apply()
    }

    /** Kept outside normal preferences so Android backup cannot clone one device identity. */
    fun deviceId(): String {
        preferences.getString(KEY_DEVICE_ID, null)?.takeIf(String::isNotBlank)?.let { return it }
        return UUID.randomUUID().toString().also {
            preferences.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    private fun encryptionKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCE_NAME = "cloud_sync_secrets"
        const val KEY_WEBDAV_PASSWORD = "webdav_password"
        const val KEY_DEVICE_ID = "device_id"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "easybangumi_cloud_sync_credentials_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE_BYTES = 12
        const val GCM_TAG_BITS = 128
    }
}
