package com.ludocode.ludocodebackend.storage.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storage")
class StorageProperties (
    var mode: Mode = Mode.LOCAL,
    var local: Local = Local(),
    var gcs: Gcs = Gcs(),
    var s3: S3 = S3()
) {
   enum class Mode {LOCAL, GCS, S3}

    data class Local(var bucketName: String = "/data/local-bucket")

    data class Gcs(
        var projectId: String = "",
        var bucket: String = "",
        var host: String = "",
        var credentialsJson: String = ""
    )

    data class S3(
        var region: String = "eu-central-1",
        var bucket: String = "",
        var endpoint: String = ""
    )



}