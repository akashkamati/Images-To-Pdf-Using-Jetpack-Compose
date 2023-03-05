package com.akash.images_to_pdf_using_jetpack_compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainViewModel : ViewModel() {

    private var _state  = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    fun onImagesSelected(uris: List<Uri>,context: Context){
        viewModelScope.launch {
            val newList = _state.value.imageBitmaps.toMutableList()
            uris.forEach { uri ->
                val bitmap = uriToBitmap(uri, context)
                if (bitmap != null) {
                    newList.add(bitmap)
                }
            }
            _state.value = state.value.copy(
                imageBitmaps = newList
            )
        }
    }

    fun removeImage(index:Int){
        viewModelScope.launch {
            val newList = _state.value.imageBitmaps.toMutableList()
            newList.removeAt(index)
            _state.value = state.value.copy(
                imageBitmaps = newList
            )
        }
    }

    fun writeToSelectedPath(selectedPathUri: Uri,context: Context){

        viewModelScope.launch {
            try {
                context.contentResolver.openFileDescriptor(selectedPathUri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { stream ->
                        if (_state.value.imageBitmaps.isNotEmpty()){
                            createPdf(_state.value.imageBitmaps,stream)
                        } else{
                            return@launch
                        }
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun createPdf(bitmaps: List<Bitmap>,stream: FileOutputStream){
        withContext(Dispatchers.IO) {
            _state.value = state.value.copy(
                isLoading = true
            )
            val document = PdfDocument()

            bitmaps.forEachIndexed { index , bitmap ->
                println("the page no is  $index")
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index+1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()

                val scaledBm = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, false)

                canvas.drawBitmap(scaledBm, 0F, 0F, paint)
                document.finishPage(page)
            }

            try {
                document.writeTo(stream)
                _state.value = state.value.copy(
                    success = true
                )
            } catch (e: Exception) {
                _state.value = state.value.copy(
                    success = false
                )
                e.printStackTrace()
            } finally {
                document.close()
                _state.value = state.value.copy(
                    isLoading = false,
                    imageBitmaps = emptyList()
                )
            }
        }
    }

    private suspend fun uriToBitmap(uri : Uri, context: Context): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
                return@withContext image
            } catch (e: IOException) {
                e.printStackTrace()
            }
            null
        }
    }

}