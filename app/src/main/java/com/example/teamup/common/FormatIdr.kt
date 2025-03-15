package com.example.teamup.common

import java.text.NumberFormat
import java.util.*

fun FormatIDR(price: Int): String {
    val localeId: Locale = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeId)
    return numberFormat.format(price).toString()
}