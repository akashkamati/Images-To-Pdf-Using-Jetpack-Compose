package com.akash.images_to_pdf_using_jetpack_compose

import android.graphics.Bitmap

data class MainScreenState(
    val imageBitmaps : List<Bitmap> = emptyList(),
    val isLoading : Boolean = false,
    val success : Boolean? = null
)
