package com.example.repositories

import com.example.config.DatabaseConfig
import com.example.models.User
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime
import slick.jdbc.OracleProfile.api._

class UserRepository(dbConfig: DatabaseConfig)(implicit ec: ExecutionContext) {
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

  private class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[String]("ID", O.PrimaryKey)
    def username = column[String]("USERNAME")
    def email = column[String]("EMAIL")
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")
    def createdAt = column[LocalDateTime]("CREATED_AT")
    def updatedAt = column[LocalDateTime]("UPDATED_AT")

    def * = (
      id,
      username,
      email,
      firstName,
      lastName,
      createdAt,
      updatedAt
    ) <> (User.tupled, User.unapply)
  }

  private val users = TableQuery[Users]

  def findById(id: String): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  def findByUsername(username: String): Future[Option[User]] = {
    db.run(users.filter(_.username === username).result.headOption)
  }

  def create(user: User): Future[User] = {
    val now = LocalDateTime.now()
    val userWithTimestamps = user.copy(createdAt = now, updatedAt = now)
    db.run(users += userWithTimestamps).map(_ => userWithTimestamps)
  }

  def update(user: User): Future[User] = {
    val updatedUser = user.copy(updatedAt = LocalDateTime.now())
    db.run(users.filter(_.id === user.id).update(updatedUser))
      .map(_ => updatedUser)
  }

  def delete(id: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).delete).map(_ > 0)
  }
}
