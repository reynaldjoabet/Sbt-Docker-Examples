# Sbt-Examples
what tasks depends on what? -> sbt scope delegation



```scala
/**
 * Identifies a setting.  It consists of three parts: the scope, the name, and the type of a value associated with this key.
 * The scope is represented by a value of type Scope.
 * The name and the type are represented by a value of type `AttributeKey[T]`.
 * Instances are constructed using the companion object.
 */
sealed abstract class SettingKey[T]
    extends ScopedTaskable[T]
    with KeyedInitialize[T]
    with Scoped.ScopingSetting[SettingKey[T]]
    with Scoped.DefinableSetting[T] {
         val key: AttributeKey[T]

         final def scopedKey: ScopedKey[T] = ScopedKey(scope, key)
    }



/**
 * Identifies a task.  It consists of three parts: the scope, the name, and the type of the value computed by a task associated with this key.
 * The scope is represented by a value of type Scope.
 * The name and the type are represented by a value of type `AttributeKey[Task[T]]`.
 * Instances are constructed using the companion object.
 */
sealed abstract class TaskKey[T]
    extends ScopedTaskable[T]
    with KeyedInitialize[Task[T]]
    with Scoped.ScopingSetting[TaskKey[T]]
    with Scoped.DefinableTask[T] {
          val key: AttributeKey[Task[T]]

            def scopedKey: ScopedKey[Task[T]] = ScopedKey(scope, key)
    }

    /**
 * Identifies an input task.  An input task parses input and produces a task to run.
 * It consists of three parts: the scope, the name, and the type of the value produced by an input task associated with this key.
 * The scope is represented by a value of type Scope.
 * The name and the type are represented by a value of type `AttributeKey[InputTask[T]]`.
 * Instances are constructed using the companion object.
 */
sealed trait InputKey[T]
    extends Scoped
    with KeyedInitialize[InputTask[T]]
    with Scoped.ScopingSetting[InputKey[T]]
    with Scoped.DefinableSetting[InputTask[T]] {

  val key: AttributeKey[InputTask[T]]

  override def toString: String = s"InputKey($scope / $key)"

  def scopedKey: ScopedKey[InputTask[T]] = ScopedKey(scope, key)
    }

    final case class Task[T](info: Info[T], work: Action[T])
    final class InputTask[T] private (val parser: State => Parser[Task[T]])
```

```scala
 /** A common type for SettingKey and TaskKey so that both can be used as inputs to tasks.*/
sealed trait ScopedTaskable[T] extends Scoped with Taskable[T]


/** An abstraction on top of Settings for build configuration and task definition. */
sealed trait Scoped extends Equals {
  def scope: Scope
  val key: AttributeKey[_]
}

```



