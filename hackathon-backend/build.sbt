name := "hackathon-backend"
version := "0.40"
scalaVersion := "2.12.10"

enablePlugins(SbtNativePackager)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerBaseImage := "java:openjdk-8"
daemonUser := "root"
dockerRepository := None
dockerExposedPorts := Seq(8080)

val commonSettings = Seq(
  scalaVersion := "2.12.10",
  resolvers ++= Dependencies.additionalResolvers,
  scalacOptions ++= CompilerOpts.all,
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  sources in (Compile, doc) := Seq.empty,
//  scalafmtTestOnCompile in ThisBuild := true,
  scalafmtOnCompile in ThisBuild := true,
  parallelExecution in Test := false
)

val protoSettings = Seq(
  PB.targets in Compile := Seq(
    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
  )
)

lazy val `hackathon-backend` =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(protoSettings)
    .settings(libraryDependencies ++= Dependencies.all)
    .settings(
      dockerBaseImage := "java:openjdk-8",
      daemonUser in Docker := "root",
      dockerRepository := Some("docker.codeheroes.io"),
      dockerExposedPorts := Seq(8080)
    )

PB.protoSources in Compile := Seq(
  "ner-service"
).map(baseDirectory.value / ".." / "ner-service" / "bitehack-proto" / _)
