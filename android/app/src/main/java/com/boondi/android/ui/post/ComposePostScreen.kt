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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.BoondiButton
import com.boondi.android.ui.theme.BoondiBorderWidth
import com.boondi.android.ui.theme.Coral500
import kotlinx.coroutines.flow.collectLatest

/**
 * Full-screen post composer (E4-09): text with a live character counter, optional image
 * attachment, and submit. Reused for replies (E6-18) — when opened via [Routes.compose] with
 * a parentPostId, the ViewModel fetches and previews that parent post above the text field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostScreen(
    onClose: () -> Unit,
    onPosted: (String) -> Unit,
    viewModel: ComposePostViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isReply = viewModel.parentPostId != null

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.posted.collectLatest { postId -> onPosted(postId) }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.onImagePicked(uri) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (isReply) "Reply" else "New post", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        BoondiButton(
                            onClick = viewModel::submit,
                            enabled = state.canSubmit,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.padding(end = 12.dp, top = 4.dp, bottom = 4.dp),
                        ) {
                            if (state.submitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text(if (isReply) "Reply" else "Post")
                            }
                        }
                    },
                )
                HorizontalDivider(thickness = BoondiBorderWidth, color = MaterialTheme.colorScheme.outline)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp),
        ) {
            state.parentPost?.let { parent ->
                ReplyingToPreview(parent)
                Spacer(Modifier.height(12.dp))
            }

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
                            .clip(RoundedCornerShape(16.dp))
                            .border(BoondiBorderWidth, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
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
                        state.remaining <= 50 -> Coral500
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

/** Read-only parent-post preview shown above the composer when replying (E6-18). */
@Composable
private fun ReplyingToPreview(parent: com.boondi.android.domain.model.Post) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(imageUrl = parent.author.profilePictureUrl, name = parent.author.name, size = 32.dp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(parent.author.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "@${parent.author.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (parent.content.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = parent.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Replying to @${parent.author.username}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
