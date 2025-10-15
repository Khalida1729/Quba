package com.kashif1729.quba.ui.kashif
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    var studentName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Nursery") }
    var date by remember { mutableStateOf(dateFormat.format(calendar.time)) }
    val subjectMarks = remember { mutableStateMapOf<String, Pair<Int, Int>>() }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var formResetKey by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val classes = listOf("Nursery", "KG", "1st", "2nd", "3rd", "4th", "5th")
    val subjects = getSubjectsForClass(selectedClass)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = lazyListState
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Student Details",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp),
                        textAlign = TextAlign.Center
                    )
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = {
                            if (it.all { char -> char.isLetter() || char.isWhitespace() }) {
                                studentName = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Student Name") },
                        isError = errorMessage != null && studentName.isBlank(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = {
                            if (it.all { char -> char.isLetter() || char.isWhitespace() }) {
                                fatherName = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Father's Name") },
                        isError = errorMessage != null && fatherName.isBlank(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    // Fixed Class Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedClass,
                            onValueChange = {},
                            label = { Text("Class") },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .menuAnchor(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            classes.forEach { className ->
                                DropdownMenuItem(
                                    text = { Text(className) },
                                    onClick = {
                                        selectedClass = className
                                        subjectMarks.clear()
                                        formResetKey++
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 6) {
                                rollNo = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Roll No") },
                        isError = errorMessage != null && rollNo.isBlank(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    DatePickerField(
                        date = date,
                        onDateSelected = { date = it }
                    )
                }
            }
        }
        item {
            Text(
                text = "Subject Marks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }
        itemsIndexed(subjects) { index, subject ->
            key(subject, formResetKey) {
                SubjectMarksInput(
                    subject = subject,
                    //subjectIndex = index,
                    //totalSubjects = subjects.size,
                    subjectMarks = subjectMarks,
                    onMarksChanged = { halfYearly, annual ->
                        subjectMarks[subject] = Pair(halfYearly, annual)
                    },
                    onError = {
                        scope.launch {
                            lazyListState.animateScrollToItem(index + 1)
                        }
                    },
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        when {
                            studentName.isBlank() -> errorMessage = "Please enter student name"
                            fatherName.isBlank() -> errorMessage = "Please enter father's name"
                            rollNo.isBlank() -> errorMessage = "Please enter roll number"
                            subjectMarks.isEmpty() -> errorMessage = "Please enter marks for at least one subject"
                            subjectMarks.any { it.value.first > 50 || it.value.second > 50 } ->
                                errorMessage = "Marks must be between 0-50"
                            else -> {
                                errorMessage = null
                                generateAndSavePdf(
                                    context = context,
                                    studentName = studentName,
                                    fatherName = fatherName,
                                    rollNo = rollNo,
                                    className = selectedClass,
                                    date = date,
                                    subjectMarks = subjectMarks,
                                    setLoading = { isLoading = it }
                                )
                            }
                        }
                        if (errorMessage != null) {
                            scope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Generate PDF", fontSize = 16.sp)
                    }
                }
                Button(
                    onClick = {
                        studentName = ""
                        fatherName = ""
                        rollNo = ""
                        selectedClass = "Nursery"
                        date = dateFormat.format(calendar.time)
                        subjectMarks.clear()
                        errorMessage = null
                        formResetKey++
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Clear Form", fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}