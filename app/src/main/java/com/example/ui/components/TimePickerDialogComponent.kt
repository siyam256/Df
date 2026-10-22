package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogComponent(
    initialHour: Int,
    initialMinute: Int,
    language: String,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                },
                modifier = Modifier.testTag("confirm_time_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (language == "bn") "সময় সেভ করুন" else "Save Time",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (language == "bn") "বাতিল" else "Cancel",
                    color = TextPrimary
                )
            }
        },
        title = {
            Text(
                text = if (language == "bn") "ডেইলি অটো ক্লিনের সময় নির্ধারণ" else "Set Daily Auto Clean Time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (language == "bn") {
                        "প্রতিদিন এই সময়ে ফোন স্লিপ থাকলেও ব্যাকগ্রাউন্ডে ক্যাচ সাফ হবে।"
                    } else {
                        "Cache will be cleaned automatically at this time every day, even when sleeping."
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF1F5F9),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = TextPrimary,
                        selectorColor = EmeraldPrimary,
                        containerColor = Color.White,
                        periodSelectorBorderColor = EmeraldPrimary,
                        periodSelectorSelectedContainerColor = EmeraldContainer,
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorSelectedContentColor = EmeraldOnContainer,
                        periodSelectorUnselectedContentColor = TextPrimary,
                        timeSelectorSelectedContainerColor = EmeraldContainer,
                        timeSelectorUnselectedContainerColor = Color(0xFFF1F5F9),
                        timeSelectorSelectedContentColor = EmeraldPrimary,
                        timeSelectorUnselectedContentColor = TextPrimary
                    )
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
