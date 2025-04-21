package com.example.graphql

import com.example.config.DatabaseConfig
import com.example.security.OktaAuthentication
import com.example.repositories.{UserRepository, ETLJobRepository}
import scala.concurrent.ExecutionContext

class GraphQLContext(
    val dbConfig: DatabaseConfig,
    val oktaAuth: OktaAuthentication,
    val userRepository: UserRepository,
    val etlJobRepository: ETLJobRepository
)(implicit val ec: ExecutionContext) {
  def validateToken(token: String)(implicit ec: ExecutionContext) = {
    oktaAuth.validateToken(token)
  }
}
