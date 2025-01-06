package shell

import sn.gnome.glib.internal.*
import sn.gnome.gtk4.internal.*
import sn.gnome.gtk4.fluent.*
import scala.scalanative.unsafe.*

object InputMapping {
  def add[I <: Interactive](interactive: I)(
      sessionRef: Ptr[Session[interactive.type]],
      controller: Ptr[GtkEventController],
      bindings: interactive.Bindings
  )(using Tag[Session[I]]): Ptr[GtkEventController] = {
    val keyPressedCallback = CFuncPtr5.fromScalaFunction {
      (
          actualController: Ptr[GtkEventControllerKey],
          keyval: guint,
          _: guint,
          _: GdkModifierType,
          data: gpointer
      ) =>
        {
          val sessionRef = data.value.asPtr[Session[interactive.type]]
          val session = !sessionRef
          val controllerId: ControllerId = actualController.toString
          val inputSource = session.inputSource(controllerId)
          inputSource.keyBindings.lift(keyval.value.toInt) match {
            case Some(input) => {
              session.emitInputStart(controllerId, input)
              gboolean(gint(1))
            }
            case None => gboolean(gint(0))
          }
        }
    }

    val keyReleasedCallback = CFuncPtr5.fromScalaFunction {
      (
          actualController: Ptr[GtkEventControllerKey],
          keyval: guint,
          _: guint,
          _: GdkModifierType,
          data: gpointer
      ) =>
        {
          val sessionRef = data.value.asPtr[Session[interactive.type]]
          val controllerId: ControllerId = actualController.toString
          val session = !sessionRef
          val inputSource = session.inputSource(controllerId)
          inputSource.keyBindings.lift(keyval.value.toInt) match {
            case Some(input) => {
              session.emitInputStop(controllerId, input)
              gboolean(gint(1))
            }
            case None => gboolean(gint(0))
          }
        }
    }

    g_signal_connect(
      controller,
      c"key-pressed",
      keyPressedCallback,
      sessionRef.asPtr[Byte]
    )
    g_signal_connect(
      controller,
      c"key-released",
      keyReleasedCallback,
      sessionRef.asPtr[Byte]
    )

    gtk_event_controller_set_propagation_phase(
      controller,
      GtkPropagationPhase.GTK_PHASE_CAPTURE
    )

    val controllerId: ControllerId = controller.toString
    (!sessionRef).addInputSource(controllerId, bindings)

    controller
  }
}
