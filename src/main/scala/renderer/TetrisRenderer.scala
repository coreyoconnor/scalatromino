package renderer

import game.TetrisGame
import game.tetris._

import libcairo.all.*
import scala.scalanative.unsafe.*

object TetrisRenderer:
  val minHeight = 768
  val minWidth = minHeight * 9 / 18

  val nextPieceWidth = 100
  val nextPieceHeight = 200

  type Color = (Double, Double, Double)

  def pulse(micros: Long, low: Color, high: Color): Color = {
    val period = 8.0

    val t = micros.toDouble / 1000000.0
    val v1 = ((scala.math.cos(
      2.0 * math.Pi * t / period
    ) + 1.0) / 2.0 * (high._1 - low._1) + low._1)
    val v2 = ((scala.math.cos(
      2.0 * math.Pi * t / period
    ) + 1.0) / 2.0 * (high._2 - low._2) + low._2)
    val v3 = ((scala.math.cos(
      2.0 * math.Pi * t / period
    ) + 1.0) / 2.0 * (high._3 - low._3) + low._3)

    (v1, v2, v3)
  }

  case class ColorScheme(
      background: Color,
      backgroundGrid: Color,
      activeOutline: Color,
      activeFill: Color,
      gridOutline: Color,
      gridFill: Color
  )

  def colorScheme(micros: Long, phase: GameState.Phase): ColorScheme =
    phase match {
      case GameState.Phase.GameOver =>
        ColorScheme(
          background = pulse(micros, (0.8, 0.5, 0.5), (0.9, 0.55, 0.55)),
          backgroundGrid = (0.0, 0.0, 0.0),
          activeOutline = (0.2, 0.2, 0.2),
          activeFill = (0.1, 0.1, 0.1),
          gridOutline = (0.8, 0.8, 0.9),
          gridFill = pulse(micros, (0.8, 0.8, 0.5), (0.9, 0.55, 0.55))
        )
      case _ =>
        ColorScheme(
          background = pulse(micros, (0.25, 0.25, 0.25), (0.28, 0.28, 0.28)),
          backgroundGrid = (0.3, 0.3, 0.3),
          activeOutline = (0.9, 0.9, 0.9),
          activeFill = pulse(micros, (0.1, 0.7, 0.2), (0.1, 0.75, 0.25)),
          gridOutline = (0.0, 0.6562, 0.4843),
          gridFill =
            pulse(micros, (0.65625, 0.0, 0.1679), (0.75625, 0.0, 0.2679))
        )
    }

  val render: TetrisGame.Renderer = {
    (
        cr: Ptr[cairo_t],
        width: CInt,
        height: CInt,
        state: GameState,
        deltaT: Double,
        micros: Long
    ) =>

      val cs = colorScheme(micros, state.phase)

      cairo_set_source_rgb(
        cr,
        cs.background._1,
        cs.background._2,
        cs.background._3
      )
      cairo_rectangle(cr, 0, 0, width, height)
      cairo_fill(cr)

      val pieceSize = width / state.grid.width

      for {
        y <- 0 until state.grid.height
        x <- 0 until state.grid.width
      } {
        if (state.grid(x, y).exists(_ != Grid.Cell.Empty)) {
          cairo_set_source_rgb(
            cr,
            cs.gridFill._1,
            cs.gridFill._2,
            cs.gridFill._3
          )
          cairo_rectangle(
            cr,
            x * pieceSize,
            y * pieceSize,
            pieceSize,
            pieceSize
          )
          cairo_fill(cr)
          cairo_set_source_rgb(
            cr,
            cs.gridOutline._1,
            cs.gridOutline._2,
            cs.gridOutline._3
          )
          cairo_set_line_width(cr, 2.0)
          cairo_rectangle(
            cr,
            x * pieceSize,
            y * pieceSize,
            pieceSize,
            pieceSize
          )
          cairo_stroke(cr)
        } else {
          cairo_set_source_rgb(
            cr,
            cs.backgroundGrid._1,
            cs.backgroundGrid._2,
            cs.backgroundGrid._3
          )
          cairo_set_line_width(cr, 1.0)
          cairo_rectangle(
            cr,
            x * pieceSize,
            y * pieceSize,
            pieceSize,
            pieceSize
          )
          cairo_stroke(cr)
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

            cairo_set_source_rgb(
              cr,
              cs.activeFill._1,
              cs.activeFill._2,
              cs.activeFill._3
            )
            cairo_rectangle(
              cr,
              outX * pieceSize,
              outY * pieceSize,
              pieceSize,
              pieceSize
            )
            cairo_fill(cr)
            cairo_set_source_rgb(
              cr,
              cs.activeOutline._1,
              cs.activeOutline._2,
              cs.activeOutline._3
            )
            cairo_set_line_width(cr, 2.0)
            cairo_rectangle(
              cr,
              outX * pieceSize,
              outY * pieceSize,
              pieceSize,
              pieceSize
            )
            cairo_stroke(cr)
          }
        }
      }
  }

  val renderNextPiece: TetrisGame.Renderer = {
    (
        cr: Ptr[cairo_t],
        width: CInt,
        height: CInt,
        state: GameState,
        deltaT: Double,
        micros: Long
    ) =>
      val cs = colorScheme(0, state.phase)

      cairo_set_source_rgb(
        cr,
        cs.background._1,
        cs.background._2,
        cs.background._3
      )
      cairo_rectangle(cr, 0, 0, width, height)
      cairo_fill(cr)

      val pieceSize = width / 6

      val layout = PieceLayout(state.nextPiece, Rotation.CW0)

      cairo_translate(cr, pieceSize * 3, pieceSize * 2)

      for {
        y <- 0 until layout.height
        x <- 0 until layout.width
      } {
        if (layout(x, y)) {
          val outX = x - layout.centerX
          val outY = y - layout.centerY

          cairo_set_source_rgb(
            cr,
            cs.activeFill._1,
            cs.activeFill._2,
            cs.activeFill._3
          )
          cairo_rectangle(
            cr,
            outX * pieceSize,
            outY * pieceSize,
            pieceSize,
            pieceSize
          )
          cairo_fill(cr)
          cairo_set_source_rgb(
            cr,
            cs.activeOutline._1,
            cs.activeOutline._2,
            cs.activeOutline._3
          )
          cairo_set_line_width(cr, 2.0)
          cairo_rectangle(
            cr,
            outX * pieceSize,
            outY * pieceSize,
            pieceSize,
            pieceSize
          )
          cairo_stroke(cr)
        }
      }
  }

end TetrisRenderer
