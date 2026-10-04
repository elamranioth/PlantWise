package com.example.service.update

import java.io.File

data class AppVersionManifest(
    val versionCode: Int,
    val versionName: String,
    val releaseDate: String,
    val releaseTitle: String,
    val releaseNotes: List<String>,
    val downloadUrl: String,
    val packageSizeMb: Float,
    val sha256Checksum: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val minSdk: Int = 24,
    val isMandatory: Boolean = false
)

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    data class UpToDate(
        val currentVersion: String,
        val lastChecked: String
    ) : UpdateState
    data class UpdateAvailable(
        val manifest: AppVersionManifest,
        val currentVersion: String,
        val lastChecked: String
    ) : UpdateState
    data class Downloading(
        val progress: Float, // 0.0 to 1.0
        val downloadedMb: Float,
        val totalMb: Float,
        val manifest: AppVersionManifest
    ) : UpdateState
    data class ReadyToInstall(
        val apkFile: File,
        val manifest: AppVersionManifest
    ) : UpdateState
    data class Error(
        val message: String,
        val lastChecked: String
    ) : UpdateState
}
