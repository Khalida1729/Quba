package com.kashif1729.quba.ui.kashif
import android.os.Environment

fun isStorageAvailable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}