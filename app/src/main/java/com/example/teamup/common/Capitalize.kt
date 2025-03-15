package com.example.teamup.common

import java.util.*

fun Capitalize(str: String): String {
    return str.trim().split("\\s+".toRegex())
        .map { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }.joinToString(" ")
}