package com.gokcank.triviaquiz.util

/** HTML entity'lerini düz metne çevirir. OpenTDB soruları HTML-encoded gelebiliyor. */
fun String.decodeHtml(): String =
    android.text.Html.fromHtml(this, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
