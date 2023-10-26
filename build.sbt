lazy val stage = taskKey[File]("stage")

scalaVersion := "3.3.1"

enablePlugins(ScalaNativePlugin)

import scala.scalanative.build._

libraryDependencies += "com.indoorvivants.gnome" %%% "gtk4" % "0.0.4"

nativeConfig := {
  val out = nativeConfig.value
    .withLTO(LTO.thin)
    .withMode(Mode.releaseFast)
    .withGC(GC.commix)

  out
}

stage := {
  val exeFile = (Compile / nativeLink).value
  val targetFile = target.value / "cmd-on-event"

  sbt.IO.copyFile(exeFile, targetFile)

  targetFile
}
