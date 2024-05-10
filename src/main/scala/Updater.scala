import glib.all.*
import gtk.all.*
import gtk.fluent.*

import scala.scalanative.unsafe.*

object Updater:
  def tick[S, E] = CFuncPtr3.fromScalaFunction {
    (_: Ptr[GtkWidget], _: Ptr[GdkFrameClock], data: gpointer) =>

      val session = data.value.asPtr[Session[S, E]]
      val holder = !session

      val micros = g_get_monotonic_time().value
      val deltaMicros = holder.priorMicros match {
        case None              => 20000
        case Some(priorMicros) => micros - priorMicros
      }
      val deltaT = deltaMicros.toDouble / 1000000.0

      holder.state foreach { state =>
        val updatedState = holder.updater(
          deltaT,
          micros,
          holder.events.toSeq,
          state
        )

        (!session).state = Some(updatedState)
      }

      (!session).priorMicros = Some(micros)
      (!session).events.clear()
      gboolean(1)
  }
end Updater

@FunctionalInterface
trait Updater[S, E]:
  def apply(deltaT: Double, micros: Long, events: Seq[E], state: S): S
