package com.ludocode.ludocodebackend.playground.app.dto.request

import java.util.UUID

data class RenameRequest (val targetId: UUID, val newName: String)