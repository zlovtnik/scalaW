package com.example.security

import com.example.config.OktaConfig
import com.okta.authn.sdk.client.AuthenticationClient
import com.okta.authn.sdk.client.AuthenticationClients
import com.okta.authn.sdk.resource.{
  AuthenticationResponse,
  AuthenticationStatus
}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class OktaAuthentication(config: OktaConfig) {
  private val client: AuthenticationClient = AuthenticationClients
    .builder()
    .setOrgUrl(config.orgUrl)
    .build()

  def authenticate(
      token: String
  )(implicit ec: ExecutionContext): Future[AuthenticationResponse] = {
    Future {
      client.authenticate(token, null, null, null)
    }
  }

  def validateToken(
      token: String
  )(implicit ec: ExecutionContext): Future[Boolean] = {
    authenticate(token).map { response =>
      response.getStatus == AuthenticationStatus.SUCCESS
    }
  }
}
