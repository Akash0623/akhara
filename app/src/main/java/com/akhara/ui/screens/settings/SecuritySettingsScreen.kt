package com.akhara.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.akhara.security.AppLockManager
import com.akhara.security.SecurityUtils
import com.akhara.ui.components.GlassCard
import com.akhara.ui.theme.BackgroundDark
import com.akhara.ui.theme.Destructive
import com.akhara.ui.theme.PrimaryTeal
import com.akhara.ui.theme.SuccessGreen
import com.akhara.ui.theme.TextSecondary
import com.akhara.ui.theme.WarningAmber

@Composable
fun SecuritySettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lockManager = remember { AppLockManager(context) }
    var isLockEnabled by remember { mutableStateOf(lockManager.isLockEnabled()) }
    val authCapability = remember { lockManager.getAuthCapability() }
    val isRooted = remember { SecurityUtils.isDeviceRooted() }
    val isDebuggable = remember { SecurityUtils.isDebuggable(context) }
    val isEmulator = remember { SecurityUtils.isRunningOnEmulator() }
    val signatureValid = remember { SecurityUtils.verifyAppSignature(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryTeal
                )
            }
            Icon(
                Icons.Rounded.Shield,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Security",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Rounded.Fingerprint,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "App Lock",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = when (authCapability) {
                                AppLockManager.AuthCapability.BIOMETRIC_AVAILABLE -> "Biometric + Device Credential"
                                AppLockManager.AuthCapability.CREDENTIAL_AVAILABLE -> "PIN / Password / Pattern"
                                AppLockManager.AuthCapability.NONE -> "No lock method available on device"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = isLockEnabled,
                    onCheckedChange = { enabled ->
                        if (authCapability == AppLockManager.AuthCapability.NONE) {
                            Toast.makeText(context, "Set up device lock screen first", Toast.LENGTH_SHORT).show()
                            return@Switch
                        }
                        if (enabled) {
                            lockManager.authenticate(
                                activity = context as FragmentActivity,
                                onSuccess = {
                                    lockManager.setLockEnabled(true)
                                    isLockEnabled = true
                                },
                                onFailure = {
                                    Toast.makeText(context, "Verify identity first", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            lockManager.authenticate(
                                activity = context as FragmentActivity,
                                onSuccess = {
                                    lockManager.setLockEnabled(false)
                                    isLockEnabled = false
                                },
                                onFailure = {
                                    Toast.makeText(context, "Verify identity to disable", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    enabled = authCapability != AppLockManager.AuthCapability.NONE,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDark,
                        checkedTrackColor = PrimaryTeal,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = BackgroundDark
                    )
                )
            }
        }

        Text(
            text = "DEVICE SECURITY STATUS",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SecurityStatusRow(
                    label = "Database Encryption",
                    status = "AES-256 (SQLCipher)",
                    isSecure = true
                )
                SecurityStatusRow(
                    label = "Key Storage",
                    status = "Android Keystore",
                    isSecure = true
                )
                SecurityStatusRow(
                    label = "Preferences Encryption",
                    status = "EncryptedSharedPreferences",
                    isSecure = true
                )
                SecurityStatusRow(
                    label = "App Signature",
                    status = if (signatureValid) "Valid" else "Tampered",
                    isSecure = signatureValid
                )
                SecurityStatusRow(
                    label = "Root Detection",
                    status = if (isRooted) "ROOTED DEVICE" else "Not Rooted",
                    isSecure = !isRooted
                )
                SecurityStatusRow(
                    label = "Debug Mode",
                    status = if (isDebuggable) "DEBUGGABLE" else "Release",
                    isSecure = !isDebuggable
                )
                SecurityStatusRow(
                    label = "Environment",
                    status = if (isEmulator) "EMULATOR" else "Physical Device",
                    isSecure = !isEmulator
                )
            }
        }

        if (isRooted) {
            GlassCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = Destructive,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Security Warning",
                            fontWeight = FontWeight.Bold,
                            color = Destructive
                        )
                        Text(
                            text = "This device appears to be rooted. Your workout data may be at risk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SecurityStatusRow(label: String, status: String, isSecure: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Rounded.Security,
                contentDescription = null,
                tint = if (isSecure) SuccessGreen else WarningAmber,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isSecure) SuccessGreen else Destructive
        )
    }
}
