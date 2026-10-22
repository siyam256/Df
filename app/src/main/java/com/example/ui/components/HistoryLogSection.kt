package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanRecord
import com.example.model.CleanType
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun HistoryLogSection(
    historyList: List<CleanRecord>,
    language: String,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == "bn") "ক্লিনিং হিস্ট্রি" else "Cleaning History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (historyList.isNotEmpty()) {
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear history",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (historyList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = Color(0xFFA7F3D0),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == "bn") {
                            "এখনো কোনো ক্লিনিং হিস্ট্রি নেই"
                        } else {
                            "No cleaning history yet"
                        },
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (language == "bn") {
                            "শিডিউল বা ম্যানুয়াল ক্লিনের পর এখানে রেকর্ড থাকবে"
                        } else {
                            "Automatic & manual clean records will appear here"
                        },
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            historyList.take(6).forEach { record ->
                HistoryItemRow(record = record, language = language)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HistoryItemRow(
    record: CleanRecord,
    language: String
) {
    val isAuto = record.cleanType == CleanType.AUTO_SCHEDULED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAuto) MintContainer else EmeraldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAuto) Icons.Default.AutoAwesome else Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAuto) {
                                if (language == "bn") "অটো শিডিউল ক্লিন" else "Auto Scheduled Clean"
                            } else {
                                if (language == "bn") "ম্যানুয়াল ক্লিন" else "Manual Clean"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = FormatUtils.formatDateTime(record.timestamp),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "+${FormatUtils.formatFileSize(record.freedBytes)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
        }
    }
}
