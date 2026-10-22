package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun DashboardHero(
    isCleaning: Boolean,
    progress: Float,
    cleaningStep: String,
    estimatedJunkBytes: Long,
    lastCleanedBytes: Long,
    totalCleanedBytes: Long,
    language: String,
    onCleanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCleaning) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Status Circle
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Background Soft Mint Circle
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                        .background(EmeraldContainer)
                        .border(1.5.dp, Color(0xFFA7F3D0), CircleShape)
                )

                // Circular Progress if cleaning
                if (isCleaning) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(185.dp),
                        color = EmeraldPrimary,
                        strokeWidth = 6.dp,
                        trackColor = Color(0xFFD1FAE5),
                        strokeCap = StrokeCap.Round
                    )
                }

                // Inner content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isCleaning) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Cleaning in progress",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    } else {
                        val sizeText = FormatUtils.formatFileSize(estimatedJunkBytes)
                        Text(
                            text = sizeText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == "bn") "ক্লিন করার মতো ক্যাচ" else "Cached Junk Found",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            if (isCleaning) {
                Text(
                    text = cleaningStep.ifEmpty {
                        if (language == "bn") "ক্লিনিং চলছে..." else "Cleaning in progress..."
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Center
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (estimatedJunkBytes > 0) EmeraldPrimary else Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == "bn") "সিস্টেম রেডি • দ্রুত ক্লিন সম্ভব" else "System Ready • Fast Clean Available",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Minimal Clean Now Button
            Button(
                onClick = onCleanClick,
                enabled = !isCleaning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("clean_now_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFA7F3D0),
                    disabledContentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isCleaning) Icons.Default.Done else Icons.Default.CleaningServices,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isCleaning) {
                        if (language == "bn") "ক্লিন হচ্ছে..." else "Cleaning..."
                    } else {
                        if (language == "bn") "এখনই ক্লিন করুন (Clean Now)" else "Clean Cache Now"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick stats row: Last Cleaned & Total Freed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (language == "bn") "সর্বশেষ মুক্ত" else "Last Cleaned",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = FormatUtils.formatFileSize(lastCleanedBytes),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (language == "bn") "মোট খালি করা হয়েছে" else "Total Freed",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = FormatUtils.formatFileSize(totalCleanedBytes),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}
