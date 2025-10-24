package com.example.financemanager.model

import java.util.*

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val createdAt: Date = Date(),
    val lastLogin: Date = Date()
)