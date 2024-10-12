import scala.tools.nsc.classpath.VirtualDirectoryClassPath

import sbt._
import sbt.internal.DslEntry
import sbt.Keys._


name := "Sbt-Docker-Examples"

version := "1.0"

// the scala version in root will be 2.13.13 while that in domain will be 2.12.18

// to set the same version to all projects in this build, we need to do ThisBuild / ScalaVersion := "2.13.13"
val f = settingKey[Int]("")

//name in Global := "hello Global scope"

//name / Global := "hello Global scope"

// name in (Compile, packageBin) := "hello Compile scope packageBin"

//  name in Compile := "hello Compile scope"

//  name.in(Compile).:=("hello ugly syntax")

//      thisProject
// ThisScope

// Scope

// ScopeAxis

// GlobalScope
// ThisBuild
// BuildReference

ThisBuild / scalaVersion := "3.3.3"

//InThisBuild/ scalaVersion

lazy val global: Project = project
  .in(file("."))
  .aggregate(
    common,
    multi1,
    multi2
  )
  .settings(update / aggregate := false)

//update / aggregate is the aggregate key scoped to the update task
lazy val mytask = taskKey[Int]("hello")

mytask := {
  println("hello world")
  // println(global.base)
  // println(global.uses)
  println(global.project)
  // println(global.settings)

  // println(global.configurations)
//    println( global.dependencies)
//     println(app.plugins)

//     println(global.projectOrigin)

//     println(global.id)

//     println(global.referenced)

  println(multi2.dependencies)
  println(multi1.configuration)
  2

}
lazy val foo = settingKey[Int]("76yufugfh")
lazy val bar = settingKey[Int]("7uuu")

//   lazy val projX = (project in file("x"))
//     .settings(
//       foo := {
//         (Test / bar).value + 1
//       },
//       Compile / bar := 1,
//       Test/bar :=8
//     )

ThisBuild / Compile / bar := 1
//ThisBuild/Runtime/bar :=8
//IntegrationTest/ bar := 80
//ThisBuild/ bar:=800

//Test/bar will also be 8  since Test extends Runtime

lazy val common = project


lazy val multi1 = project.dependsOn(
  common
)

//This defines compile dependency between multi1 and common. Before we compile multi1,common must be successfully compiled first
// And when we work  within multi1, we can use classes from src/main/scala and resources from src/main/resources from common. Buf if we wanted to reuse in multi1's tests some code from module common, it won't work. We need to make it work like so
// lazy val multi1 = project.dependsOn(
//   common%"compile->compile; test->test"
// )

//A project may depend on code in another project
//Now code in multi2  can use classes from common
lazy val multi2 = project.dependsOn(
  common
)
//common % "compile->compile;test->test"
// core dependsOn(util) means that the compile configuration in core depends on the compile configuration in util.
// You could write this explicitly as dependsOn(util % "compile->compile").
// The -> in "compile->compile" means “depends on” so "test->compile" means the test configuration in core would depend on the compile configuration in util

lazy val buildInfo = taskKey[Seq[File]]("Generates basic build information")

buildInfo := {
  val f = sourceManaged.value

  val v = version.value

  val i = java.time.Instant.now()
  IO.write(
    f,
    s""" import java.time.Instant
         object BuildInfo{
            val version:String="$v"
            val time: Instant=Instant.ofEpochMilli(${i.toEpochMilli()}L)
         }

    """.stripMargin
  )
  // returns a Seq[File]
  f :: Nil
}

ThisBuild/semanticdbEnabled := true
//add the task to the list of source generators
//sourceGenerators in  Compile += buildInfo

//You can experiment on your machine by using a local repository:

//ThisBuild / pushRemoteCacheTo := Some(MavenCache("local-cache", file("/tmp/remote-cache")))
ThisBuild / pushRemoteCacheTo := Some(MavenCache("local-cache", file("/tmp/remote-cache")))

//compileIncremental := compileIncremental.dependsOn(pullRemoteCache).value
//products           := products.dependsOn(pullRemoteCache).value
//copyResources      := copyResources.dependsOn(pullRemoteCache).value

// incOptions := incOptions.value

//   .withIgnoredScalacOptions(
//     incOptions.value.ignoredScalacOptions ++ Array(
//       "-Xplugin:.*",
//       "-Ybackend-parallelism [\\d]+"
//     )
//   ).withApiDebug(true)

// publishArtifact in Test := false

// pushRemoteCache := {}
// pullRemoteCache := {}
// Compilation cache setup
//ThisBuild / pushRemoteCacheTo := Some(MavenCache("compilation-cache", (ThisBuild / baseDirectory).value / "compilation-cache"))
// ThisBuild / version := sys.env.getOrElse("VERSION", "LOCAL")
ThisBuild / pushRemoteCacheTo := Some(
  MavenCache("local-cache", (ThisBuild / baseDirectory).value / "local-cache-tmp")
)

// pushRemoteCacheTo := Some(
//   MavenCache("local-cache", (ThisBuild / baseDirectory).value / "remote-cache")
// )

// remoteCacheId := "fixed-id"

// remoteCacheIdCandidates := Seq(remoteCacheId.value)

// Save build artifacts to a cache that isn't shadowed by docker.
// https://www.scala-sbt.org/1.x/docs/Remote-Caching.html
// During the build step, we push build artifacts to the "build-cache" dir,
// which is saved in the image file by the deploy process.
// On later loads, we pull assets from that cache and incrementally compile,
// any changes, plus the dynamically generated code (autovalue and routes).

