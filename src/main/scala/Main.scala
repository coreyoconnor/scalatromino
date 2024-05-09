import gio.all.*
import glib.all.*
import gtk.all.*
import gtk.fluent.*
import libcairo.all.*
import scala.scalanative.unsafe.*

@main def main =
  gtk_init()

  val app = gtk_application_new(
    c"com.coreyoconnor.scalatromino",
    GApplicationFlags.G_APPLICATION_FLAGS_NONE
  )

  val stateHolder = stackalloc[StateHolder[TetrisGameState, GameEvent]](1)
  !stateHolder = new StateHolder(TetrisGameState.update)

  g_signal_connect(
    app,
    c"activate",
    MainUI.activate,
    stateHolder.asPtr[Byte]
  )

  g_application_run(app.asPtr[GApplication], 0, null)
end main
