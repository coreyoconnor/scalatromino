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
