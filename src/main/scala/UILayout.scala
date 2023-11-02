import gio.all.*
import glib.all.*
import gtk.all.*
import gtk.fluent.*
import libcairo.all.*
import scala.scalanative.unsafe.*

object UILayout:
  def build(window: Ptr[GtkWidget],
            startGameButton: Ptr[GtkWidget],
            drawingArea: Ptr[GtkDrawingArea]
            ): Unit = {

    gtk_window_set_title(
      window.asInstanceOf[Ptr[GtkWindow]],
      c"Scalatromino"
    )

    gtk_window_set_default_size(window.asPtr[GtkWindow], 640, 480)

    val box = gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL, 0)
    gtk_widget_set_halign(box, GtkAlign.GTK_ALIGN_CENTER)
    gtk_widget_set_valign(box, GtkAlign.GTK_ALIGN_CENTER)

    gtk_window_set_child(window.asPtr[GtkWindow], box)

    gtk_box_append(box.asPtr[GtkBox], startGameButton)

    gtk_drawing_area_set_content_width(drawingArea, 1024)
    gtk_drawing_area_set_content_height(drawingArea, 768)

    gtk_box_append(box.asPtr[GtkBox], drawingArea.asPtr[GtkWidget])
  }

end UILayout
