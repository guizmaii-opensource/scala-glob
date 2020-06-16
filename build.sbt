
version := "0.0.1"
scalaVersion := "2.11.12"

libraryDependencies += "com.lihaoyi" %% "fastparse" % "2.1.2"
libraryDependencies += "org.tpolecat" %% "atto-core" % "0.7.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test

organization := "com.github.salva"
organizationHomepage := Some(url("https://github.com/salva"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/salva/scala-glob"),
    "https://github.com/salva/scala-glob.git"
  )
)

developers := List (
  Developer(
    id = "salva",
    name = "Salvador Fandiño",
    email = "sfandino@yahoo.com",
    url = url("https://github.com/salva")
  )
)

description := "Manipulate file system glob patterns"
licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/salva/scala-glob"))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

