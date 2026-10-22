package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleaner.AppCleanState
import com.example.model.AppCacheItem
import com.example.ui.theme.DarkEmerald
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun AppsCacheManagerCard(
    apps: List<AppCacheItem>,
    isAccessibilityEnabled: Boolean,
    hasUsageStatsPermission: Boolean,
    cleanState: AppCleanState,
    language: String,
    onStartAutoClean: () -> Unit,
    onStopAutoClean: () -> Unit,
    onOpenAppStorage: (String) -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAllApps by remember { mutableStateOf(false) }

    val totalAppsCache = apps.sumOf { it.cacheBytes }
    val displayApps = if (showAllApps) apps else apps.take(5)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (language == "bn") "ইনস্টল করা অ্যাপের ক্যাচ" else "Installed Apps Cache",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (language == "bn") {
                                "${apps.size} টি অ্যাপ • মোট ${FormatUtils.formatFileSize(totalAppsCache)}"
                            } else {
                                "${apps.size} apps • Total ${FormatUtils.formatFileSize(totalAppsCache)}"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation & Permission Status
            if (!isAccessibilityEnabled) {
                // Helpful guidance card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0FDF4))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == "bn") {
                                    "অটোমেটিক সব অ্যাপ ক্যাচ ক্লিনিং"
                                } else {
                                    "Automated All Apps Cache Clean"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (language == "bn") {
                                "অ্যান্ড্রয়েড সুরক্ষার কারণে অন্য অ্যাপের ক্যাচ সরাসরি মুছতে অ্যাক্সেসিবিলিটি সাহায্য প্রয়োজন। এটি স্বয়ংক্রিয়ভাবে প্রতিটি অ্যাপের 'ক্যাশে মুছুন' বাটনে ক্লিক করে দেবে।"
                            } else {
                                "Android security requires Accessibility Service to automate tapping 'Clear Cache' inside each app's storage settings for you."
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onRequestAccessibility,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("enable_accessibility_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == "bn") {
                                    "অ্যাক্সেসিবিলিটি চালু করুন (Enable Auto Clean)"
                                } else {
                                    "Enable Auto Deep Cleaner"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // If Accessibility is enabled
                if (cleanState.isRunning) {
                    // Running progress state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(EmeraldContainer)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 3.dp,
                                        color = EmeraldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (language == "bn") {
                                            "ক্লিন হচ্ছে: ${cleanState.currentAppIndex}/${cleanState.totalAppsToClean}"
                                        } else {
                                            "Cleaning: ${cleanState.currentAppIndex}/${cleanState.totalAppsToClean}"
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldOnContainer
                                    )
                                }

                                OutlinedButton(
                                    onClick = onStopAutoClean,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == "bn") "থামান" else "Stop",
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${cleanState.currentAppName} ...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (language == "bn") {
                                    "মুছে ফেলা হয়েছে: ${FormatUtils.formatFileSize(cleanState.totalBytesCleanedSoFar)}"
                                } else {
                                    "Freed so far: ${FormatUtils.formatFileSize(cleanState.totalBytesCleanedSoFar)}"
                                },
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    // Ready to Run One-Tap Auto Clean button
                    Button(
                        onClick = onStartAutoClean,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auto_clean_apps_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == "bn") {
                                "অটো সব অ্যাপের ক্যাচ মুছুন (Auto Clean All)"
                            } else {
                                "Auto Clean All Apps Cache"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle for per-app manual / list view
            Text(
                text = if (language == "bn") "অ্যাপগুলোর তালিকা ও ক্যাচ সাইজ:" else "Apps List & Cache Size:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            // App list items
            displayApps.forEach { item ->
                AppCacheItemRow(
                    item = item,
                    language = language,
                    onCleanApp = { onOpenAppStorage(item.packageName) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (apps.size > 5) {
                OutlinedButton(
                    onClick = { showAllApps = !showAllApps },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (showAllApps) {
                            if (language == "bn") "কম দেখান (Show Less)" else "Show Less"
                        } else {
                            if (language == "bn") "সব অ্যাপ দেখুন (+${apps.size - 5} টি)" else "View All Apps (+${apps.size - 5})"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }

            // Usage Access suggestion if not granted
            if (!hasUsageStatsPermission) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "bn") {
                            "রিয়েল-টাইম বাইট সাইজের জন্য পারমিশন দিন"
                        } else {
                            "Allow Usage Access for exact byte tracking"
                        },
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onRequestUsageAccess,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (language == "bn") "অনুমতি" else "Allow",
                            fontSize = 11.sp,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCacheItemRow(
    item: AppCacheItem,
    language: String,
    onCleanApp: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (item.isCleaned) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
            .border(
                1.dp,
                if (item.isCleaned) Color(0xFFA7F3D0) else Color(0xFFE2E8F0),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (item.isCleaned) EmeraldContainer else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (item.isCleaned) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = item.appName.firstOrNull()?.uppercase() ?: "A",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = item.appName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = if (item.isCleaned) {
                        if (language == "bn") "ক্যাচ পরিষ্কার হয়েছে" else "Cache cleaned"
                    } else {
                        FormatUtils.formatFileSize(item.cacheBytes)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isCleaned) EmeraldPrimary else TextSecondary
                )
            }
        }

        // Direct 1-tap Clean Button for this specific app
        Button(
            onClick = onCleanApp,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(34.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (item.isCleaned) Color(0xFFD1FAE5) else EmeraldPrimary,
                contentColor = if (item.isCleaned) EmeraldOnContainer else Color.White
            )
        ) {
            Text(
                text = if (item.isCleaned) {
                    if (language == "bn") "সম্পন্ন" else "Cleaned"
                } else {
                    if (language == "bn") "ক্লিন" else "Clean"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open app storage settings",
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
