package game

import game.tetris.*
import shell.control.*

import gio.all.*
import gtk.all.*
import gtk.fluent.*
import scala.scalanative.unsafe.*

@main def main =
  gtk_init()

  val app = gtk_application_new(
    c"com.coreyoconnor.scalatromino",
    GApplicationFlags.G_APPLICATION_FLAGS_NONE
  )

  val session = stackalloc[Session[TetrisGameState, GameEvent]](1)
  !session = new Session(TetrisGameState.update)

  g_signal_connect(
    app,
    c"activate",
    MainUI.activate,
    session.asPtr[Byte]
  )

  g_application_run(app.asPtr[GApplication], 0, null)
end main
