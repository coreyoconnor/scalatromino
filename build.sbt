lazy val stage = taskKey[File]("stage")

import scala.scalanative.build._
import scala.sys.process._

def pkgConfig(pkg: String, arg: String) = {
  import sys.process.*
  s"pkg-config --$arg $pkg".!!.trim.split(" ").toList
}

inThisBuild(Seq(
  scalaVersion := "3.4.1",
  version := "0.2.0-SNAPSHOT",
  organization := "com.coreyoconnor",
  versionScheme := Some("early-semver")
))

lazy val shell = project
  .in(file("shell"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    // https://github.com/indoorvivants/scala-native-gtk-bindings
    libraryDependencies += "com.indoorvivants.gnome" %%% "gtk4" % "0.0.5",
    nativeConfig := {
      nativeConfig.value
        .withLTO(LTO.thin)
        .withMode(Mode.releaseFast)
        .withCompileOptions(pkgConfig("gtk4", "cflags") :+ "-Wno-unused-command-line-argument")
        .withLinkingOptions(pkgConfig("gtk4", "libs") :+ "-Wno-unused-command-line-argument")
    }
  )

lazy val root = project
  .in(file("."))
  .dependsOn(shell)
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig := {
      nativeConfig.value
        .withLTO(LTO.thin)
        .withMode(Mode.releaseFast)
        .withCompileOptions(pkgConfig("gtk4", "cflags") :+ "-Wno-unused-command-line-argument")
        .withLinkingOptions(pkgConfig("gtk4", "libs") :+ "-Wno-unused-command-line-argument")
    },
    stage := {
      val exeFile = (Compile / nativeLink).value
      val targetFile = target.value / "scalatromino"

      sbt.IO.copyFile(exeFile, targetFile)

      targetFile
    },
    publish := (shell / publish).value,
    publishLocal := (shell / publishLocal).value,
    Compile / resourceGenerators += Def.task {
      val inputDir = (Compile / resourceDirectory).value
      val input = inputDir / "main.gresource.xml"
      val file = (Compile / resourceManaged).value / "scala-native" / "gresources.c"
      IO.createDirectory(file.getParentFile())
      val processResult = Process(
        Seq(
          "glib-compile-resources",
          "--generate-source",
          "--target",
          file.toString(),
          input.toString()
        ),
        inputDir
      ) ! streams.value.log
      assert(processResult == 0)
      Seq(file)
    }.taskValue
  )
