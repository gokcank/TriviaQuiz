package com.gokcank.triviaquiz.data.model

data class Category(val name: String?)   // null = tüm kategoriler

/** Yerel JSON'daki kategori isimleri */
val CATEGORIES = listOf(
    Category(null),
    Category("Genel Kültür"),
    Category("Türk Tarihi"),
    Category("Dünya Tarihi"),
    Category("Coğrafya"),
    Category("Bilim & Doğa"),
    Category("Spor"),
    Category("Film & Dizi"),
    Category("Müzik"),
    Category("Teknoloji"),
    Category("Matematik"),
    Category("Edebiyat"),
    Category("Sanat"),
    Category("Hayvanlar")
)

val Category.displayName: String
    get() = name ?: "Tüm Kategoriler"
