enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
//dockerBaseImage := "openjdk:11-jdk"
//maintainer in Docker := "Group"
Docker/maintainer :=""

// Docker/dockerAdditionalPermissions
// Docker/dockerAlias
Docker/dockerAlias := dockerAlias.value.withTag(dockerAlias.value.tag.map(_.replace("+", "_")))
// Docker/dockerAliases

// Docker/dockerApiVersion

Docker/dockerAutoremoveMultiStageIntermediateImages:= true

Docker/dockerBaseImage:= "openjdk:11-jdk"//openjdk:11-jre-slim-buster
//eclipse-temurin:21.0.4_7-jre-noble
//eclipse-temurin:21.0.4_7-jre
//eclipse-temurin:17.0.12_7-jre-noble

// Docker/ dockerBuildCommand

// Docker/dockerBuildEnvVars

Docker/dockerBuildInit := false

// Docker/dockerBuildOptions
Docker/ dockerBuildOptions ++= Seq("--network=host")
// Docker/dockerBuildkitEnabled

// Docker/dockerBuildxPlatforms
Docker/  dockerBuildxPlatforms := Seq("linux/amd64", "linux/arm64") // not used in case of Docker/publishLocal
// Docker/dockerChmodType

// Docker/dockerCmd

// Docker/ dockerCommands
// clear the existing docker commands
Docker/dockerCommands := Seq()



Docker/dockerEnvVars:= Map.empty
Docker/ dockerEnvVars                        := Map(
      "HTTP_PORT" -> "8080"
    )
// Docker/dockerExecCommand

// Docker/dockerExposedPorts
Docker/dockerExposedPorts ++= Seq(8080)
    Docker/makeBatScripts := Seq()
// Docker/dockerExposedUdpPorts

// Docker/dockerExposedVolumes

// Docker/dockerGenerateConfig


// Docker/dockerGroupLayers

//Docker/dockerLabels
Docker/dockerLabels                         := Map(
      "version" -> version.value,
      "scala"   -> scalaVersion.value
    )


// Docker/dockerLayerMappings

// Docker/dockerPackageMappings

// Docker/dockerPermissionStrategy

// Docker/dockerRepository

// Docker/dockerRmiCommand

// Docker/dockerUpdateLatest
//Docker/dockerUpdateLatest := true

// Docker/dockerUsername

// Docker/dockerVersion

Docker / packageName := "shopping"


Docker/daemonUser := "daemon"
Docker / version := (ThisBuild / version).value

val scala213 = "2.13.15"
val scala333 = "3.3.3"
lazy val defaultScalaV = sys.env.get("SCALA_VERSION") match {
  case None | Some("2.13") => scala213
  case Some("3.3.3")        => scala333
  case Some(unsupported)   => throw new IllegalArgumentException(s"doesn't support $unsupported Scala version")
}

ThisBuild/usePipelining := true

lazy val IntegrationTest = config("it") extend Test

lazy val connector = (project in file("connector"))
  .configs(IntegrationTest)
  //.settings(Defaults.itSettings: _*) //This and above enables the "it" suite







  Compile / packageDoc / mappings := Seq()

  Compile / sourcesInBase := false

  // first define a task key
  lazy val mytest = taskKey[Unit]("My test key to show how scoped settings work")

  // then implement the task key
  mytest := {
      val dirs = (Test/sourceDirectories).value
      println(dirs)
  }


  lazy val mycompile = taskKey[Unit]("My compile key to show how scoped settings work")

  mycompile := {
      val dirs = ( Compile/sourceDirectories).value
      println(dirs)
  }

  lazy val mysetting = settingKey[String]("My setting")

  mysetting := "mysetting for the current project, all configurations and all tasks"

  Test/mysetting := "mysetting for the current project, for the Test configuration and all tasks"

  //mysetting in Test in myTask := "mysetting for the current project, for the Test configuration for the task MyTask only"
Test/mysetting / myTask := ";;mysetting for the current project, for the Test configuration for the task MyTask only"

Compile/mysetting / myTask := ";;mysetting for the current project, for the Test configuration for the task MyTask only"
//mysetting in Test in myTask := "mysetting for the current project, for the Test configuration for the task MyTask only"
Test/ myTask/mysetting  := "mysetting for the current project, for the Test configuration for the task MyTask only"

Compile/ myTask/mysetting  := "mysetting for the current project, for the Compile configuration for the task MyTask only"

//Runtime/ myTask/mysetting  := "mysetting for the current project, for the Runtime configuration for the task MyTask only"
  lazy val myTask = taskKey[Unit]("My task")

  myTask := {
      val str = (Test/mysetting / myTask).value
      println(str)
  }


  lazy val task1 = taskKey[String]("t1")
  lazy val task2 = taskKey[String]("t2")
  lazy val task3 = taskKey[String]("t3")
  lazy val runAll = taskKey[String]("all parallel (the default behavior)")

  task1 := {
    Thread.sleep(1000)
    println("t1")
    "task1"
  }

  task2 := {
    Thread.sleep(750)
    println("t2")
    "task2"
  }

  task3 := {
    Thread.sleep(850)
    println("t3")
    "task3"
  }

  runAll := {
    val t1 = task1.value
    val t2 = task2.value
    val t3 = task3.value
    val all = s"$t1 - $t2 - $t3"
    println(all)
    all
  }

  lazy val runAllSequential = taskKey[String]("all sequential (forced by use of Def.sequential().value")

  runAllSequential := Def.sequential(task1, task2, task3).value


  lazy val choice = settingKey[String]("The task to execute")
  choice := "t1"

  lazy val staticChoice = taskKey[Unit]("")
  staticChoice := Def.taskDyn {
    choice.value match {
      case "t1" => task1.toTask
      case "t2" => task2.toTask
      case "t3" => task3.toTask
      case "all" => runAll.toTask
      case _ => runAllSequential.toTask
    }
  }.value

  ThisBuild / developers := List(
    Developer(
      id = "sample",
      name = "Developer Name",
      email = "Developer email",
      url = url("http://localhost")
    )
  )

  ThisBuild / description := "sbt"
  ThisBuild / licenses := Seq("MIT License" -> url("http://localhost"))
  ThisBuild / homepage := Some(url("http://localhost"))

  //unmanagedSources/includeFilter

  Compile/name := "hello"

  Compile/compile/scalacOptions := Seq("-deprecation", "-feature")

  ThisBuild / credentials += {
      val credHost = new java.net.URL("http//localhost")
      Credentials("ProGet Feed ", credHost.getHost(), "api", "key")
  }

  // packageSrc/packageOptions

  // packageBin/packageOptions

  // packageDoc/packageOptions
  //Defaults.itSettings
  
  // ThisBuild / develocityConfiguration ~= { previous =>
  //   previous
  //     .withServer(
  //       previous.server
  //         .withUrl(url("https://develocity.mycompany.com"))
  //     )
  // }

  ThisProject/Compile / console / scalacOptions+="-deprecation"

  // same as Zero / Zero / Zero / concurrentRestrictions
  Global / concurrentRestrictions := Seq(
    Tags.limitAll(1)
  )

  