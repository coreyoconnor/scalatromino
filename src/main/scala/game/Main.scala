package game

import game.TetrisGame
import game.tetris
import renderer.TetrisRenderer
import shell.*
import shell.tetris.{TetrisUI, TetrisKeyBindings}

import sn.gnome.glib.internal.g_bytes_get_data
import sn.gnome.gio.internal.*
import sn.gnome.gtk4.internal.*
import sn.gnome.gtk4.fluent.*
import scala.scalanative.unsafe.*

@main def main =
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
