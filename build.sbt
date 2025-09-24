Global / onChangedBuildSource := ReloadOnSourceChanges

val scala213               = "2.13.16"
val scala3                 = "3.3.6"
val supportedScalaVersions = List(scala213, scala3)

ThisBuild / version              := "0.0.3"
ThisBuild / organization         := "com.github.salva"
ThisBuild / organizationHomepage := Some(url("https://github.com/salva"))
ThisBuild / scalaVersion         := scala213
ThisBuild / description          := "Manipulate file system glob patterns"
ThisBuild / licenses             := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage             := Some(url("https://github.com/salva/scala-glob"))

ThisBuild / scmInfo    := Some(
  ScmInfo(
    url("https://github.com/salva/scala-glob"),
    "https://github.com/salva/scala-glob.git",
  )
)
ThisBuild / developers := List(
  Developer(
    id = "salva",
    name = "Salvador Fandiño",
    email = "sfandino@yahoo.com",
    url = url("https://github.com/salva"),
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val `scala-blob` =
  project
    .in(file("."))
    .settings(
      name               := "scala-blob",
      crossScalaVersions := supportedScalaVersions,
      libraryDependencies ++= Seq(
        "com.lihaoyi"   %% "fastparse"          % "3.1.1",
        "org.scalatest" %% "scalatest-funsuite" % "3.2.19" % Test,
      ),
    )
