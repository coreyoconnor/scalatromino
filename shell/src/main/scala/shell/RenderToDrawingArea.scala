package shell

import gio.all.*
import glib.all.*
import gtk.all.*
import gtk.fluent.*
import libcairo.all.*
import scala.scalanative.unsafe.*

object RenderToDrawingArea:
  val tickCallback = CFuncPtr3.fromScalaFunction {
    (widget: Ptr[GtkWidget], _: Ptr[GdkFrameClock], _: gpointer) =>
      {
        gtk_widget_queue_draw(widget)
        gboolean(1)
      }
  }

  def startRefresh(widget: Ptr[GtkWidget]): Unit = {
    gtk_widget_add_tick_callback(
      widget,
      tickCallback.asInstanceOf[GtkTickCallback],
      gpointer(null),
      GDestroyNotify(null)
    )
  }

  def withRenderTiming(rendererId: RendererId, session: Session[?])(
      f: (Double, Long) => Unit
  ): Unit =
    session.priorMicros.foreach { micros =>
      val renderMicros = g_get_monotonic_time().value
      val deltaRenderMicros = session.priorRenderMicros.get(rendererId) match {
        case None                    => 20000
        case Some(priorRenderMicros) => renderMicros - priorRenderMicros
      }
      val deltaRenderT = deltaRenderMicros.toDouble / 1000000.0

      f(deltaRenderT, renderMicros)

      session.updateRenderMicros(rendererId, renderMicros)
    }

  def startRender(
      interactive: Interactive
  )(sessionRef: Ptr[Session[interactive.type]], drawingArea: Ptr[GtkDrawingArea])(
      renderer: interactive.Renderer
  ): Unit = {
    // totally stable, right?
    val rendererId: RendererId = drawingArea.toString
    (!sessionRef).addRender(rendererId)(renderer)

    val drawFunc = CFuncPtr5.fromScalaFunction {
      (
          actualDrawingArea: Ptr[GtkDrawingArea],
          cr: Ptr[cairo_t],
          width: CInt,
          height: CInt,
          data: gpointer
      ) =>
        val rendererId: RendererId = actualDrawingArea.toString
        val sessionRef = data.value.asPtr[Session[interactive.type]]
        val session = !sessionRef

        withRenderTiming(rendererId, session) { (deltaRenderT, renderMicros) =>
          session.state foreach { state =>
            session.renders(rendererId)(
              cr,
              width,
              height,
              state,
              deltaRenderT,
              renderMicros
            )
          }
        }
    }

    gtk_drawing_area_set_draw_func(
      drawingArea,
      drawFunc.asInstanceOf[GtkDrawingAreaDrawFunc],
      gpointer(sessionRef.asPtr[Byte]),
      GDestroyNotify(null)
    )

    startRefresh(drawingArea.asPtr[GtkWidget])
  }

end RenderToDrawingArea
