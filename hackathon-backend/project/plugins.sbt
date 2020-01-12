logLevel := Level.Warn

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")
addSbtPlugin("io.get-coursier"  % "sbt-coursier"        % "1.0.3")
addSbtPlugin("com.thesamet"     % "sbt-protoc"          % "0.99.19")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.3.0")




libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.4"
