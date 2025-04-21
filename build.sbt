ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

val akkaVersion = "2.8.5"
val akkaHttpVersion = "10.5.3"
val sangriaVersion = "4.0.2"
val oktaVersion = "3.0.0"
val oracleVersion = "21.8.0.0"
val camelVersion = "3.20.5"
val circeVersion = "0.14.6"
val slickVersion = "3.4.1"

lazy val root = (project in file("."))
  .settings(
    name := "scala-graphql-oracle",
    resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= Seq(
      // Akka
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

      // GraphQL
      "org.sangria-graphql" %% "sangria" % sangriaVersion,
      "org.sangria-graphql" %% "sangria-spray-json" % "1.0.3",

      // JSON
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",

      // Okta
      "com.okta.authn.sdk" % "okta-authn-sdk-api" % oktaVersion,
      "com.okta.authn.sdk" % "okta-authn-sdk-impl" % oktaVersion,
      "com.okta.sdk" % "okta-sdk-httpclient" % "8.2.5",

      // Oracle
      "com.oracle.database.jdbc" % "ojdbc8" % oracleVersion,

      // Slick
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.typesafe.slick" %% "slick-codegen" % slickVersion,

      // Apache Camel
      "org.apache.camel" % "camel-core" % camelVersion,
      "org.apache.camel" % "camel-jdbc" % camelVersion,

      // Config
      "com.typesafe" % "config" % "1.4.3",

      // Logging
      "ch.qos.logback" % "logback-classic" % "1.4.14",

      // Testing
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    )
  )

// Add scalafmt plugin
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
