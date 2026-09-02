package com.example.ehtracker.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.ehtracker.lock.AppLockManager
import kotlinx.coroutines.delay

private const val MAX_PIN_ATTEMPTS = 5

@Composable
fun LockScreen(
    lockManager: AppLockManager,
    appName: String,
    onUnlocked: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var retryKey by rememberSaveable { mutableIntStateOf(0) }
    var usePinKey by rememberSaveable { mutableIntStateOf(0) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var showPinPad by rememberSaveable { mutableStateOf(!lockManager.biometricAvailable()) }
    var pin by rememberSaveable { mutableStateOf("") }
    var pinAttempts by rememberSaveable { mutableIntStateOf(0) }
    var lockedUntil by rememberSaveable { mutableStateOf(0L) }

    val useBiometric = activity != null && lockManager.biometricAvailable()

    fun submitPin() {
        if (pin.length < 4) return
        if (lockManager.verifyPin(pin)) {
            message = null
            onUnlocked()
        } else {
            pinAttempts++
            if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                lockedUntil = System.currentTimeMillis() + 15_000
                pinAttempts = 0
                message = "Too many attempts. Wait 15 seconds."
            } else {
                message = "Incorrect PIN. ${MAX_PIN_ATTEMPTS - pinAttempts} attempts left."
            }
            pin = ""
        }
    }

    fun tryBiometric() {
        if (!useBiometric) {
            showPinPad = true
            return
        }
        lockManager.authenticate(
            host = activity!!,
            title = "Unlock $appName",
            onSucceeded = {
                message = null
                onUnlocked()
            },
            onError = { err ->
                message = err
            }
        )
    }

    val processOwner = ProcessLifecycleOwner.get()
    val processState by processOwner.lifecycle.currentStateFlow
        .collectAsState(initial = Lifecycle.State.RESUMED)

    LaunchedEffect(retryKey, usePinKey, processState) {
        if (processState == Lifecycle.State.RESUMED && useBiometric && !showPinPad) {
            delay(250)
            tryBiometric()
        }
    }

    LaunchedEffect(lockedUntil) {
        if (lockedUntil > 0L && lockedUntil > System.currentTimeMillis()) {
            delay(lockedUntil - System.currentTimeMillis())
            lockedUntil = 0L
            message = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "App locked",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message ?: "Authenticate to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = if (message != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (showPinPad) {
                PinPad(
                    pin = pin,
                    interactive = System.currentTimeMillis() >= lockedUntil,
                    onDigit = { d ->
                        if (pin.length < 4) {
                            pin += d
                            if (pin.length == 4) submitPin()
                        }
                    },
                    onBackspace = {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    }
                )
            } else {
                Button(onClick = { message = null; retryKey++ }) {
                    Text("Unlock")
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { showPinPad = true; usePinKey++ }) {
                    Text("Use PIN")
                }
            }
        }
    }
}

@Composable
private fun PinPad(
    pin: String,
    interactive: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            color = if (index < pin.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                )
            }
        }
        val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "back")
        digits.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { label ->
                    if (label.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = interactive) {
                                    if (label == "back") onBackspace() else onDigit(label)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (label == "back") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "4-digit PIN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}