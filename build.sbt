Global / onChangedBuildSource := ReloadOnSourceChanges

val scala212               = "2.12.15"
val scala213               = "2.13.6"
val scala3                 = "3.3.6"
val supportedScalaVersions = List(scala212, scala213, scala3)

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

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val `scala-blob` =
  project
    .in(file("."))
    .settings(
      name               := "scala-blob",
      crossScalaVersions := supportedScalaVersions
    )
    .settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest-funsuite" % "3.2.10" % Test
      ) ++ PartialFunction
        .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
          case Some((3, _))                    =>
            Seq("com.lihaoyi" %% "fastparse" % "3.1.0")
          case Some((2, minor)) if minor >= 12 =>
            Seq("com.lihaoyi" %% "fastparse" % "2.3.3")
          case unsupportedVersion              => throw new IllegalArgumentException(s"Bad Scala version $unsupportedVersion found")
        }
        .toList
        .flatten
    )