```scala
final case class Scope(
    project: ScopeAxis[Reference],
    config: ScopeAxis[ConfigKey],
    task: ScopeAxis[AttributeKey[_]],
    extra: ScopeAxis[AttributeMap]
)

object Scope {
  val ThisScope: Scope = Scope(This, This, This, This)
  val Global: Scope = Scope(Zero, Zero, Zero, Zero)
  val GlobalScope: Scope = Global
}

/**
 * This is a scope component that represents not being
 * scoped by the user, which later could be further scoped automatically
 * by sbt.
 */
case object This extends ScopeAxis[Nothing]

/**
 * Zero is a scope component that represents not scoping.
 * It is a universal fallback component that is strictly weaker
 * than any other values on a scope axis.
 */
case object Zero extends ScopeAxis[Nothing]

/**
 * Select is a type constructor that is used to wrap type `S`
 * to make a scope component, equivalent of Some in Option.
 */
final case class Select[S](s: S) extends ScopeAxis[S] {
  override def isSelect = true
}

// in all of these, the URI must be resolved and normalized before it is definitive

/** Identifies a project or build. */
sealed trait Reference

/** A fully resolved, unique identifier for a project or build. */
sealed trait ResolvedReference extends Reference

/** Identifies a build. */
sealed trait BuildReference extends Reference

/** Identifies the build for the current context. */
final case object ThisBuild extends BuildReference

/** Uniquely identifies a build by a URI. */
final case class BuildRef(build: URI) extends BuildReference with ResolvedReference

/** Identifies a project. */
sealed trait ProjectReference extends Reference

/** Uniquely references a project by a URI and a project identifier String. */
final case class ProjectRef(build: URI, project: String)
    extends ProjectReference
    with ResolvedReference

/** Identifies a project in the current build context. */
final case class LocalProject(project: String) extends ProjectReference

/** Identifies the root project in the specified build. */
final case class RootProject(build: URI) extends ProjectReference

/** Identifies the root project in the current build context. */
final case object LocalRootProject extends ProjectReference

/** Identifies the project for the current context. */
final case object ThisProject extends ProjectReference


  sealed trait ScopingSetting[ResultType] {
    // @deprecated(Scope.inIsDeprecated, "1.5.0")
    def in(s: Scope): ResultType

      def scopedSetting[T](s: Scope, k: AttributeKey[T]): SettingKey[T] =
    new SettingKey[T] { val scope = s; val key = k }

  def scopedInput[T](s: Scope, k: AttributeKey[InputTask[T]]): InputKey[T] =
    new InputKey[T] { val scope = s; val key = k }

  def scopedTask[T](s: Scope, k: AttributeKey[Task[T]]): TaskKey[T] =
    new TaskKey[T] { val scope = s; val key = k }


  }
````

buildSettings are settings defined to be in `ThisBuild` or directly against the `Build` object. They are initialized once for the build

```scala
ThisBuild /foo := "hi"
```
projectSettings are settings specific to a project. They are specific to a particular subproject in the build. A plugin may be contributting its settings to more than one project, in which case the values are duplicated for each project. You can add project specific settings 
```scala
lazy val root= (project in file("."))
```


##### ThisBuild:
 means that the settings inside it are the same for all subprjects in the build

##### Settings
 are a sequence of key-value pairs called settings expressions.The `:=` operator ,in the sbt DSL, represents an association. When we write  `name:="app"` we are simply associating the key `name` to the value `app`

In a multi-project build, we cannot run `sbt run` directly, even if we set the `mainClass` key in the root project. For example, if we had to run the `app` module , we ha e to use `sbt app/run` and the same is true for other tasks like `sbt app/test`

if the main class is named exactly `Main`, there is no need to add it to the build. if it is a different name, then `mainClass` needs to be set

##### aggregate
it means that each time a task is run in root, it will also run in common

sbt package: What this will do is package your code into a JAR fil

sbt publishLocal : This will take the JAR file that was created by SBT (can be found somewhere in the target folder) and push it for you to our local repository manager (ivy2).



```scala
lazy val global = project
  .in(file("."))
  .aggregate(
    common,
    multi1,
    multi2
  )

lazy val common = project
  
lazy val multi1 = project
  .dependsOn(
    common
  )

lazy val multi2 = project
  .dependsOn(
    common
  )
  ```
Using aggregate in global implies that running a task on the aggregate project will also run it on the aggregated ones. On the other hand, using dependsOn sets the code dependency between sub-projects.

By default, dependency is set for the compile configuration. If we'd need to also depend on the test configuration (i.e. we have tests depending on other sub-project's test source code), we'd need to define dependency as follows: common % "compile->compile;test->test".



SettingKey[T] is evaluated once when sbt starts ( or with reload command)

TaskKey[T] is evaluated every time a command is run

InputKey[T] is for tasks taking arguments( eg testOnly .*SomeSpec)



```scala
sealed trait Project extends ProjectDefinition[ProjectReference] with CompositeProject {}

```
sbt provides a DSL to define tasks(Setting can be considered a Task that runs only once). This makes sbt a Task engine



```scala

/** Represents an Ivy configuration. */
final class Configuration private[sbt] (
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val extendsConfigs: Vector[Configuration],
    val transitive: Boolean
) extends ConfigurationExtra

 lazy val Default = Configuration.of("Default", "default")
  lazy val Compile = Configuration.of("Compile", "compile")
  @deprecated("Create a separate subproject for testing instead", "1.9.0")
  lazy val IntegrationTest = Configuration.of("IntegrationTest", "it") extend (Runtime)
  lazy val Provided = Configuration.of("Provided", "provided")
  lazy val Runtime = Configuration.of("Runtime", "runtime") extend (Compile)
  lazy val Test = Configuration.of("Test", "test") extend (Runtime)
  lazy val System = Configuration.of("System", "system")
  lazy val Optional = Configuration.of("Optional", "optional")
  lazy val Pom = Configuration.of("Pom", "pom")

