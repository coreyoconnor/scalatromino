lazy val stage = taskKey[File]("stage")

import scala.scalanative.build._

def pkgConfig(pkg: String, arg: String) = {
  import sys.process.*
  s"pkg-config --$arg $pkg".!!.trim.split(" ").toList
}

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    scalaVersion := "3.4.1",
    // https://github.com/indoorvivants/scala-native-gtk-bindings
    libraryDependencies += "com.indoorvivants.gnome" %%% "gtk4" % "0.0.5",
    nativeConfig := {
      val out = nativeConfig.value
        .withLTO(LTO.thin)
        .withMode(Mode.releaseFast)
        .withCompileOptions(pkgConfig("gtk4", "cflags"))
        .withLinkingOptions(pkgConfig("gtk4", "libs"))

      out
    },
    stage := {
      val exeFile = (Compile / nativeLink).value
      val targetFile = target.value / "scalatromino"

      sbt.IO.copyFile(exeFile, targetFile)

      targetFile
    }
  )
