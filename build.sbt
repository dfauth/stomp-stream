name := "stomp-stream"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.6"

val kafkaVersion = "2.1.1"
val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.10"
val springVersion = "5.1.8.RELEASE"

val scalactic = "org.scalactic" %% "scalactic" % "3.0.5"
val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
val akkaHttp = "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
val kafkaCore = "org.apache.kafka" %% "kafka" % kafkaVersion
val akkaStreamsKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "0.14"
val kafkaStreams = "org.apache.kafka" % "kafka-streams" % kafkaVersion withSources()
val kafkaClient = "org.apache.kafka" % "kafka-clients" % kafkaVersion withSources()
val embeddedKafka = "io.github.embeddedkafka" %% "embedded-kafka" % "2.1.1" % Test withSources()
val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
val play = "com.typesafe.play" %% "play-json" % "2.6.10"
val springMessaging = "org.springframework" % "spring-messaging" % springVersion
val springWebsocket = "org.springframework" % "spring-websocket" % springVersion
val javaxWebsocket = "javax.websocket" % "javax.websocket-api" % "1.1"
val testNG = "org.testng" % "testng" % "6.14.3" % Test
val tyrus = "org.glassfish.tyrus.bundles" % "tyrus-standalone-client-jdk" % "1.12" % Test
val webJars = Seq("org.webjars" % "webjars-locator-core" % "latest.release",
                  "org.webjars" % "sockjs-client" % "1.0.2",
                  "org.webjars" % "stomp-websocket" % "2.3.3",
                  "org.webjars" % "bootstrap" % "3.3.7",
                  "org.webjars" % "jquery" % "3.1.0")


val commonScalaDeps = Seq(scalactic, scalatest, akkaHttpSprayJson, scalaLogging, logback)


lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= commonScalaDeps ++ Seq(
      akkaHttp,
      akkaStream,
      kafkaCore,
      akkaStreamsKafka,
      kafkaStreams,
      kafkaClient,
      embeddedKafka,
      play,
      springMessaging,
      springWebsocket,
      javaxWebsocket,
      testNG,
      tyrus,
      akkaHttpTestKit) ++ webJars
  )
  .enablePlugins(AssemblyPlugin)


