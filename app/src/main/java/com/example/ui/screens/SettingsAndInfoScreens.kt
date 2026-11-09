package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppStrings
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberDialog
import com.example.ui.theme.CairoFamily
import com.example.ui.theme.NeonGreenAccent
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.RajdhaniFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Settings Dialog with language switch, notifications, backup export/import, and reset.
 */
@Composable
fun SettingsDialog(
    prefsManager: PreferencesManager,
    isArabic: Boolean,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    var notificationAlerts by remember { mutableStateOf(true) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showRestoreModal by remember { mutableStateOf(false) }
    var jsonInputText by remember { mutableStateOf("") }

    CyberDialog(
        title = AppStrings.settingsTitle(isArabic),
        onDismissRequest = onDismissRequest,
        testTag = "settings_dialog"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Language Selection Card
            Text(
                text = AppStrings.languageLabel(isArabic),
                color = TextWhite,
                fontSize = 13.sp,
                fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isArabic) NeonRedPrimary else Color(0xFF1E212D))
                        .clickable { prefsManager.setLanguage(false) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "English",
                        color = if (!isArabic) TextWhite else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isArabic) NeonRedPrimary else Color(0xFF1E212D))
                        .clickable { prefsManager.setLanguage(true) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "العربية",
                        color = if (isArabic) TextWhite else TextMuted,
                        fontSize = 13.sp,
                        fontFamily = CairoFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Notifications Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161822))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.notifications(isArabic),
                    color = TextWhite,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = notificationAlerts,
                    onCheckedChange = { notificationAlerts = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonGreenAccent, checkedTrackColor = NeonGreenAccent.copy(alpha = 0.4f))
                )
            }

            // Backup & Restore Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val json = prefsManager.exportBackupJson()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("GameBoostBackup", json))
                        Toast.makeText(context, if (isArabic) "تم نسخ كود النسخة الاحتياطية للحافظة!" else "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212D)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = AppStrings.backupExport(isArabic), fontSize = 10.sp, color = TextWhite)
                }

                Button(
                    onClick = { showRestoreModal = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212D)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = AppStrings.backupRestore(isArabic), fontSize = 10.sp, color = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Reset All Button
            Button(
                onClick = { showResetConfirm = true },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381016)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = NeonRedBright, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = AppStrings.resetAll(isArabic), color = NeonRedBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Confirmation Alert
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(AppStrings.resetAll(isArabic), color = NeonRedBright) },
            text = { Text(AppStrings.resetConfirm(isArabic), color = TextWhite) },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefsManager.resetAllData()
                        showResetConfirm = false
                        Toast.makeText(context, if (isArabic) "تمت إعادة ضبط كافة البيانات!" else "All data reset!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(AppStrings.confirm(isArabic), color = NeonRedBright, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(AppStrings.cancel(isArabic), color = TextMuted)
                }
            },
            containerColor = Color(0xFF161822)
        )
    }

    // Restore Modal
    if (showRestoreModal) {
        CyberDialog(
            title = AppStrings.backupRestore(isArabic),
            onDismissRequest = { showRestoreModal = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "الصق كود النسخة الاحتياطية (JSON) هنا:" else "Paste your Backup JSON here:",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = jsonInputText,
                    onValueChange = { jsonInputText = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NeonRedBright,
                        unfocusedBorderColor = Color(0xFF2C3042)
                    )
                )
                Button(
                    onClick = {
                        val success = prefsManager.importBackupJson(jsonInputText)
                        if (success) {
                            showRestoreModal = false
                            Toast.makeText(context, if (isArabic) "تم استعادة النسخة بنجاح!" else "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, if (isArabic) "صيغة JSON غير صالحة!" else "Invalid JSON format!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreenAccent)
                ) {
                    Text(AppStrings.confirm(isArabic), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Info Dialog with fair-play statement and usage notice.
 */
@Composable
fun InfoDialog(
    isArabic: Boolean,
    onDismissRequest: () -> Unit
) {
    CyberDialog(
        title = AppStrings.infoTitle(isArabic),
        onDismissRequest = onDismissRequest,
        testTag = "info_dialog"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "GAME BOOST AI v4.0 PRO",
                color = NeonRedBright,
                fontSize = 16.sp,
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = AppStrings.fairPlayStatement(isArabic),
                color = TextWhite,
                fontSize = 12.sp,
                fontFamily = if (isArabic) CairoFamily else RajdhaniFamily,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF161822))
                    .padding(10.dp)
            ) {
                Text(
                    text = if (isArabic) "100% متوافق مع سياسات الأمان واللعب النظيف - لا تعديل للملفات أو الذاكرة."
                    else "100% Fair-Play Compliant - Zero Memory Injection or File Tampering.",
                    color = NeonGreenAccent,
                    fontSize = 11.sp,
                    fontFamily = if (isArabic) CairoFamily else OrbitronFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
