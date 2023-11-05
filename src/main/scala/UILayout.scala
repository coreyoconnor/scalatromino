import gio.all.*
import glib.all.*
import gtk.all.*
import gtk.fluent.*
import libcairo.all.*
import scala.scalanative.unsafe.*

object UILayout:
  def build(window: Ptr[GtkWidget],
            startGameButton: Ptr[GtkWidget],
            mainArea: Ptr[GtkDrawingArea],
            nextPieceArea: Ptr[GtkDrawingArea]
            ): Unit = {

    gtk_drawing_area_set_content_width(mainArea, GameRenderer.minWidth)
    gtk_drawing_area_set_content_height(mainArea, GameRenderer.minHeight)

    gtk_drawing_area_set_content_width(nextPieceArea, GameRenderer.nextPieceWidth)
    gtk_drawing_area_set_content_height(nextPieceArea, GameRenderer.nextPieceHeight)

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

    val gameArea = gtk_box_new(GtkOrientation.GTK_ORIENTATION_HORIZONTAL, 0)

    val sidebar = gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL, 0)

    val shortHelp = gtk_label_new(c"")
    gtk_label_set_markup(shortHelp.asPtr[GtkLabel], c"""
<tt>
A &#x2190;         Move left
D &#x2192;         Move right
S &#x2193;         Rotate clockwise
W &#x2191; `space` Drop
</tt>
    """)

    gtk_box_append(sidebar.asPtr[GtkBox], nextPieceArea.asPtr[GtkWidget])
    gtk_box_append(sidebar.asPtr[GtkBox], shortHelp.asPtr[GtkWidget])

    gtk_box_append(gameArea.asPtr[GtkBox], sidebar.asPtr[GtkWidget])
    gtk_box_append(gameArea.asPtr[GtkBox], mainArea.asPtr[GtkWidget])

    gtk_box_append(topLevel.asPtr[GtkBox], gameArea.asPtr[GtkWidget])
    gtk_window_set_child(window.asPtr[GtkWindow], topLevel)
  }

end UILayout
