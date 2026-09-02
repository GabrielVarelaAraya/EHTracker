package com.example.ehtracker.lock

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators as Auth
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.security.MessageDigest

private fun String.sha256(): String = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray())
    .joinToString("") { "%02x".format(it) }

/**
 * Owns the persisted lock state: whether the app lock is enabled (Room-backed,
 * via [TrackerRepository.biometricEnabled]) and the 4-digit fallback PIN hash
 * (SharedPreferences). Does not hold Compose UI state.
 */
class AppLockManager(
    private val context: Context,
    private val repository: TrackerRepository,
) {
    companion object {
        private const val PREFS = "ehtracker_security"
        private const val KEY_PIN_HASH = "lock_pin_hash"
        private const val TAG = "AppLock"
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    val lockEnabled: Flow<Boolean> = repository.biometricEnabled()

    fun setLockEnabled(enabled: Boolean) {
        ioScope.launch { repository.setBiometricEnabled(enabled) }
    }

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, pin.sha256()).commit()
        Log.d(TAG, "PIN saved")
    }

    fun hasPin(): Boolean = prefs.contains(KEY_PIN_HASH)

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val ok = stored == pin.sha256()
        if (!ok) Log.d(TAG, "PIN mismatch")
        return ok
    }

    fun biometricAvailable(): Boolean {
        val manager = BiometricManager.from(context)
        val strong = manager.canAuthenticate(Auth.BIOMETRIC_STRONG)
        val weak = manager.canAuthenticate(Auth.BIOMETRIC_WEAK)
        Log.d(TAG, "canAuthenticate strong=$strong weak=$weak")
        return strong == BiometricManager.BIOMETRIC_SUCCESS ||
            weak == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Builds and starts a [BiometricPrompt] against [host]. [onSucceeded] is
     * invoked on success; [onError] with a user-facing message otherwise.
     */
    fun authenticate(
        host: androidx.fragment.app.FragmentActivity,
        title: String,
        onSucceeded: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d(TAG, "biometric success")
                onSucceeded()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.d(TAG, "biometric error $errorCode: $errString")
                onError(when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> "Authentication cancelled"
                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "No PIN or fingerprint is set up on this device"
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> "Biometric hardware is currently unavailable"
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> "No fingerprint enrolled on this device"
                    BiometricPrompt.ERROR_LOCKOUT -> "Too many attempts. Try again later"
                    else -> errString.toString()
                })
            }

            override fun onAuthenticationFailed() {
                Log.d(TAG, "biometric failed")
                onError("Fingerprint not recognized. Try again or use PIN.")
            }
        }
        val prompt = BiometricPrompt(host, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle("Authenticate to continue")
            .setNegativeButtonText("Use PIN")
            .build()
        try {
            prompt.authenticate(info)
        } catch (t: Throwable) {
            Log.d(TAG, "authenticate threw", t)
            onError(t.localizedMessage ?: "Biometric authentication unavailable")
        }
    }
}