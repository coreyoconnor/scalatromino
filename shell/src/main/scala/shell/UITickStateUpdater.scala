package shell

import sn.gnome.glib.internal.*
import sn.gnome.gtk4.internal.*
import sn.gnome.gtk4.fluent.*

import scala.scalanative.unsafe.*

object UITickStateUpdater:
  def tick = CFuncPtr3.fromScalaFunction {
    (_: Ptr[GtkWidget], _: Ptr[GdkFrameClock], data: gpointer) =>

      val micros = g_get_monotonic_time().value

      val sessionRef = data.value.asPtr[Session[?]]
      val session = !sessionRef

      val deltaMicros = session.priorMicros match {
        case None              => 20000
        case Some(priorMicros) => micros - priorMicros
      }
      val deltaT = deltaMicros.toDouble / 1000000.0

      session.update(deltaT, micros.longValue())

      (!sessionRef).priorMicros = Some(micros.longValue())
      (!sessionRef).events.clear()
      gboolean(gint(1))
  }

  def start[I <: Interactive](owner: Ptr[GtkWidget], sessionRef: Ptr[Session[I]]): Unit = {
    gtk_widget_add_tick_callback(
      owner,
      tick.asInstanceOf[GtkTickCallback],
      gpointer(sessionRef.asPtr[Byte]),
      GDestroyNotify(null)
    )
  }

end UITickStateUpdater

@FunctionalInterface
trait UITickStateUpdater[S, E]:
  def apply(deltaT: Double, micros: Long, events: Seq[E], state: S): S
