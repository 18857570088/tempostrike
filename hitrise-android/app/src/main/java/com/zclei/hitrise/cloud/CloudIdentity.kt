package com.zclei.hitrise.cloud

/** Stable, locally generated identity for training records and profile sync. */
data class CloudIdentity(
    val userId: String,
    val installId: String,
    val deviceHash: String,
)
