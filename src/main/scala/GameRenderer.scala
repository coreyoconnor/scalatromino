import glib.all.*
import gtk.all.*
import gtk.fluent.*
import libcairo.all.*
import scala.scalanative.unsafe.*

def grayPulse(micros: Long): (Double, Double, Double) = {
  val min = 0.35
  val max = 0.50
  val period = 10.0

  val t = micros.toDouble / 1000000.0
  val v = 1.0 - ((scala.math.cos(2.0 * math.Pi * t / period) + 1.0)/2.0 * (max - min) + min)

  (v, v, v)
}

object GameRenderer:
  def render(drawingArea: Ptr[GtkDrawingArea],
             cr: Ptr[cairo_t],
             width: CInt, height: CInt,
             state: GameState,
             deltaT: Double,
             micros: Long): Unit = {

    val color = grayPulse(micros)
    cairo_set_source_rgb(cr, color._1, color._2, color._3)
    cairo_rectangle(cr, 0, 0, width, height)
    cairo_fill(cr)

    for {
      y <- 0 until state.grid.height
      x <- 0 until state.grid.width
    } {
      if (state.grid(x, y).exists(_ != GridState.Empty)) {
        cairo_set_source_rgb(cr, 0, 0, 0)
        cairo_rectangle(cr, x * 10, y * 10, 10, 10)
        cairo_fill(cr)
      }
    }

    state.activePiece foreach { activePiece =>
      val layout = PieceLayout(activePiece.piece, activePiece.rotation)

      for {
        y <- 0 until layout.height
        x <- 0 until layout.width
      } {
        if (layout(x, y)) {
          val outX = activePiece.posX + x - layout.centerX
          val outY = activePiece.posY + y - layout.centerY

          cairo_set_source_rgb(cr, 1, 0, 0)
          cairo_rectangle(cr, outX * 10, outY * 10, 10, 10)
          cairo_fill(cr)
        }
      }
    }
  }
end GameRenderer