```

a key depends on the context

sbt calls these context dependencies :Scoped axes. there are 3 different axes needed to fully qualify a task

project axis, the configuration axis and the task axis
project axis eg name, scalaVersion

configuration axis. the sources task depends on the "configuration"


managed library uses % to denote the configuration  libraryDependencies


libraryDependencies += "org.spec2" %% "spec2" % 2.2.3" % Test

libraryDependencies += "org.spec2" %% "spec2" % 2.2.3" % "test"

Library dependencies can be added in two ways:

    unmanaged dependencies are jars dropped into the lib directory
    managed dependencies are configured in the build definition and downloaded automatically from repositories 

    Unmanaged dependencies work like this: add jars to lib and they will be placed on the project classpath



    unmanagedBase// ~/folder/sbt-examples/lib
   
    //sbt-examples/src/main/resources// resourceDirectories also resourceDirectory
```scala
[info] Delegates:
[info]  Compile / managedSourceDirectories
[info]  managedSourceDirectories
[info]  ThisBuild / Compile / managedSourceDirectories
[info]  ThisBuild / managedSourceDirectories
[info]  Zero / Compile / managedSourceDirectories
[info]  Global / managedSourceDirectories
[info] Related:
[info]  domain / Test / managedSourceDirectories
[info]  domain / Compile / managedSourceDirectories
[info]  multi1 / Test / managedSourceDirectories
[info]  multi1 / Compile / managedSourceDirectories
[info]  multi2 / Test / managedSourceDirectories
[info]  multi2 / Compile / managedSourceDirectories
[info]  common / Test / managedSourceDirectories
[info]  common / Compile / managedSourceDirectories
[info]  Test / managedSourceDirectories
```


```scala
[info] Dependencies:
[info]  Compile / packageBin
[info] Delegates:
[info]  Compile / package
[info]  package
[info]  ThisBuild / Compile / package
[info]  ThisBuild / package
[info]  Zero / Compile / package
[info]  Global / package
[info] Related:
[info]  multi1 / Compile / package
[info]  common / Compile / package
[info]  domain / Test / package
[info]  multi1 / Test / package
[info]  domain / Compile / package
[info]  multi2 / Test / package
[info]  common / Test / package
[info]  Test / package
[info]  multi2 / Compile / package
```



->run `tasks` on sbt shell

This is a list of tasks defined for the current project.
It does not list the scopes the tasks are defined in; use the 'inspect' command for that.
Tasks produce values.  Use the 'show' command to run the task and print the resulting value.

  run `settings -V` on the sbt shell

  A setting can't depend on a task  but a task can depend on both another task and setting

  `remoteCacheId` uses hash of content hashes for input sources.

sbt remote caching exposes 2 main sbt tasks to the end user:

`pushRemoteCache` to push the remote cache artifact to a repository
`pullRemoteCache` to pull the remote cache artifact from the repository
and a setting key:

`pushRemoteCacheTo` to specify the repository

When we pushRemoteCache we are packaging up the project into a JAR file.

build artifacts are files produced by a build. typically these include distribution packages, war files, container images, reports, log files etc

Typically, autoImport is used to provide new keys - SettingKeys, TaskKeys, or InputKeys - or core methods without requiring an import or qualification.

A plugin extends the build definition, most commonly by adding new settings. The new settings could be new tasks. For example, a plugin could add a codeCoverage task which would generate a test coverage report.

A plugin can declare that its settings be automatically added to the build definition, in which case you don’t have to do anything to add them.

The enablePlugins method allows projects to explicitly define the auto plugins they wish to consume.

- run: sbt pushRemoteCache
    - run: cat target/scala-2.13/routes/main/router/RoutesPrefix.scala
    - run: sbt clean pullRemoteCache compile
    - run: cat target/scala-2.13/routes/main/router/RoutesPrefix.scala


[sbt plugins](https://www.scala-sbt.org/1.x/docs/Plugins.html)
[remote caching](https://www.muki.rocks/sbt-remote-cache-recipes/)
  [remote caching](https://reibitto.github.io/blog/remote-caching-with-sbt-and-s3/)

[remote caching](https://aminaadewusi.com/sbt-remote-caching-intro/)

The plugin needs to be extended from AutoPlugin. By doing this, we can override the below methods:

trigger: Determines whether this AutoPlugin will be activated for this project when the `requires` clause is satisfied.
requires: Defines the dependencies of the plugin.
projectSettings: Sequence of settings to be added to the project scope where this plugin is activated.


for example, the compile task depends on the sources task, which depends on the sourceDirectories task and the sourceGenerators task, and so on.

sbt:Sbt-Examples> tasks -V

This is a list of tasks defined for the current project.
It does not list the scopes the tasks are defined in; use the 'inspect' command for that.
Tasks produce values.  Use the 'show' command to run the task and print the resulting value.

 

#### Scope
A  scope is thr combination of three axes; project, task and config

project axis - the subprojects inside the build definition file. also known as multimodules

config axis- the context of the build and the most common are Compile, Runtime and Test
Settings and tasks can also be scoped to specific configurations, such as Compile, Test, Runtime, etc
Configuration scope is used to customize behavior for different phases of the build process. For example, you might want to include a testing library dependency only for the Test configuration, or specify different compiler options for Compile and Test configurations.

A build definition is defined in build.sbt, and it consists of a set of projects (of type Project)

Each entry is called a setting expression. Some among them are also called task expressions.

A key is an instance of SettingKey[T], TaskKey[T], or InputKey[T] where T is the expected value type


A TaskKey[T] is said to define a task. Tasks are operations such as compile or package. They may return Unit (Unit is void for Scala), or they may return a value related to the task, for example package is a TaskKey[File] and its value is the jar file it creates.

task axis- function in the build definition that executions anytime it is called

you can associate keys only to specific tasks


```scala
sealed trait ProjectDefinition[PR <: ProjectReference] {

