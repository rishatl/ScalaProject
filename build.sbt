val CatsVersion = "2.6.1"
val CirceVersion = "0.14.1"
val CirceGenericExVersion = "0.14.1"
val CirceConfigVersion = "0.8.0"
val DoobieVersion = "0.13.4"
val EnumeratumCirceVersion = "1.7.0"
val H2Version = "1.4.200"
val Http4sVersion = "0.21.28"
val KindProjectorVersion = "0.13.2"
val LogbackVersion = "1.2.6"
val Slf4jVersion = "1.7.30"
val ScalaCheckVersion = "1.15.4"
val ScalaTestVersion = "3.2.9"
val ScalaTestPlusVersion = "3.2.2.0"
val FlywayVersion = "7.15.0"
val TsecVersion = "0.2.1"
val PotgreSQLVersion = "42.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaProject",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % CatsVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-h2" % DoobieVersion,
      "org.tpolecat" %% "doobie-scalatest" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion,
      "io.circe" %% "circe-generic-extras" % CirceGenericExVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "io.circe" %% "circe-config" % CirceConfigVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.postgresql" % "postgresql" % PotgreSQLVersion,
      "org.flywaydb" % "flyway-core" % FlywayVersion,
      // Authentication dependencies
      "io.github.jmcardon" %% "tsec-common" % TsecVersion,
      "io.github.jmcardon" %% "tsec-password" % TsecVersion,
      "io.github.jmcardon" %% "tsec-mac" % TsecVersion,
      "io.github.jmcardon" %% "tsec-signatures" % TsecVersion,
      "io.github.jmcardon" %% "tsec-jwt-mac" % TsecVersion,
      "io.github.jmcardon" %% "tsec-jwt-sig" % TsecVersion,
      "io.github.jmcardon" %% "tsec-http4s" % TsecVersion,
    )
  )

dependencyOverrides += "org.slf4j" % "slf4j-api" % Slf4jVersion

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.typelevel" % "kind-projector" % KindProjectorVersion cross CrossVersion.full)