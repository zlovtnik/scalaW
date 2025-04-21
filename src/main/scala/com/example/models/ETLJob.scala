package com.example.models

import java.time.LocalDateTime

case class ETLJob(
    id: String,
    name: String,
    status: String,
    startTime: LocalDateTime,
    endTime: Option[LocalDateTime],
    recordsProcessed: Long,
    errorCount: Long
)
