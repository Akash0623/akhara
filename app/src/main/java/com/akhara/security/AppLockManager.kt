package com.akhara.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AppLockManager(private val context: Context) {

    enum class AuthCapability {
        BIOMETRIC_AVAILABLE,
        CREDENTIAL_AVAILABLE,
        NONE
    }

    fun getAuthCapability(): AuthCapability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> AuthCapability.BIOMETRIC_AVAILABLE
            else -> {
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> AuthCapability.CREDENTIAL_AVAILABLE
                    else -> AuthCapability.NONE
                }
            }
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    onFailure("Authentication cancelled")
                } else {
                    onFailure(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailure("Authentication failed. Try again.")
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val allowedAuthenticators = when (getAuthCapability()) {
            AuthCapability.BIOMETRIC_AVAILABLE ->
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            AuthCapability.CREDENTIAL_AVAILABLE ->
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            AuthCapability.NONE -> return onFailure("No authentication method available")
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Akhara")
            .setSubtitle("Verify your identity to access your workout data")
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun isLockEnabled(): Boolean = SecurePreferences.isAppLockEnabled(context)

    fun setLockEnabled(enabled: Boolean) {
        SecurePreferences.setAppLockEnabled(context, enabled)
    }
}
