
lazy val scala213 = "2.13.6"
lazy val scala212 = "2.12.15"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala211, scala212, scala213)

ThisBuild / version      := "0.0.3"
ThisBuild / organization := "com.github.salva"
ThisBuild / organizationHomepage := Some(url("https://github.com/salva"))
ThisBuild / scalaVersion := scala213
ThisBuild / description := "Manipulate file system glob patterns"
ThisBuild / licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/salva/scala-glob"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/salva/scala-glob"),
    "https://github.com/salva/scala-glob.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "salva",
    name = "Salvador Fandiño",
    email = "sfandino@yahoo.com",
    url = url("https://github.com/salva")
  )
)

lazy val root = (project in file("."))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, minor)) => {
          if (minor >= 12)
            "com.lihaoyi" %% "fastparse" % "2.3.3"
          else
            "com.lihaoyi" %% "fastparse" % "2.1.2"
        }
        case unsupportedVersion => throw new IllegalArgumentException("Bad Scala version $unsupportedVersion found")
      }
    },
    libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.10" % Test,
  )

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

