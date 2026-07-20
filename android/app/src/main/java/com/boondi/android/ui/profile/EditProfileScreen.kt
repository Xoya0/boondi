package com.boondi.android.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoCamera
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.boondi.android.ui.common.Avatar
import com.boondi.android.ui.common.BoondiButton
import com.boondi.android.ui.theme.BoondiBorderWidth
import com.boondi.android.ui.theme.Coral100
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onClose: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.saved.collectLatest { username -> onSaved(username) }
    }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.onAvatarPicked(uri) }
    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.onBannerPicked(uri) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Edit profile", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                    },
                    actions = {
                        BoondiButton(
                            onClick = viewModel::save,
                            enabled = state.canSubmit,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.padding(end = 12.dp, top = 4.dp, bottom = 4.dp),
                        ) {
                            if (state.submitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    },
                )
                HorizontalDivider(thickness = BoondiBorderWidth, color = MaterialTheme.colorScheme.outline)
            }
        },
    ) { innerPadding ->
        // Avatar overlaps the bottom edge of the banner (matches ProfileScreen's layout) with a
        // small camera badge so it reads as tappable — previously it had no visual affordance
        // at all, unlike the banner's always-visible camera icon.
        val avatarSize = 84.dp
        val avatarRingWidth = 4.dp
        val avatarOuterSize = avatarSize + avatarRingWidth * 2
        val avatarOverlap = avatarSize / 2

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Banner picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Coral100)
                    .clickable {
                        bannerPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                val bannerModel = state.bannerUri ?: state.currentBannerUrl
                if (bannerModel != null) {
                    AsyncImage(
                        model = bannerModel,
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = "Change banner",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            // Avatar picker — reserves just enough height for the overlap so the text fields
            // below start right after the avatar's visible bottom edge (same math as
            // ProfileScreen's header: outerSize - overlap).
            Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(avatarOuterSize - avatarOverlap)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = -avatarOverlap)
                        .size(avatarOuterSize),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .clickable {
                                avatarPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Avatar(
                            imageUrl = state.avatarUri?.toString() ?: state.currentAvatarUrl,
                            name = state.username,
                            size = avatarSize,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.PhotoCamera,
                            contentDescription = "Change avatar",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = viewModel::onDisplayNameChange,
                    label = { Text("Display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("Username") },
                    prefix = { Text("@") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.bio,
                    onValueChange = viewModel::onBioChange,
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                )
                if (state.error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
