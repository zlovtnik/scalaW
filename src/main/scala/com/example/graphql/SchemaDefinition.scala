package com.example.graphql

import sangria.schema._
import sangria.marshalling.sprayJson._
import spray.json._
import com.example.models.{User, ETLJob}
import java.time.LocalDateTime
import sangria.validation.ValueCoercionViolation

object SchemaDefinition {
  case object LocalDateTimeCoercionViolation
      extends ValueCoercionViolation("LocalDateTime value expected")

  implicit val LocalDateTimeType: ScalarType[LocalDateTime] =
    ScalarType[LocalDateTime](
      "DateTime",
      coerceOutput = (dt, _) => JsString(dt.toString),
      coerceInput = {
        case sangria.ast.StringValue(dt, _, _, _, _) =>
          Right(LocalDateTime.parse(dt))
        case _ => Left(LocalDateTimeCoercionViolation)
      },
      coerceUserInput = {
        case s: String => Right(LocalDateTime.parse(s))
        case _         => Left(LocalDateTimeCoercionViolation)
      }
    )

  val UserType = ObjectType(
    "User",
    fields[GraphQLContext, User](
      Field("id", StringType, resolve = _.value.id),
      Field("username", StringType, resolve = _.value.username),
      Field("email", StringType, resolve = _.value.email),
      Field("firstName", StringType, resolve = _.value.firstName),
      Field("lastName", StringType, resolve = _.value.lastName),
      Field("createdAt", LocalDateTimeType, resolve = _.value.createdAt),
      Field("updatedAt", LocalDateTimeType, resolve = _.value.updatedAt)
    )
  )

  val ETLJobType = ObjectType(
    "ETLJob",
    fields[GraphQLContext, ETLJob](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("status", StringType, resolve = _.value.status),
      Field("startTime", LocalDateTimeType, resolve = _.value.startTime),
      Field(
        "endTime",
        OptionType(LocalDateTimeType),
        resolve = _.value.endTime
      ),
      Field("recordsProcessed", LongType, resolve = _.value.recordsProcessed),
      Field("errorCount", LongType, resolve = _.value.errorCount)
    )
  )

  val QueryType = ObjectType(
    "Query",
    fields[GraphQLContext, Unit](
      Field(
        "hello",
        StringType,
        description = Some("A simple greeting"),
        resolve = _ => "Hello, GraphQL!"
      ),
      Field(
        "user",
        OptionType(UserType),
        arguments = Argument("id", StringType) :: Nil,
        resolve = ctx => ctx.ctx.userRepository.findById(ctx.arg("id"))
      ),
      Field(
        "etlJob",
        OptionType(ETLJobType),
        arguments = Argument("id", StringType) :: Nil,
        resolve = ctx => ctx.ctx.etlJobRepository.findById(ctx.arg("id"))
      ),
      Field(
        "etlJobs",
        ListType(ETLJobType),
        resolve = ctx => ctx.ctx.etlJobRepository.findAll()
      )
    )
  )

  val MutationType = ObjectType(
    "Mutation",
    fields[GraphQLContext, Unit](
      Field(
        "createUser",
        UserType,
        arguments = Argument("username", StringType) ::
          Argument("email", StringType) ::
          Argument("firstName", StringType) ::
          Argument("lastName", StringType) :: Nil,
        resolve = ctx => {
          val now = LocalDateTime.now()
          val user = User(
            id = java.util.UUID.randomUUID().toString,
            username = ctx.arg("username"),
            email = ctx.arg("email"),
            firstName = ctx.arg("firstName"),
            lastName = ctx.arg("lastName"),
            createdAt = now,
            updatedAt = now
          )
          ctx.ctx.userRepository.create(user)
        }
      ),
      Field(
        "startETLJob",
        ETLJobType,
        arguments = Argument("name", StringType) :: Nil,
        resolve = ctx => {
          val now = LocalDateTime.now()
          val job = ETLJob(
            id = java.util.UUID.randomUUID().toString,
            name = ctx.arg("name"),
            status = "RUNNING",
            startTime = now,
            endTime = None,
            recordsProcessed = 0,
            errorCount = 0
          )
          ctx.ctx.etlJobRepository.create(job)
        }
      )
    )
  )

  val schema = Schema(QueryType, Some(MutationType))
}
