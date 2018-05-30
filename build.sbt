val ScalatraVersion = "2.6.3"

organization := "com.example"

name := "Appointment System"

version := "0.1.0"

scalaVersion := "2.12.4"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.8.v20171121" % "container",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "org.scalatra" %% "scalatra-json" % "2.6.3",
  "org.json4s"   %% "json4s-jackson" % "3.5.3",
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "com.mchange" % "c3p0" % "0.9.5.2",
  "com.mchange" % "mchange-commons-java" % "0.2.15",
  "org.scalatra" %% "scalatra-auth" % "2.6.3",
  "com.github.t3hnar" %% "scala-bcrypt" % "3.1",
  "com.h2database" % "h2" % "1.4.197",
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "net.databinder.dispatch" %% "dispatch-core" % "0.13.1",
)

javaOptions ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)
