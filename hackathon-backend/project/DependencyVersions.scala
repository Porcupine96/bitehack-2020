import scalapb.compiler.Version

//noinspection SpellCheckingInspection
object DependencyVersions {
  val akkaVersion                = "2.5.19"
  val logbackVersion             = "1.2.3"
  val scalaLoggingVersion        = "3.9.2"
  val catsVersion                = "1.5.0"
  val catsEffectVersion          = "1.1.0"
  val inMemJournalVersion        = "2.5.1.1"
  val akkaCassandraVersion       = "0.80"
  val akkaPersistenceJdbcVersion = "3.4.0"
  val codeheroesCommonsVersion   = "0.34"
  val enumeratumVersion          = "1.5.13"
  val kafkaVersion               = "0.20"
  val akkaHttpVersion            = "10.1.7"
  val json4sVersion              = "3.5.3"
  val akkaHttpJson4sVersion      = "1.18.0"
  val logbackLogstashVersion     = "5.3"
  val slickVersion               = "3.2.0"
  val postgresqlVersion          = "42.2.5"
  val slickHikariCpVersion       = "3.2.0"
  val corsVersion                = "0.2.2"
  val jwtVersion                 = "0.14.1"
  val courierVersion             = "0.1.4"
  val heroChatVersion            = "0.81"

  val grpcNettyVersion: String   = Version.grpcJavaVersion
  val grpcRuntimeVersion: String = Version.scalapbVersion
}
