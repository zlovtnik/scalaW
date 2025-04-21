package com.example.config

import com.typesafe.config.Config

case class OktaConfig(
  orgUrl: String,
  clientId: String,
  clientSecret: String
)

case class AppConfig(
  serverPort: Int,
  oktaConfig: OktaConfig
)

object AppConfig {
  def apply(config: Config): AppConfig = {
    AppConfig(
      serverPort = config.getInt("akka.http.server.port"),
      oktaConfig = OktaConfig(
        orgUrl = config.getString("okta.org-url"),
        clientId = config.getString("okta.client-id"),
        clientSecret = config.getString("okta.client-secret")
      )
    )
  }
} 