//   Global / pushRemoteCacheTo := Some(
//     MavenCache("local-cache", file(baseDirectory.value + "/../build-cache"))
//   )
Compile / pushRemoteCacheConfiguration := (Compile / pushRemoteCacheConfiguration)
  .value
  .withOverwrite(true)
Test / pushRemoteCacheConfiguration := (Test / pushRemoteCacheConfiguration)
  .value
  .withOverwrite(true)

//   // Load the "remote" cache on startup.
//   Global / onLoad := {
//     //val previous = (Global / onLoad).value
//     // compose the new transition on top of the existing one
//     // in case your plugins are using this hook.
//     //startupTransition compose previous
//   }

// Compile / remoteCacheId := "fixed-id"
// Compile / remoteCacheIdCandidates := Seq((Compile / remoteCacheId).value)
// Test / remoteCacheId := "fixed-id"
// Test / remoteCacheIdCandidates := Seq((Test / remoteCacheId).value)
// Compile / pushRemoteCacheConfiguration := (Compile / pushRemoteCacheConfiguration).value.withOverwrite(true)
// Test / pushRemoteCacheConfiguration := (Test / pushRemoteCacheConfiguration).value.withOverwrite(true)

// lazy val remoteCacheSettings = Seq(
//   pushRemoteCacheTo := Some(
//     MavenCache(
//       "local-cache",
//       (ThisBuild / baseDirectory).value / "target" / "remote-cache"
//     )
//   )
// )

lazy val hello = project
  .in(file("."))
  /// .aggregate(helloCore)
  // .dependsOn(helloCore)
  .enablePlugins(JavaAppPackaging, GraalVMNativeImagePlugin, DockerPlugin)
  .settings(
    name       := "Hello",
    maintainer := "A Scala Dev!"
  )

//   ThisBuild / credentials += Credentials(
//     "Sonatype Nexus Repository Manager",
//     "nexus..cc",
//     sys.env("NEXUS_USER"),
//     sys.env("NEXUS_PW")
//   )
val publishToNexus =
  settingKey[Option[Resolver]]("Nexus repository resolver")

ThisBuild / publishToNexus := {
  val nexus = "https://nexus.somename/repository"
  if (isSnapshot.value)
    Some("Nexus Realm".at(s"$nexus/maven-snapshots"))
  else
    Some("Nexus Realm".at(s"$nexus/maven-releases"))
}
//ThisBuild / scalafmtOnCompile := true
//To speed up compilation you can disable documentation generation:
// Run tests in a separate JVM to prevent resource leaks.
ThisBuild / Test / fork := true

Compile / doc / sources                := Seq.empty
Compile / packageDoc / publishArtifact := false

Test / parallelExecution := true
Test / fork              := false

publish / skip := true

//sbtPlugin := true

// ThisBuild / developers := List(
//   Developer(
//     id    = "",
//     name  = "",
//     email = "",
//     url   = url("")
//   ),
// )

ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots".at(nexus + "content/repositories/snapshots"))
  else Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
}

lazy val app = project
  .in(file("./app"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    // for an application with a main method
    scalaJSUseMainModuleInitializer := true
  )

//To deploy build outputs to a repository with the publish task, user credentials can be declared in the build.sbt file:

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "nexus.scala-tools.org",
  "admin",
  "admin123"
)

// publishTo := Some("Artifactory Realm" at "http://<host>:<port>/artifactory/<repo-key>")
// credentials += Credentials("Artifactory Realm", "<host>", "<USERNAME>", "<PASS>")

//sbt can search your local Maven repository if you add it as a repository:
resolvers += (
  "Local Maven Repository".at("file://" + Path.userHome.absolutePath + "/.m2/repository")
)
//Resolver.defaults

lazy val integration = (project in file("integration"))
  // .dependsOn(core) // your current subproject
  .settings(
    publish / skip := true
    // test dependencies
    // libraryDependencies += something % Test,
  )



// val docker = Seq(
//   Universal / javaOptions += "-Djdk.httpclient.allowRestrictedHeaders=cache-control,accept,accept-encoding,accept-language,connection,content-type,content-length,expect,host,referer",
//   dockerEnvVars ++= Map("VERSION" -> version.value),
//   packageName        := moduleName.value,
//   version            := version.value,
//   maintainer         := "example@example.com",
//   dockerUsername     := sys.env.get("DOCKER_USERNAME"),
//   dockerRepository   := sys.env.get("DOCKER_REPO_URI"),
//   dockerBaseImage    := "amazoncorretto:22.0.0-alpine",
//   dockerUpdateLatest := true,
//   dockerCommands := {
//     val commands         = dockerCommands.value
//     val (stage0, stage1) = commands.span(_ != DockerStageBreak)
//     val (before, after)  = stage1.splitAt(4)
//     val installBash      = Cmd("RUN", "apk update && apk upgrade && apk add bash && apk add curl")
//     stage0 ++ before ++ List(installBash) ++ after
//   }
// )

Compile / compile / scalacOptions += "-deprecation"


//myTask depends on compile
lazy val myTask2 = taskKey[Unit]("A custom task that depends on compile")

myTask2 := {
  val _ =(Compile / compile ).value // This ensures that `compile` runs before `myTask`
  println("Compilation completed. Now executing myTask.")
}
