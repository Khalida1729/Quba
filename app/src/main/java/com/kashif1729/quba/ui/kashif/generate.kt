package com.kashif1729.quba.ui.kashif

import android.os.Environment
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
