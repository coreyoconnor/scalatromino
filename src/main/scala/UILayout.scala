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
    gtk_drawing_area_set_content_width(drawingArea, GameRenderer.minWidth)
    gtk_drawing_area_set_content_height(drawingArea, GameRenderer.minHeight)

    gtk_window_set_title(
      window.asInstanceOf[Ptr[GtkWindow]],
      c"Scalatromino"
    )

    val gameControls = gtk_box_new(GtkOrientation.GTK_ORIENTATION_HORIZONTAL, 0)
    gtk_widget_set_halign(gameControls, GtkAlign.GTK_ALIGN_START)
    gtk_widget_set_valign(gameControls, GtkAlign.GTK_ALIGN_START)
    gtk_box_append(gameControls.asPtr[GtkBox], startGameButton)

    val topLevel = gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL, 0)
    gtk_widget_set_halign(topLevel, GtkAlign.GTK_ALIGN_CENTER)
    gtk_widget_set_valign(topLevel, GtkAlign.GTK_ALIGN_CENTER)

    gtk_box_append(topLevel.asPtr[GtkBox], gameControls.asPtr[GtkWidget])
    gtk_box_append(topLevel.asPtr[GtkBox], drawingArea.asPtr[GtkWidget])

    gtk_window_set_child(window.asPtr[GtkWindow], topLevel)
  }

end UILayout
