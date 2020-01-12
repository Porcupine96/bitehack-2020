import DependencyVersions._
import sbt._

//noinspection SpellCheckingInspection
object Dependencies {

  private val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor"             % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"             % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence"       % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"            % akkaVersion,
    "com.typesafe.akka" %% "akka-remote"            % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster"           % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools"     % akkaVersion
  )

  private val akkaHttpDependencies = Seq(
    "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % akkaHttpJson4sVersion,
    "org.json4s"        %% "json4s-native"    % json4sVersion
  )

  private val akkaPersistenceDependencies = Seq(
    "com.github.dnvriend" %% "akka-persistence-inmemory"  % inMemJournalVersion,
    "com.typesafe.akka"   %% "akka-persistence-cassandra" % akkaCassandraVersion,
    "com.github.dnvriend" %% "akka-persistence-jdbc"      % akkaPersistenceJdbcVersion,
  )

  private val loggingDependencies = Seq(
    "ch.qos.logback"             % "logback-classic"          % logbackVersion,
    "ch.qos.logback"             % "logback-core"             % logbackVersion,
    "com.typesafe.scala-logging" %% "scala-logging"           % scalaLoggingVersion,
    "net.logstash.logback"       % "logstash-logback-encoder" % logbackLogstashVersion
  )

  private val catsDependencies = Seq(
    "org.typelevel" %% "cats-core"   % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.typelevel" %% "mouse"       % "0.20"
  )

  private val persistenceDependencies = Seq(
    "com.typesafe.slick" %% "slick"          % slickVersion,
    "org.postgresql"     % "postgresql"      % postgresqlVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickHikariCpVersion
  )

  private val grpcDependencies = Seq(
    "io.grpc"              % "grpc-netty"            % grpcNettyVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % grpcRuntimeVersion
  )

  private val commonDependencies = Seq(
    "io.codeheroes" %% "codeheroes-commons" % codeheroesCommonsVersion
  )

  private val chatDependencies = Seq(
    "io.codeheroes" %% "herochat" % heroChatVersion,
  )

  private val enumeratumDependencies = Seq(
    "com.beachape" %% "enumeratum"        % enumeratumVersion,
    "com.beachape" %% "enumeratum-json4s" % enumeratumVersion
  )

  private val corsDependencies = Seq(
    "ch.megard" %% "akka-http-cors" % corsVersion
  )

  private val jwtDependencies = Seq(
    "com.pauldijou" %% "jwt-json4s-native" % jwtVersion
  )

  private val courierDependenices = Seq(
    "ch.lightshed" %% "courier" % courierVersion
  )

  val all: Seq[ModuleID] =
    akkaPersistenceDependencies ++
      akkaHttpDependencies ++
      loggingDependencies ++
      akkaDependencies ++
      catsDependencies ++
      grpcDependencies ++
      commonDependencies ++
      persistenceDependencies ++
      corsDependencies ++
      jwtDependencies ++
      chatDependencies ++
      enumeratumDependencies ++
      courierDependenices

  val additionalResolvers: Seq[Resolver] =
    Seq(
      Resolver.jcenterRepo,
      Resolver.mavenCentral,
      Resolver.bintrayRepo("codeheroes", "maven"),
      "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype Repository" at "https://oss.sonatype.org/content/groups/public"
    )

}
