package com.boondi.android.ui.post

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

/**
 * Full-screen post composer (E4-09): text with a live character counter, optional image
 * attachment, and submit. Reused for replies by passing [parentPostId].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostScreen(
    onClose: () -> Unit,
    onPosted: (String) -> Unit,
    parentPostId: String? = null,
    viewModel: ComposePostViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.posted.collectLatest { postId -> onPosted(postId) }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.onImagePicked(uri) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (parentPostId == null) "New post" else "Reply") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.submit(parentPostId) },
                        enabled = state.canSubmit,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (state.submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(if (parentPostId == null) "Post" else "Reply")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.text,
                onValueChange = viewModel::onTextChange,
                placeholder = { Text("What's happening?") },
                isError = state.overLimit,
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )

            if (state.imageUri != null) {
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Attached image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                    if (state.uploadingImage) {
                        Box(
                            Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = Color.White) }
                    }
                    IconButton(
                        onClick = viewModel::onRemoveImage,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove image", tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    enabled = state.imageUri == null,
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = "Attach image",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = state.remaining.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        state.overLimit -> MaterialTheme.colorScheme.error
                        state.remaining <= 50 -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
