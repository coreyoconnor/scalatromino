lazy val stage = taskKey[File]("stage")

import scala.scalanative.build._

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
        .withCompileOptions(pkgConfig("gtk4", "cflags"))
        .withLinkingOptions(pkgConfig("gtk4", "libs"))
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
        .withCompileOptions(pkgConfig("gtk4", "cflags"))
        .withLinkingOptions(pkgConfig("gtk4", "libs"))
    },
    stage := {
      val exeFile = (Compile / nativeLink).value
      val targetFile = target.value / "scalatromino"

      sbt.IO.copyFile(exeFile, targetFile)

      targetFile
    },
    publish := (shell / publish).value,
    publishLocal := (shell / publishLocal).value
  )
