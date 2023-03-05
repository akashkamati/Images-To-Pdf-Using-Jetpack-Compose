package com.akash.images_to_pdf_using_jetpack_compose

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = GetMultipleContents(),
        onResult = {
            viewModel.onImagesSelected(it, context)
        }
    )

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument(),
        onResult = {
            viewModel.writeToSelectedPath(it, context)
        }
    )

    val state = viewModel.state.collectAsState()
    val imageBitmaps = state.value.imageBitmaps
    val isLoading = state.value.isLoading
    val success = state.value.success

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    LaunchedEffect(key1 = success ) {
        if (success != null){
            if (success) {
                Toast.makeText(
                    context,
                    "Successfully converted images to pdf",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        if (imageBitmaps.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.75f)
            ) {
                itemsIndexed(imageBitmaps) { index: Int, bitmap: Bitmap ->
                    ImagePreviewItem(
                        bitmap = bitmap,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 15.dp),
                        onRemoveClick = {
                            viewModel.removeImage(index)
                        }
                    )
                }
            }
        } else Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.75f),
            contentAlignment = Center
        ){
            Text(text = "Select some images")
        }

        Box(contentAlignment = BottomCenter, modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()) {
            Column(horizontalAlignment = CenterHorizontally) {

                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Select images")
                }

                Button(
                    enabled = imageBitmaps.isNotEmpty(),
                    onClick = { createDocumentLauncher.launch("ImgToPdf_${System.currentTimeMillis()}.pdf") }
                ) {
                    Text("Save")
                }
            }
        }
    }

    if (isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center){
            AlertDialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                buttons = {}
            )
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ImagePreviewItem(bitmap: Bitmap, onRemoveClick: () -> Unit,modifier: Modifier) {

    Column(modifier = modifier){
        AsyncImage(
            model = bitmap,
            contentDescription = null,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onRemoveClick,
            colors = buttonColors(backgroundColor = Color.Red, contentColor = Color.White),
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Text(text = "Remove")
        }
    }
}