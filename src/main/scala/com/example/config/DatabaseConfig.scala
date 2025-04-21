package com.example.config

import com.typesafe.config.Config

case class DatabaseConfig(
  url: String,
  username: String,
  password: String,
  maxConnections: Int,
  minConnections: Int
)

object DatabaseConfig {
  def apply(config: Config): DatabaseConfig = {
    DatabaseConfig(
      url = config.getString("oracle.url"),
      username = config.getString("oracle.username"),
      password = config.getString("oracle.password"),
      maxConnections = config.getInt("oracle.pool.max-connections"),
      minConnections = config.getInt("oracle.pool.min-connections")
    )
  }
} 