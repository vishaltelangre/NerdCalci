package com.vishaltelangre.nerdcalci.ui

import android.text.format.DateFormat
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import java.util.Date

fun labelValueText(label: String, value: String) = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
        append(label)
    }
    append(" ")
    append(value)
}

fun formatFriendlyDateTime(value: Long): String {
    return DateFormat.format("MMM d, yyyy h:mm a", Date(value)).toString()
}
