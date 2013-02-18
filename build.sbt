name := "shoehorn"

version := "1.0-SNAPSHOT"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "Sonatype"    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "JBoss"       at "http://repository.jboss.org/nexus/content/groups/public/",
  "Akka"        at "http://repo.akka.io/releases/",
  "GuiceyFruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
)

libraryDependencies ++= Seq(
  "com.github.jdegoes" % "blueeyes-core_2.9.1" % "0.6.1-SNAPSHOT",
  "com.github.jdegoes" % "blueeyes-json_2.9.1" % "0.6.1-SNAPSHOT",
  "com.github.jdegoes" % "blueeyes-mongo_2.9.1" % "0.6.1-SNAPSHOT",
  "ch.qos.logback" % "logback-classic" % "1.0.6"
)
