package com.example.health

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.config.DatabaseConfig
import com.example.security.OktaAuthentication
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import slick.jdbc.OracleProfile.api._

case class HealthResponse(status: String, components: Map[String, String])
object HealthResponse {
  implicit val healthResponseFormat: RootJsonFormat[HealthResponse] =
    jsonFormat2(HealthResponse.apply)
}

class HealthCheck(dbConfig: DatabaseConfig, oktaAuth: OktaAuthentication) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val db = Database.forURL(
    url = dbConfig.url,
    user = dbConfig.username,
    password = dbConfig.password,
    driver = "oracle.jdbc.OracleDriver",
    executor = AsyncExecutor(
      name = "oracle-executor",
      minThreads = dbConfig.minConnections,
      maxThreads = dbConfig.maxConnections,
      queueSize = 1000,
      maxConnections = dbConfig.maxConnections
    )
  )

  def routes(implicit ec: ExecutionContext): Route = {
    path("health") {
      get {
        onSuccess(checkHealth()) { response =>
          complete(response)
        }
      }
    }
  }

  private def checkHealth()(implicit ec: ExecutionContext) = {
    val dbHealth = checkDatabase()
    val oktaHealth = checkOkta()

    for {
      dbStatus <- dbHealth
      oktaStatus <- oktaHealth
    } yield {
      val overallStatus = if (dbStatus && oktaStatus) "UP" else "DOWN"
      val response = HealthResponse(
        status = overallStatus,
        components = Map(
          "database" -> (if (dbStatus) "UP" else "DOWN"),
          "okta" -> (if (oktaStatus) "UP" else "DOWN")
        )
      )
      logger.info(s"Health check completed: $response")
      response
    }
  }

  private def checkDatabase()(implicit
      ec: ExecutionContext
  ): Future[Boolean] = {
    val checkQuery = sql"SELECT 1 FROM DUAL".as[Int]
    db.run(checkQuery)
      .map { result =>
        result.nonEmpty && result.head == 1
      }
      .recover { case e: Exception =>
        logger.error("Database health check failed", e)
        false
      }
  }

  private def checkOkta()(implicit ec: ExecutionContext): Future[Boolean] = {
    oktaAuth
      .validateToken("dummy-token")
      .map(_ => true)
      .recover { case e: Exception =>
        logger.error("Okta health check failed", e)
        false
      }
  }
}
