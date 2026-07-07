package com.boondi.android.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Builds a `multipart/form-data` "file" part from raw image bytes for upload endpoints. */
internal fun imagePart(bytes: ByteArray, mimeType: String?, fileName: String): MultipartBody.Part {
    val media = (mimeType ?: "image/*").toMediaTypeOrNull()
    val body = bytes.toRequestBody(media)
    return MultipartBody.Part.createFormData("file", fileName, body)
}
