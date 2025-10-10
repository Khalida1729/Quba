package com.kashif1729.quba
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kashif1729.quba.ui.theme.QubaTheme
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QubaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

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

private fun getSubjectsForClass(className: String): List<String> {
    return when (className) {"Nursery" -> listOf("Urdu", "Hindi", "English", "Arabic", "Mathematics", "Counting", "Dua")
        "KG" -> listOf("Urdu", "Hindi", "English", "Drawing", "Arabic", "Mathematics", "Dua")
        "1st" -> listOf("Urdu", "Hindi", "English", "Diniyat", "Arabic", "Mathematics", "Dua")
        "2nd" -> listOf("Urdu", "Hindi", "English", "Diniyat", "Arabic", "Mathematics", "Science", "Social Study", "Moral Education", "Dua")
        else -> listOf("Urdu", "Hindi", "English", "Diniyat", "Arabic", "Mathematics", "Science", "Social Study", "Moral Education", "Dua")
    }
    }


@Composable
fun DatePickerField(
    date: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        onDateSelected(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }
    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text("Date") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        readOnly = true,
        singleLine = true,
        interactionSource = interactionSource,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

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
                halfYearly = value
                val num = value.toIntOrNull()
                halfYearlyError = value.isNotEmpty() && (num == null || num !in 0..50)
                if (halfYearlyError) onError()
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

fun generateAndSavePdf(
    context: android.content.Context,
    studentName: String,
    fatherName: String,
    rollNo: String,
    className: String,
    date: String,
    subjectMarks: SnapshotStateMap<String, Pair<Int, Int>>,
    setLoading: (Boolean) -> Unit
) {
    try {
        if (!isStorageAvailable()) {
            Toast.makeText(context, "Storage not available", Toast.LENGTH_LONG).show()
            return
        }
        setLoading(true)
        generatePdf(
            studentName = studentName,
            fatherName = fatherName,
            rollNo = rollNo,
            className = className,
            date = date,
            subjectMarks = subjectMarks,
            onComplete = { success, message ->
                setLoading(false)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        )
    } catch (e: Exception) {
        setLoading(false)
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun getOutputFile(className: String, studentName: String): File {
    val sanitizedClass = className.replace("[^a-zA-Z0-9-_.]".toRegex(), "_")
    val sanitizedName = studentName.replace("[^a-zA-Z0-9-_.]".toRegex(), "_")
    val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val outputDir = File(baseDir, "Quba/$sanitizedClass").apply { mkdirs() }
    var fileName = "${sanitizedName}.pdf"
    var file = File(outputDir, fileName)
    var counter = 1
    while (file.exists()) {
        fileName = "${sanitizedName}$counter.pdf"
        file = File(outputDir, fileName)
        counter++
    }
    return file
}

fun generatePdf(
    studentName: String,
    fatherName: String,
    rollNo: String,
    className: String,
    date: String,
    subjectMarks: Map<String, Pair<Int, Int>>,
    onComplete: (Boolean, String) -> Unit
) {
    try {
        val file = getOutputFile(className, studentName)
        val parentFile = file.parentFile
        if (parentFile?.exists() != true && !parentFile?.mkdirs()!!) {
            throw IOException("Failed to create directory")
        }
        val boldFont = PdfFontFactory.createFont("Helvetica-Bold")
        FileOutputStream(file).use { outputStream ->
            PdfWriter(outputStream).use { pdfWriter ->
                PdfDocument(pdfWriter).use { pdfDocument ->
                    Document(pdfDocument).use { document ->
                        // Header
                        document.add(
                            Paragraph("Permanently Recognized by Basic Shiksha Parishad\nExamination 2025-2026")
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontSize(12f)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        document.add(
                            Paragraph("QUBA PUBLIC SCHOOL")
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontSize(20f)
                                .setFont(boldFont)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        document.add(
                            Paragraph("Tulsiyapur (Chauraha), P.O Aundahi Kalan (Barhani)\nDistt. Siddharth Nagar, U.P (India)")
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontSize(12f)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        document.add(
                            Paragraph("MARKSHEET")
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontSize(18f)
                                .setFont(boldFont)
                                .setUnderline()
                                .setFontColor(ColorConstants.BLACK)
                        )
                        document.add(Paragraph("\n"))
                        // Student Details
                        val nameTable = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()
                        nameTable.addCell(
                            Cell().add(Paragraph("Student Name: $studentName"))
                                .setBorder(Border.NO_BORDER)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        nameTable.addCell(
                            Cell().add(Paragraph("Father's Name: $fatherName"))
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBorder(Border.NO_BORDER)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        document.add(nameTable)
                        val classTable = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()
                        classTable.addCell(
                            Cell().add(Paragraph("Class: $className"))
                                .setBorder(Border.NO_BORDER)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        classTable.addCell(
                            Cell().add(Paragraph("Roll No: $rollNo"))
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBorder(Border.NO_BORDER)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        document.add(classTable)
                        document.add(Paragraph("\n"))
                        // Marks Table
                        val table = Table(UnitValue.createPercentArray(floatArrayOf(10f, 40f, 20f, 20f, 10f))).useAllAvailableWidth()
                        table.addHeaderCell(
                            Cell().add(Paragraph("S.No").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        table.addHeaderCell(
                            Cell().add(Paragraph("Subject").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        table.addHeaderCell(
                            Cell().add(Paragraph("Half Yearly").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        table.addHeaderCell(
                            Cell().add(Paragraph("Annual").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        table.addHeaderCell(
                            Cell().add(Paragraph("Total").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setFontColor(ColorConstants.BLACK)
                        )
                        var halfYearlyTotal = 0
                        var annualTotal = 0
                        var grandTotal = 0
                        subjectMarks.entries.forEachIndexed { index, (subject, marks) ->
                            val total = marks.first + marks.second
                            halfYearlyTotal += marks.first
                            annualTotal += marks.second
                            grandTotal += total
                            table.addCell(Cell().add(Paragraph("${index + 1}")))
                            table.addCell(Cell().add(Paragraph(subject)))
                            table.addCell(Cell().add(Paragraph("${marks.first}")))
                            table.addCell(Cell().add(Paragraph("${marks.second}")))
                            table.addCell(Cell().add(Paragraph("$total").setFont(boldFont)))
                        }
                        // Grand Total Row
                        table.addCell(
                            Cell().add(Paragraph(""))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        )
                        table.addCell(
                            Cell().add(Paragraph("Grand Total").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        )
                        table.addCell(
                            Cell().add(Paragraph("$halfYearlyTotal").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        )
                        table.addCell(
                            Cell().add(Paragraph("$annualTotal").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        )
                        table.addCell(
                            Cell().add(Paragraph("$grandTotal").setFont(boldFont))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        )
                        document.add(table)
                        // Result
                        val maxPossibleMarks = subjectMarks.size * 100
                        val percentage = if (maxPossibleMarks > 0) {
                            (grandTotal.toFloat() / maxPossibleMarks) * 100
                        } else {
                            0f
                        }
                        val division = when {
                            percentage >= 60 -> "First"
                            percentage >= 45 -> "Second"
                            percentage >= 33 -> "Third"
                            else -> "Fail"
                        }
                        val result = if (percentage >= 33) "Pass" else "Fail"
                        document.add(Paragraph("\n"))
                        val resultTable = Table(floatArrayOf(1f, 1f, 1f)).useAllAvailableWidth()
                        resultTable.addCell(
                            Cell().add(Paragraph("Percentage: ${"%.2f".format(percentage)}%").setFont(boldFont))
                                .setBorder(Border.NO_BORDER)
                        )
                        resultTable.addCell(
                            Cell().add(Paragraph("Division: $division").setFont(boldFont))
                                .setBorder(Border.NO_BORDER)
                        )
                        resultTable.addCell(
                            Cell().add(Paragraph("Result: $result").setFont(boldFont))
                                .setBorder(Border.NO_BORDER)
                        )
                        document.add(resultTable)
                        document.add(Paragraph("\n\n\n"))
                        document.add(Paragraph("Date: $date"))
                        // Signatures
                        val signatureTable = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()
                        signatureTable.addCell(
                            Cell().add(Paragraph("Teacher's Signature: _______________"))
                                .setBorder(Border.NO_BORDER)
                        )
                        signatureTable.addCell(
                            Cell().add(Paragraph("Principal's Signature: _______________"))
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBorder(Border.NO_BORDER)
                        )
                        document.add(signatureTable)
                    }
                }
            }
        }
        onComplete(true, "PDF saved at ${file.absolutePath}")
    } catch (e: Exception) {
        onComplete(false, "Error: ${e.message}")
    }
}

fun isStorageAvailable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}