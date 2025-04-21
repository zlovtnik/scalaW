/** Aplicação principal que configura e inicia o servidor GraphQL com integração
  * Oracle.
  *
  * Este objeto é responsável por:
  *   - Configurar o sistema Akka
  *   - Inicializar o contexto Apache Camel para operações ETL
  *   - Configurar autenticação Okta
  *   - Inicializar repositórios de banco de dados
  *   - Configurar e iniciar o servidor GraphQL
  *   - Configurar verificações de saúde
  *
  * @author
  *   Rafael Cardoso
  * @version 1.0
  */
package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.example.config.{AppConfig, DatabaseConfig, OktaConfig}
import com.example.etl.ETLProcessor
import com.example.graphql.{GraphQLContext, GraphQLServer}
import com.example.health.HealthCheck
import com.example.repositories.{UserRepository, ETLJobRepository}
import com.example.security.OktaAuthentication
import com.typesafe.config.ConfigFactory
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.spi.CamelEvent
import org.apache.camel.support.EventNotifierSupport
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._

/** Objeto principal que inicia a aplicação.
  *
  * Este objeto estende App e é o ponto de entrada da aplicação. Ele configura
  * todos os componentes necessários e inicia o servidor HTTP.
  */
object Main extends App {
  private val logger = LoggerFactory.getLogger(getClass)

  logger.info("Starting Scala GraphQL Oracle application...")

  implicit val system: ActorSystem = ActorSystem("scala-graphql-oracle")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val config = ConfigFactory.load()
  logger.info("Configuration loaded successfully")

  val appConfig = AppConfig(config)
  val dbConfig = DatabaseConfig(config)
  val oktaConfig = OktaConfig(
    orgUrl = config.getString("okta.org-url"),
    clientId = config.getString("okta.client-id"),
    clientSecret = config.getString("okta.client-secret")
  )
  logger.info("Configuration objects created successfully")

  val oktaAuth = new OktaAuthentication(oktaConfig)
  val userRepository = new UserRepository(dbConfig)
  val etlJobRepository = new ETLJobRepository(dbConfig)

  logger.info("Initializing Apache Camel context...")
  val camelContext = new DefaultCamelContext()

  /** Configura e inicia o contexto Apache Camel para operações ETL.
    *
    * @param camelContext
    *   O contexto Camel a ser configurado
    */
  private def configureCamelContext(camelContext: DefaultCamelContext): Unit = {
    val eventNotifier = new EventNotifierSupport {
      override def notify(event: CamelEvent): Unit = {
        event match {
          case e: CamelEvent.CamelContextStartedEvent =>
            logger.info("Camel context started")
          case e: CamelEvent.CamelContextStoppingEvent =>
            logger.info("Camel context stopping")
          case e: CamelEvent.CamelContextStoppedEvent =>
            logger.info("Camel context stopped")
          case e: CamelEvent.RouteStartedEvent =>
            logger.info(s"Route started: ${e.getRoute.getRouteId}")
          case e: CamelEvent.RouteStoppedEvent =>
            logger.info(s"Route stopped: ${e.getRoute.getRouteId}")
          case _ => // Ignore other events
        }
      }

      override def isEnabled(event: CamelEvent): Boolean = true
    }
    camelContext.getManagementStrategy.addEventNotifier(eventNotifier)
  }

  val etlProcessor = new ETLProcessor(camelContext, dbConfig)

  try {
    logger.info("Starting Apache Camel context...")
    camelContext.start()
    logger.info("Apache Camel context started successfully")
  } catch {
    case e: Exception =>
      logger.error("Failed to start Apache Camel context", e)
      throw e
  }

  logger.info("Services initialized successfully")

  val graphQLContext = new GraphQLContext(
    dbConfig,
    oktaAuth,
    userRepository,
    etlJobRepository
  )

  val graphQLServer = new GraphQLServer(
    dbConfig,
    oktaAuth,
    userRepository,
    etlJobRepository
  )

  val healthCheck = new HealthCheck(dbConfig, oktaAuth)

  val routes = concat(
    graphQLServer.routes,
    healthCheck.routes
  )

  logger.info(s"Starting server on port ${appConfig.serverPort}...")
  val bindingFuture = Http().bindAndHandle(
    routes,
    "localhost",
    appConfig.serverPort
  )

  bindingFuture.onComplete {
    case Success(binding) =>
      logger.info(s"Server online at http://localhost:${appConfig.serverPort}/")
      logger.info("Health check available at /health")
      logger.info("GraphQL endpoint available at /graphql")
      logger.info("GraphiQL interface available at /graphql (GET)")
    case Failure(ex) =>
      logger.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }

  /** Configura os hooks de desligamento para garantir um encerramento limpo da
    * aplicação.
    *
    * Este método é chamado quando a aplicação está sendo encerrada e garante
    * que todos os recursos sejam liberados adequadamente.
    */
  private def setupShutdownHooks(): Unit = {
    sys.addShutdownHook {
      logger.info("Shutting down...")
      try {
        logger.info("Stopping Apache Camel context...")
        camelContext.stop()
        logger.info("Apache Camel context stopped successfully")
      } catch {
        case e: Exception =>
          logger.error("Error stopping Apache Camel context", e)
      }
      system.terminate()
    }
  }

  setupShutdownHooks()
}