  /**
   * The project ID is used to uniquely identify a project within a build.
   * It is used to refer to a project from the command line and in the scope of keys.
   */
  def id: String

  /** The base directory for the project.*/
  def base: File

  /**
   * The configurations for this project.  These are groups of related tasks and the main reason
   * to list them here is when one configuration extends another.  In this case, a setting lookup
   * in one configuration will fall back to the configurations it extends configuration if the setting doesn't exist.
   */
  def configurations: Seq[Configuration]

  /**
   * The explicitly defined sequence of settings that configure this project.
   * These do not include the automatically appended settings as configured by `auto`.
   */
  def settings: Seq[Setting[_]]

  /**
   * The references to projects that are aggregated by this project.
   * When a task is run on this project, it will also be run on aggregated projects.
   */
  def aggregate: Seq[PR]

  /** The references to projects that are classpath dependencies of this project. */
  def dependencies: Seq[ClasspathDep[PR]]

  /** The references to projects that are aggregate and classpath dependencies of this project. */
  def uses: Seq[PR] = aggregate ++ dependencies.map(_.project)
  def referenced: Seq[PR] = uses

  /**
   * The defined [[Plugins]] associated with this project.
   * A [[AutoPlugin]] is a common label that is used by plugins to determine what settings, if any, to add to a project.
   */
  def plugins: Plugins

  /** Indicates whether the project was created organically, or was generated synthetically. */
  def projectOrigin: ProjectOrigin

  /** The [[AutoPlugin]]s enabled for this project.  This value is only available on a loaded Project. */
  private[sbt] def autoPlugins: Seq[AutoPlugin]

  }

