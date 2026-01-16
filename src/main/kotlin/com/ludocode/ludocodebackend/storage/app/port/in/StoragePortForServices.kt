package com.ludocode.ludocodebackend.storage.app.port.`in`

import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageGetRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.dto.response.StorageContentMap
import com.ludocode.ludocodebackend.storage.app.dto.response.UploadedPaths

interface StoragePortForServices {

    fun getList (req: StorageGetRequest) : StorageContentMap
    fun uploadList (req: StoragePutRequestList): UploadedPaths
    fun get(path: String): String
    fun deleteList (req: StorageDeleteRequest): UploadedPaths

}
