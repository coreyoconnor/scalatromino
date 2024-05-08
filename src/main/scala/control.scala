import glib.all.*
import gtk.all.*
import gtk.fluent.*

import scala.scalanative.unsafe.*

object UpdateStateHolder:
  val tick = CFuncPtr3.fromScalaFunction {
    (_: Ptr[GtkWidget], _: Ptr[GdkFrameClock], data: gpointer) =>

    val stateHolder = data.value.asPtr[StateHolder]

    val micros = g_get_monotonic_time().value
    val deltaMicros = (!stateHolder).priorMicros match {
      case None => 20000
      case Some(priorMicros) => {
        micros - priorMicros
      }
    }
    val deltaT = deltaMicros.toDouble / 1000000.0
    (!stateHolder).priorMicros = Some(micros)

    (!stateHolder).state foreach { state =>
      val updatedState = GameState.update(
        deltaT,
        micros,
        (!stateHolder).events.toSeq,
        state
      )
      (!stateHolder).state = Some(updatedState)
    }

    (!stateHolder).events.clear()
    gboolean(1)
  }
