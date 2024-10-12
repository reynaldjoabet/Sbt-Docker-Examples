## Package the application

By default, sbt Native Packager will create a daemon user named demiourgos728. To change name of the user, add this line in your build.sbt

`daemonUser in Docker    := "daemon"`

The `JavaAppPackaging` archetype from `sbt-native-packager` provides a default application structure and executable scripts to launch your application

Enable the `JavaAppPackaging` plugin in your `build.sbt` with
`enablePlugins(JavaAppPackaging)`

```sh
sbt stage

tree ./target/universal/stage/
./target/universal/stage/
├── bin
│   ├── hello-world
│   └── hello-world.bat
└── lib
    ├── ch.epfl.scala.hello-world-1.0.jar
    ├── org.scala-lang.scala-library-2.13.1.jar
    ├── org.typelevel.cats-core_2.13-2.0.0.jar
    ├── org.typelevel.cats-kernel_2.13-2.0.0.jar
    └── org.typelevel.cats-macros_2.13-2.0.0.jar


  bash ./target/universal/stage/bin/hello-world

  ```  

 ## Generate a docker image for the application
The `Docker` Plugin from `sbt-native-packager` implement the following features

- generate a Dockerfile based on - JavaAppPackaging archetype stage.
- sbt integration to build the docker image  

Enable the Docker Plugin in your `build.sbt` with

`enablePlugins(DockerPlugin)`

Run this command to generate a Dockerfile

`sbt docker:stage`


To build the docker image run the following command
`sbt docker:publishLocal`


## Optimise the docker image

The `Docker Plugin` provide an option to override the base image.

`dockerBaseImage := "adoptopenjdk:11.0.7_10-jre-hotspot"`

[dockerize-scala-application/#run-the-hello-world-project](https://enjoytechnology.netlify.app/2020/05/11/dockerize-scala-application/#run-the-hello-world-project)

- docker images made up of a stack of layers.
- Each layer corresponds to a set of file system changes from previous layer
- a docker image is the union of all these file system changes

```sh
+---------------------+
|      Layer 3       |  <-- Changes (e.g., app code) # file
+---------------------+
|      Layer 2       |  <-- Changes (e.g., libraries) # vim
+---------------------+
|      Layer 1       |  <-- Base OS (e.g., Ubuntu) #alpine
+---------------------+
```
- Layers are read only
- All changes to an image are additive
eg
   - Adding new file to image: addss a new layer
   - Updating a file on an image: Adds a layer containing the changes
   - Deleting a file on an image: Adds a layer containing a whiteout file

   Docker shares layers between images

   All read-only layers are shared between a container and its parent image
   - top layer== container layer
   - Container layer is writable

