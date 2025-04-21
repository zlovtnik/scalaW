package com.example.graphql

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.config.DatabaseConfig
import com.example.security.OktaAuthentication
import com.example.repositories.{UserRepository, ETLJobRepository}
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.parser.QueryParser
import spray.json.DefaultJsonProtocol._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.marshalling.sprayJson._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class GraphQLServer(
    dbConfig: DatabaseConfig,
    oktaAuth: OktaAuthentication,
    userRepository: UserRepository,
    etlJobRepository: ETLJobRepository
) {
  private val logger = LoggerFactory.getLogger(getClass)
  implicit val stringFormat: JsonFormat[String] = StringJsonFormat

  def routes(implicit ec: ExecutionContext): Route = {
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson =>
        val query = requestJson.asJsObject.fields
          .get("query")
          .map(_.convertTo[String])
          .getOrElse("")
        val operationName = requestJson.asJsObject.fields
          .get("operationName")
          .map(_.convertTo[String])
        val variables =
          requestJson.asJsObject.fields.getOrElse("variables", JsObject.empty)

        logger.debug(s"Received GraphQL query: $query")
        logger.debug(s"Operation name: $operationName")
        logger.debug(s"Variables: $variables")

        QueryParser.parse(query) match {
          case Success(queryAst) =>
            logger.debug("Query parsed successfully")
            complete(executeGraphQLQuery(queryAst, operationName, variables))
          case Failure(error) =>
            logger.error(s"Query parsing failed: ${error.getMessage}")
            complete(
              JsObject(
                "errors" -> JsArray(
                  JsObject("message" -> JsString(error.getMessage))
                )
              )
            )
        }
      }
    } ~
      (get & path("graphql")) {
        logger.debug("Serving GraphiQL interface")
        getFromResource("graphiql.html")
      }
  }

  private def executeGraphQLQuery(
      query: Document,
      operationName: Option[String],
      variables: JsValue
  )(implicit ec: ExecutionContext) = {
    import sangria.marshalling.sprayJson._

    val context =
      new GraphQLContext(dbConfig, oktaAuth, userRepository, etlJobRepository)

    logger.debug(s"Executing GraphQL query: ${query.source.get}")
    logger.debug(s"Operation name: $operationName")
    logger.debug(s"Variables: $variables")

    Executor
      .execute(
        schema = SchemaDefinition.schema,
        queryAst = query,
        userContext = context,
        variables = variables.asJsObject,
        operationName = operationName
      )
      .recover {
        case error: QueryAnalysisError =>
          logger.error(s"Query analysis error: ${error.getMessage}")
          JsObject(
            "errors" -> JsArray(
              JsObject("message" -> JsString(error.getMessage))
            )
          )
        case error: ErrorWithResolver =>
          logger.error(s"Execution error: ${error.getMessage}")
          JsObject(
            "errors" -> JsArray(
              JsObject("message" -> JsString(error.getMessage))
            )
          )
      }
  }
}
