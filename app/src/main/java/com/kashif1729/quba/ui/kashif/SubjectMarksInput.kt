package com.kashif1729.quba.ui.kashif

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SubjectMarksInput(
    subject: String,
    subjectMarks: SnapshotStateMap<String, Pair<Int, Int>>,
    onMarksChanged: (Int, Int) -> Unit,
    onError: () -> Unit,
) {
    var halfYearly by remember(subject, subjectMarks) {
        mutableStateOf(subjectMarks[subject]?.first?.toString() ?: "")
    }
    var annual by remember(subject, subjectMarks) {
        mutableStateOf(subjectMarks[subject]?.second?.toString() ?: "")
    }
    var halfYearlyError by remember { mutableStateOf(false) }
    var annualError by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = subject,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            textAlign = TextAlign.Start
        )
        OutlinedTextField(
            value = halfYearly,
            onValueChange = { value ->
                halfYearly = value
                val num = value.toIntOrNull()
                halfYearlyError = value.isNotEmpty() && (num == null || num !in 0..50)
                if (halfYearlyError) onError()
                onMarksChanged(num ?: 0, annual.toIntOrNull() ?: 0)

            },
            label = { Text("HY") },
            isError = halfYearlyError,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            supportingText = {
                if (halfYearlyError) {
                    Text("0-50", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        OutlinedTextField(
            value = annual,
            onValueChange = {value ->
                annual = value
                val num = value.toIntOrNull()
                annualError = value.isNotEmpty() && (num == null || num !in 0..50)
                if (annualError) onError()
                onMarksChanged(num ?: 0, annual.toIntOrNull() ?: 0)},
            label = { Text("Annual") },
            isError = annualError,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),

            supportingText = {
                if (annualError) {
                    Text("0-50", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
