package com.akhara.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecurePreferences {

    private const val FILE_NAME = "akhara_secure_prefs"
    private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"

    @Volatile
    private var instance: SharedPreferences? = null

    fun get(context: Context): SharedPreferences {
        return instance ?: synchronized(this) {
            instance ?: createEncryptedPrefs(context).also { instance = it }
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isAppLockEnabled(context: Context): Boolean =
        get(context).getBoolean(KEY_APP_LOCK_ENABLED, false)

    fun setAppLockEnabled(context: Context, enabled: Boolean) {
        get(context).edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun getOrCreateDbPassphrase(context: Context): ByteArray {
        val prefs = get(context)
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) {
            return existing.toByteArray(Charsets.UTF_8)
        }
        val passphrase = generateSecurePassphrase()
        prefs.edit().putString(KEY_DB_PASSPHRASE, passphrase).apply()
        return passphrase.toByteArray(Charsets.UTF_8)
    }

    private fun generateSecurePassphrase(): String {
        val bytes = ByteArray(32)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