sealed trait Project extends ProjectDefinition[ProjectReference] 
```



You can run a task in another project by explicitly specifying the project ID, such as subProjectID/compile.


each key can have an associated value in more than one context, called a scope.

Some concrete examples:

if you have multiple projects (also called subprojects) in your build definition, a key can have a different value in each project.
the compile key may have a different value for your main sources and your test sources, if you want to compile them differently.
the packageOptions key (which contains options for creating jar packages) may have different values when packaging class files (packageBin) or packaging source code (packageSrc).
There is no single value for a given key name, because the value may differ according to scope

However, there is a single value for a given scoped key.

Often the scope is implied or has a default, but if the defaults are wrong, you’ll need to mention the desired scope in build.sbt.

`plugins` on the sbt shell
```scala
Plugins that are loaded to the build but not enabled in any subprojects:
  com.typesafe.sbt.SbtNativePackager
  com.typesafe.sbt.packager.archetypes.JavaAppPackaging
  com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
  com.typesafe.sbt.packager.archetypes.jar.ClasspathJarPlugin
  com.typesafe.sbt.packager.archetypes.jar.LauncherJarPlugin
  com.typesafe.sbt.packager.archetypes.jlink.JlinkPlugin
  com.typesafe.sbt.packager.archetypes.scripts.AshScriptPlugin
  com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin
  com.typesafe.sbt.packager.archetypes.scripts.BatStartScriptPlugin
  com.typesafe.sbt.packager.archetypes.systemloader.SystemVPlugin
  com.typesafe.sbt.packager.archetypes.systemloader.SystemdPlugin
  com.typesafe.sbt.packager.archetypes.systemloader.SystemloaderPlugin
  com.typesafe.sbt.packager.archetypes.systemloader.UpstartPlugin
  com.typesafe.sbt.packager.debian.DebianDeployPlugin
  com.typesafe.sbt.packager.debian.DebianPlugin
  com.typesafe.sbt.packager.debian.JDebPackaging
  com.typesafe.sbt.packager.docker.DockerPlugin
  com.typesafe.sbt.packager.docker.DockerSpotifyClientPlugin
  com.typesafe.sbt.packager.graalvmnativeimage.GraalVMNativeImagePlugin
  com.typesafe.sbt.packager.jdkpackager.JDKPackagerDeployPlugin
  com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin
  com.typesafe.sbt.packager.linux.LinuxPlugin
  com.typesafe.sbt.packager.rpm.RpmDeployPlugin
  com.typesafe.sbt.packager.rpm.RpmPlugin
  com.typesafe.sbt.packager.universal.UniversalDeployPlugin
  com.typesafe.sbt.packager.universal.UniversalPlugin
  com.typesafe.sbt.packager.windows.WindowsDeployPlugin
  com.typesafe.sbt.packager.windows.WindowsPlugin
  org.scalajs.sbtplugin.ScalaJSJUnitPlugin
  sbt.ScriptedPlugin
  sbt.plugins.SbtPlugin
  ```



  #### Scope axes 
A scope axis is a type constructor similar to Option[A], that is used to form a component in a scope.

There are three scope axes:

The subproject axis
The dependency configuration axis
The task axis

a full scope in sbt is formed by a tuple of a subproject, a configuration, and a task value:

`projA / Compile / console / scalacOptions`

The project axis can also be set to ThisBuild, which means the “entire build”, so a setting applies to the entire build rather than a single project. Build-level settings are often used as a fallback when a project doesn’t define a project-specific setting

### Scoping by the configuration axis 
A dependency configuration (or “configuration” for short) defines a graph of library dependencies, potentially with its own classpath, sources, generated packages, etc.

By default, all the keys associated with compiling, packaging, and running are scoped to a configuration and therefore may work differently in each configuration. The most obvious examples are the task keys compile, package, and run; but all the keys which affect those keys (such as sourceDirectories or scalacOptions or fullClasspath) are also scoped to the configuration.

- A scope is a tuple of components in three axes: the subproject axis, the configuration axis, and the task axis.
- There’s a special scope component Zero for any of the scope axes.
- There’s a special scope component ThisBuild for the subprojects axis only.
Test extends Runtime, and Runtime extends Compile configuration.
- A key placed in build.sbt is scoped to ${current subproject} / Zero / Zero by default.
A key can be scoped using / operator.



Scope delegation rules 
Here are the rules for scope delegation:

- Rule 1: Scope axes have the following precedence: the subproject axis, the configuration axis, and then the task axis.
- Rule 2: Given a scope, delegate scopes are searched by substituting the task axis in the following order: the given task scoping, and then Zero, which is non-task scoped version of the scope.
- Rule 3: Given a scope, delegate scopes are searched by substituting the configuration axis in the following order: the given configuration, its parents, their parents and so on, and then Zero (same as unscoped configuration axis).
- Rule 4: Given a scope, delegate scopes are searched by substituting the subproject axis in the following order: the given subproject, ThisBuild, and then Zero.
- Rule 5: A delegated scoped key and its dependent settings/tasks are evaluated without carrying the original context.
 Each component may be Zero (no specific value), This (current context), or Select (containing a specific value). sbt resolves This_ to either Zero or Select depending on the context.


 For example, in a project, a This project axis becomes a Select referring to the defining project. All other axes that are This are translated to Zero. Functions like inConfig and inTask transform This into a Select for a specific value. For example, inConfig(Compile)(someSettings) translates the configuration axis for all settings in someSettings to be Select(Compile) if the axis value is This.
[sbt scopes](https://www.scala-sbt.org/1.x/docs/Scope-Delegation.html)


 The default repository used by SBT is Maven2 and the local Ivy repository.

```scala
def mavenCentral: Resolver = DefaultMavenRepository
  def defaults: Vector[Resolver] = Vector(mavenCentral)

