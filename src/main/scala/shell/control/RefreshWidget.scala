package shell.control

import glib.all.*
import gtk.all.*
import scala.scalanative.unsafe.*

object RefreshWidget:
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

  def renderPrimary(session: Session[?])(f: (Double, Long) => Unit): Unit =
    session.priorMicros.foreach { micros =>
      val renderMicros = g_get_monotonic_time().value
      val deltaRenderMicros = session.priorRenderMicros match {
        case None => 20000
        case Some(priorRenderMicros) => {
          renderMicros - priorRenderMicros
        }
      }
      val deltaRenderT = deltaRenderMicros.toDouble / 1000000.0

      f(deltaRenderT, renderMicros)

      session.priorRenderMicros = Some(renderMicros)
    }

  def renderSecondary(session: Session[?])(f: (Double, Long) => Unit): Unit =
    session.priorMicros.foreach { micros =>
      val renderMicros = g_get_monotonic_time().value
      val deltaRenderMicros = session.priorRenderMicros match {
        case None => 20000
        case Some(priorRenderMicros) => {
          renderMicros - priorRenderMicros
        }
      }
      val deltaRenderT = deltaRenderMicros.toDouble / 1000000.0

      f(deltaRenderT, renderMicros)
    }

end RefreshWidget
