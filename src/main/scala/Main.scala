import gio.all.*
import glib.all.*
import gtk.all.*
import gtk.fluent.*
import scala.scalanative.unsafe.*
import libcairo.all.*

object State:
  val init = State()

  def update(deltaT: Double, micros: Long, state: State): State = {
    state.copy(
      absCycle = (micros % 1000000).toDouble / 999999.0,
      relCycle = {
        val next = state.relCycle + deltaT
        if (next > 1.0) 0.0 else next
      }
    )
  }
end State

case class State(absCycle: Double = 0.0, relCycle: Double = 0.0)

class StateHolder:
  var state: State = State.init
  var priorMicros: Option[Long] = None
end StateHolder

object GameRenderer:
  def render(drawingArea: Ptr[GtkDrawingArea], cr: Ptr[cairo_t], width: CInt, height: CInt, data: gpointer): Unit = {
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

    val updatedState = State.update(deltaT, micros, (!stateHolder).state)
    (!stateHolder).state = updatedState

    cairo_set_source_rgb(cr, updatedState.absCycle, 0, 0)
    cairo_rectangle(cr, 0, 0, width/2, height)
    cairo_fill(cr)

    cairo_set_source_rgb(cr, 0, updatedState.relCycle, 0)
    cairo_rectangle(cr, width/2, 0, width/2, height)
    cairo_fill(cr)
  }
end GameRenderer

@main def example =
  gtk_init()

  val activateCallback = CFuncPtr2.fromScalaFunction {
    (application: Ptr[GtkApplication], data: gpointer) =>
      val stateHolder = data.value.asPtr[StateHolder]

      val window = gtk_application_window_new(application)

      gtk_window_set_title(
        window.asInstanceOf[Ptr[GtkWindow]],
        c"Blocks"
      )
      gtk_window_set_default_size(window.asPtr[GtkWindow], 640, 480)

      val box = gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL, 0)
      gtk_widget_set_halign(box, GtkAlign.GTK_ALIGN_CENTER)
      gtk_widget_set_valign(box, GtkAlign.GTK_ALIGN_CENTER)

      gtk_window_set_child(window.asPtr[GtkWindow], box)

      val button = gtk_button_new_with_label(c"Press me")

      val printHello = CFuncPtr2.fromScalaFunction {
        (widget: Ptr[GtkWidget], data: gpointer) =>
          g_print(c"Yoooo, click!".asGString)
      }

      g_signal_connect(button, c"clicked", printHello)

      gtk_box_append(box.asPtr[GtkBox], button)

      val drawingArea = gtk_drawing_area_new().asPtr[GtkDrawingArea]

      gtk_drawing_area_set_content_width(drawingArea, 640)
      gtk_drawing_area_set_content_height(drawingArea, 480)
      val tickCallback = CFuncPtr3.fromScalaFunction {
        (widget: Ptr[GtkWidget], frameClock: Ptr[GdkFrameClock], data: gpointer) => {
          gtk_widget_queue_draw(widget)
          gboolean(1)
        }
      }
      gtk_widget_add_tick_callback(drawingArea.asPtr[GtkWidget], tickCallback.asInstanceOf[GtkTickCallback], gpointer(null), GDestroyNotify(null))

      val drawingFunction = CFuncPtr5.fromScalaFunction(GameRenderer.render)
      gtk_drawing_area_set_draw_func(drawingArea, drawingFunction.asInstanceOf[GtkDrawingAreaDrawFunc], data, GDestroyNotify(null))

      gtk_box_append(box.asPtr[GtkBox], drawingArea.asPtr[GtkWidget])

      gtk_widget_show(window)
  }

  val app = gtk_application_new(
    c"org.gtk.example",
    GApplicationFlags.G_APPLICATION_FLAGS_NONE
  )

  val stateHolder = stackalloc[StateHolder](1)
  !stateHolder = new StateHolder

  g_signal_connect(
    app,
    c"activate",
    activateCallback,
    stateHolder.asPtr[Byte]
  )

  g_application_run(app.asPtr[GApplication], 0, null)
end example
