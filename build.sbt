name := """scannerClient"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

resolvers += "ruimo.com" at "http://static.ruimo.com/release"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9"

libraryDependencies ++= Seq(
  // Logging
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  // iotf
  "com.ruimo" %% "iotf" % "1.0-SNAPSHOT",
  // Joda time
  "joda-time" % "joda-time" % "2.7",
  // Ruimo scoins
  "com.ruimo" %% "scoins" % "1.0-SNAPSHOT",
  // JSON
  "io.spray"  %%  "spray-json" % "1.3.1",
  "org.fusesource.mqtt-client" % "mqtt-client" % "1.10"
)

enablePlugins(JavaAppPackaging)

enablePlugins(UniversalPlugin)

fork in run := true

javaOptions in run += "-DMqttAuthToken=" + System.getProperty("MqttAuthToken")
