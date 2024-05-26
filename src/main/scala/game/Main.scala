package game

import game.TetrisGame
import game.tetris
import renderer.TetrisRenderer
import shell.*
import shell.tetris.{TetrisUI, TetrisKeyBindings}

import glib.functions.g_bytes_get_data
import gio.all.*
import gtk.all.*
import gtk.fluent.*
import scala.scalanative.unsafe.*

@main def main =
  val testData = g_resources_lookup_data(c"/com/coreyoconnor/scalatromino/test.txt", GResourceLookupFlags.G_RESOURCE_LOOKUP_FLAGS_NONE, null)
  val testContents = g_bytes_get_data(testData, null)
  scalanative.libc.stdio.printf(c"WUB WUB %s\n", testContents.value)

  gtk_init()

  val app = gtk_application_new(
    c"com.coreyoconnor.scalatromino",
    GApplicationFlags.G_APPLICATION_FLAGS_NONE
  )

  val session = stackalloc[Session[TetrisGame.type]](1)
  !session = new Session(TetrisGame)(tetris.GameState)

  g_signal_connect(
    app,
    c"activate",
    TetrisUI.activate,
    session.asPtr[Byte]
  )

  g_application_run(app.asPtr[GApplication], 0, null)
end main
