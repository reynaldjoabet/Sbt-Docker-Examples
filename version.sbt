ThisBuild / version := "1.18.0-SNAPSHOT"
// In sbt2.x, the bare settings in build.sbt are interpretted to be common settings and are injected
// to all subprojects.This means we can now set scalaversion without using ThisBuild scoping

//<project-id> /Config/intask/key



//Task Chaining
lazy val taskA = taskKey[Unit]("Task A")
lazy val taskB = taskKey[Unit]("Task B, depends on task A")

taskA := {
  println("Running task A")
}

// taskB := Def.sequential(taskA, Def.task {
//   println("Running task B after task A")
// })

//taskB depends on taskA and runs after taskA is completed

lazy val printHello=taskKey[Unit]("prints hello")

printHello:= println("hello")

lazy val one= taskKey[Int]("One")

one:= 1

lazy val oneTimesTwo=taskKey[Unit]("One times two")

oneTimesTwo:= println(s"2*1=${one.value*2}")