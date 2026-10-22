package com.example.model

enum class CleanType {
    AUTO_SCHEDULED,
    MANUAL
}

data class CleanRecord(
    val id: String,
    val timestamp: Long,
    val freedBytes: Long,
    val cleanType: CleanType,
    val itemsCleaned: Int,
    val note: String = ""
)

data class CleanCategory(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val sizeBytes: Long,
    val isSelected: Boolean = true
)

data class CleanResult(
    val freedBytes: Long,
    val itemsCleaned: Int,
    val durationMs: Long,
    val success: Boolean,
    val message: String
)

data class DeviceStorageStats(
    val totalAppCacheBytes: Long,
    val tempFilesBytes: Long,
    val thumbnailBytes: Long,
    val apkLeftoverBytes: Long,
    val memoryFreedEstimateBytes: Long
) {
    val totalJunkBytes: Long
        get() = totalAppCacheBytes + tempFilesBytes + thumbnailBytes + apkLeftoverBytes + memoryFreedEstimateBytes
}
