import glib.all.*
import gtk.all.*
import gtk.fluent.*

import scala.scalanative.unsafe.*

object GameUpdater:
  def tick[S, E] = CFuncPtr3.fromScalaFunction {
    (_: Ptr[GtkWidget], _: Ptr[GdkFrameClock], data: gpointer) =>

      val stateHolder = data.value.asPtr[StateHolder[S, E]]
      val holder = !stateHolder

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

        (!stateHolder).state = Some(updatedState)
      }

      (!stateHolder).priorMicros = Some(micros)
      (!stateHolder).events.clear()
      gboolean(1)
  }
end GameUpdater

@FunctionalInterface
trait GameUpdater[S, E]:
  def apply(deltaT: Double, micros: Long, events: Seq[E], state: S): S
