package shell.control

import game.Game

import glib.all.*
import gtk.all.*
import gtk.fluent.*

import scala.scalanative.unsafe.*

object StateUpdater:
  def tick = CFuncPtr3.fromScalaFunction {
    (_: Ptr[GtkWidget], _: Ptr[GdkFrameClock], data: gpointer) =>

      val sessionRef = data.value.asPtr[Session[?]]
      val session = !sessionRef

      val micros = g_get_monotonic_time().value
      val deltaMicros = session.priorMicros match {
        case None              => 20000
        case Some(priorMicros) => micros - priorMicros
      }
      val deltaT = deltaMicros.toDouble / 1000000.0

      session.update(deltaT, micros)

      (!sessionRef).priorMicros = Some(micros)
      (!sessionRef).events.clear()
      gboolean(1)
  }
end StateUpdater

@FunctionalInterface
trait StateUpdater[S, E]:
  def apply(deltaT: Double, micros: Long, events: Seq[E], state: S): S
