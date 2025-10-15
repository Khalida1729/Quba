package com.kashif1729.quba.ui.kashif

fun getSubjectsForClass(className: String): List<String> {
    return when (className) {"Nursery" -> listOf("Urdu", "Hindi", "English", "Arabic", "Mathematics", "Counting", "Dua")
        "KG" -> listOf("Urdu", "Hindi", "English", "Drawing", "Arabic", "Mathematics", "Dua")
        "1st" -> listOf("Urdu", "Hindi", "English", "Diniyat", "Arabic", "Mathematics", "Dua")
        "2nd" -> listOf("Urdu", "Hindi", "English", "Diniyat", "Arabic", "Mathematics", "Science", "Social Study", "Moral Education", "Dua")
        else -> listOf("Urdu", "Hindi", "English", "Diniyat", "Arabic", "Mathematics", "Science", "Social Study", "Moral Education", "Dua")
    }
}
