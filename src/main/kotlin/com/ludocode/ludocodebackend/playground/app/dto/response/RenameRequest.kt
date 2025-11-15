package com.ludocode.ludocodebackend.playground.app.dto.response

import java.util.UUID

data class RenameRequest (val targetId: UUID, val newName: String)