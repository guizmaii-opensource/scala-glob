lazy val scala213               = "2.13.6"
lazy val scala212               = "2.12.15"
lazy val scala211               = "2.11.12"
lazy val supportedScalaVersions = List(scala211, scala212, scala213)

val GraalVM11 = "graalvm-ce-java11@20.3.0"

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

//---  Github Workflow CI Settings Start ---
ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)
ThisBuild / versionScheme              := Some("early-semver")

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("check", "test"))
)

//sbt-ci-release settings
ThisBuild / githubWorkflowPublishPreamble       := Seq(
  WorkflowStep.Use(UseRef.Public("olafurpg", "setup-gpg", "v3"))
)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.StartsWith(Ref.Branch("master")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublish               := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List(
  "PGP_PASSPHRASE",
  "PGP_SECRET",
  "SONATYPE_PASSWORD",
  "SONATYPE_USERNAME"
).map(envKey => envKey -> s"$${{ secrets.$envKey }}").toMap

//---  Github Workflow CI Settings End ---

lazy val `scala-blob` = project
  .in(file("."))
  .settings(
    name               := "scala-blob",
    crossScalaVersions := supportedScalaVersions,
    publishMavenStyle  := true,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-Xsource:3",
      "-unchecked",
      "-deprecation",
      "-feature",
//      "-Xfatal-warnings",
      "-language:higherKinds",
      "-language:postfixOps"
    ) ++ PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, n)) if n < 13  => Seq("-Xlint", "-Ypartial-unification")
        case Some((2, n)) if n >= 13 => Seq("-Xlint")
      }
      .toList
      .flatten
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest-funsuite" % "3.2.10" % Test
    ) ++ PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, minor)) if minor >= 12 =>
          Seq(
            "com.lihaoyi" %% "fastparse" % "2.3.3"
          )
        case Some((2, minor)) if minor < 12  =>
          Seq(
            "com.lihaoyi" %% "fastparse" % "2.1.2"
          )
        case unsupportedVersion              => throw new IllegalArgumentException(s"Bad Scala version $unsupportedVersion found")
      }
      .toList
      .flatten
  )

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots".at(nexus + "content/repositories/snapshots"))
  else Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
}

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