```


[sbt-dear-dependency-managment](https://medium.com/@linda0511ny/sbt-dear-dependency-managment-518d809027a9)


#### What is a Repository Manager?
A proxy for remote repositories which caches artifacts saving both bandwidth and time required to retrieve a software artifact from a remote repository, and
A host for internal artifacts providing an organization with a deployment target for software artifacts.
A repository manager is a dedicated server application designed to manage repositories of binary components.

nexus
 #### Deployment to Hosted Repositories
Organizations which deploy internal snapshots and releases to hosted repositories have an easier time distributing software artifacts across different teams and departments. When a department or development group deploys artifacts to a hosted repository, other departments and development groups can develop systems in parallel, relying upon dependencies served from both release and snapshot repositories. Finding an efficient way to distribute the binary software artifacts during the development cycle is essential for an organization that needs to scale system complexity and number of developers. Once you start using Nexus as a sharing mechanism across development teams, each team can then focus on smaller, more manageable systems. The web application team can focus on the code that directly supports the web application while it depends on the binary software artifacts from a team managing an Enterprise Service Bus.
A repository manager is used to storing build artifacts and provide the feature to push and pull artifacts using integration tools like Jenkins



```scala

 lazy val itSettings: Seq[Setting[_]] = inConfig(IntegrationTest) {
    testSettings
  }
  // by default just the compile, test and runtime configurations are enabled
  lazy val defaultConfigs: Seq[Setting[_]] = inConfig(Compile)(compileSettings) ++
    inConfig(Test)(testSettings) ++
    inConfig(Runtime)(Classpaths.configSettings)



//in sbt for integration test
//
lazy val tests = (project in file("tests"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings,
  libraryDependencies += scalatest % "it,test"
    // other settings here
  )
```

- configs(IntegrationTest) adds the predefined integration test configuration. This configuration is referred to by the name it.
- settings(Defaults.itSettings) adds compilation, packaging, and testing actions and settings in the IntegrationTest configuration.
- settings(libraryDependencies += scalatest % "it,test") adds scalatest to both the standard test configuration and the integration test configuration it. To define a dependency only for integration tests, use “it” as the configuration instead of “it,test”.

The standard source hierarchy is used:

`src/it/scala` for Scala sources
`src/it/java` for Java sources
`src/it/resources` for resources that should go on the integration test classpath

 For example to run all integration tests:

`> IntegrationTest/test`
Or to run a specific test:

`> IntegrationTest/testOnly org.example.AnIntegrationTest`


Similarly the standard settings may be configured for the `IntegrationTest` configuration. If not specified directly, most `IntegrationTest` settings delegate to `Test` settings by default. For example, if test options are specified as:

`Test / testOptions += ...`
then these will be picked up by the `Test` configuration and in turn by the `IntegrationTest` configuration. Options can be added specifically for integration tests by putting them in the `IntegrationTest` configuration:

`IntegrationTest / testOptions += ...`

Or, use `:=` to overwrite any existing options, declaring these to be the definitive integration test options:

`IntegrationTest / testOptions := Seq(...)`

sbt 1.9.0 deprecates IntegrationTest configuration

The recommended migration path is to create a subproject named “integration”


use to skip publishing artifact 
    `projectname/publish / skip := true`