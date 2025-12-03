package com.ludocode.ludocodebackend.playground.app.util

import com.ludocode.ludocodebackend.commons.util.sha256
import com.ludocode.ludocodebackend.playground.app.dto.response.ProjectSnapshotDiff
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import java.util.UUID
import kotlin.collections.contains

object ProjectSnapshotDiffer {


    fun computeSnapshotDiff (incomingFiles: List<ProjectFileSnapshot>, existingFiles: List<ProjectFile>): ProjectSnapshotDiff {


        val filesToAdd = mutableListOf<ProjectFileSnapshot>()
        val filesToUpdate = mutableListOf<ProjectFileSnapshot>()
        val filesToDelete = mutableListOf<ProjectFile>()

        val incomingIds = incomingFiles.mapNotNull { it.id }.toSet()
        val existingFileMap = existingFiles
            .filter { it.id in incomingIds }
            .associateBy { it.id }
        filesToDelete.addAll(existingFiles.map { it }.filter { it.id !in incomingIds })
        val filesToDeleteIds = filesToDelete.map { it.id }

        val filteredIncoming = incomingFiles.filter { it.id !in filesToDeleteIds }

        for (incoming in filteredIncoming) {

            if (incoming.id == null) {
                filesToAdd.add(incoming)
                continue
            }

            val existingFile = existingFileMap[incoming.id]

            if (existingFile == null) {
                filesToAdd.add(incoming)
                continue
            }

            if (hasFileChanged(incoming, existingFile)) {
                filesToUpdate.add(incoming)
            }

        }

        return ProjectSnapshotDiff(toAdd = filesToAdd, toDeleteFiles = filesToDelete, toUpdate = filesToUpdate )

    }


    private fun hasFileChanged (incomingFile: ProjectFileSnapshot, existingFile: ProjectFile) : Boolean {
        if (incomingFile.path != existingFile.filePath) return true
        val incomingHash = sha256(incomingFile.content)
        return incomingHash != existingFile.contentHash
    }


}