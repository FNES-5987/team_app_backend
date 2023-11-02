package com.example.app_backend.admin.user

import org.jetbrains.exposed.dao.id.LongIdTable

data class UserDTO(
    val nickname : String?,
    val birth: Int?,
    val gender :Int?,
    val bookmark: String?,
)