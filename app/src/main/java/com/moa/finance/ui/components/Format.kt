package com.moa.finance.ui.components

import java.text.DecimalFormat

private val wonFormat = DecimalFormat("#,###")

fun formatNumber(value: Long): String = wonFormat.format(value)

fun formatWon(value: Long): String = formatNumber(value) + "원"

fun formatSignedWon(value: Long): String =
    (if (value > 0) "+" else if (value < 0) "−" else "") + formatWon(kotlin.math.abs(value))
