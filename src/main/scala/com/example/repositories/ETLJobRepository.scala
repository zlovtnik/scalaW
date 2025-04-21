package com.example.repositories

import com.example.config.DatabaseConfig
import com.example.models.ETLJob
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime
import slick.jdbc.OracleProfile.api._

class ETLJobRepository(dbConfig: DatabaseConfig)(implicit
    ec: ExecutionContext
) {
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

  private class ETLJobs(tag: Tag) extends Table[ETLJob](tag, "ETL_JOBS") {
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def status = column[String]("STATUS")
    def startTime = column[LocalDateTime]("START_TIME")
    def endTime = column[Option[LocalDateTime]]("END_TIME")
    def recordsProcessed = column[Long]("RECORDS_PROCESSED")
    def errorCount = column[Long]("ERROR_COUNT")

    def * = (
      id,
      name,
      status,
      startTime,
      endTime,
      recordsProcessed,
      errorCount
    ) <> (ETLJob.tupled, ETLJob.unapply)
  }

  private val etlJobs = TableQuery[ETLJobs]

  def findById(id: String): Future[Option[ETLJob]] = {
    db.run(etlJobs.filter(_.id === id).result.headOption)
  }

  def findAll(): Future[Seq[ETLJob]] = {
    db.run(etlJobs.result)
  }

  def create(job: ETLJob): Future[ETLJob] = {
    db.run(etlJobs += job).map(_ => job)
  }

  def update(job: ETLJob): Future[ETLJob] = {
    db.run(etlJobs.filter(_.id === job.id).update(job)).map(_ => job)
  }

  def delete(id: String): Future[Boolean] = {
    db.run(etlJobs.filter(_.id === id).delete).map(_ > 0)
  }
}
