package com.kashif1729.quba.ui.kashif

import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateMap

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
