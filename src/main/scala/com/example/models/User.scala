package com.example.models

import java.time.LocalDateTime

case class User(
    id: String,
    username: String,
    email: String,
    firstName: String,
    lastName: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
)
