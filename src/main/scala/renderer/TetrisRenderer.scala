package renderer

import game.TetrisGame
import game.tetris._

import sn.gnome.cairo.internal.*
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
      backgrounds: IndexedSeq[Color],
      backgroundGrid: Color,
      activeOutline: Color,
      activeFill: Color,
      gridOutline: Color,
      gridFills: IndexedSeq[Color]
  )

  def colorScheme(micros: Long, phase: GameState.Phase): ColorScheme =
    phase match {
      case GameState.Phase.GameOver =>
        ColorScheme(
          backgrounds = IndexedSeq(
            pulse(micros, (0.8, 0.5, 0.5), (0.9, 0.55, 0.55)),
            pulse(micros * 2, (0.9, 0.5, 0.5), (0.9, 0.55, 0.55))
          ),
          backgroundGrid = (0.0, 0.0, 0.0),
          activeOutline = (0.2, 0.2, 0.2),
          activeFill = (0.1, 0.1, 0.1),
          gridOutline = (0.8, 0.8, 0.9),
          gridFills = IndexedSeq(
            pulse(micros, (0.8, 0.8, 0.5), (0.9, 0.55, 0.55)),
            pulse(micros, (0.7, 0.7, 0.5), (0.8, 0.55, 0.55))
          )
        )
      case _ =>
        ColorScheme(
          backgrounds = IndexedSeq(
            pulse(micros, (0.25, 0.25, 0.25), (0.28, 0.28, 0.28)),
            pulse(micros * 2, (0.3, 0.3, 0.3), (0.32, 0.32, 0.32))
          ),
          backgroundGrid = (0.3, 0.3, 0.3),
          activeOutline = (0.9, 0.9, 0.9),
          activeFill = pulse(micros, (0.1, 0.7, 0.2), (0.1, 0.75, 0.25)),
          gridOutline = (0.0, 0.6562, 0.4843),
          gridFills = IndexedSeq(
            pulse(micros, (0.55625, 0.0, 0.1679), (0.65625, 0.0, 0.2679)),
            pulse(micros, (0.65625, 0.1, 0.1679), (0.75625, 0.2, 0.2679))
          )
        )
    }

  def outlinedBlock(cr: Ptr[cairo_t], gx: Int, gy: Int, pieceSize: Int, gridColor: Color, fillColor: Color): Unit = {
      cairo_set_source_rgb(
        cr,
        fillColor._1,
        fillColor._2,
        fillColor._3
      )
      cairo_rectangle(
        cr,
        gx * pieceSize,
        gy * pieceSize,
        pieceSize,
        pieceSize
      )
      cairo_fill(cr)
      cairo_set_source_rgb(
        cr,
        gridColor._1,
        gridColor._2,
        gridColor._3
      )
      cairo_set_line_width(cr, 2.5)
      cairo_rectangle(
        cr,
        gx * pieceSize,
        gy * pieceSize,
        pieceSize,
        pieceSize
      )
      cairo_stroke(cr)
  }

  val render: TetrisGame.Renderer = { (cr, width, height, state, deltaT, micros) =>

      val cs = colorScheme(micros, state.phase)

      cairo_set_source_rgb(
        cr,
        cs.backgrounds.head._1,
        cs.backgrounds.head._2,
        cs.backgrounds.head._3
      )
      cairo_rectangle(cr, 0, 0, width, height)
      cairo_fill(cr)

      val pieceSize = width / state.grid.width

      for {
        gy <- 0 until state.grid.height
        gx <- 0 until state.grid.width
      } {
        if (state.grid(gx, gy).exists(_ != Grid.Cell.Empty)) {
          outlinedBlock(cr, gx, gy, pieceSize, cs.gridOutline, cs.gridFills(gx % 2))
        } else {
          outlinedBlock(cr, gx, gy, pieceSize, cs.backgroundGrid, cs.backgrounds(gy % 2))
        }
      }

      state.activePiece foreach { activePiece =>
        val layout = PieceLayout(activePiece.piece, activePiece.rotation)

        for {
          gy <- 0 until layout.height
          gx <- 0 until layout.width
        } {
          if (layout(gx, gy)) {
            val outX = activePiece.posX + gx - layout.centerX
            val outY = activePiece.posY + gy - layout.centerY
            outlinedBlock(cr, outX, outY, pieceSize, cs.activeOutline, cs.activeFill)
          }
        }
      }
  }

  val renderNextPiece: TetrisGame.Renderer = { (cr,width,height,state,deltaT,micros) =>
      val cs = colorScheme(0, state.phase)

      cairo_set_source_rgb(
        cr,
        cs.backgrounds.head._1,
        cs.backgrounds.head._2,
        cs.backgrounds.head._3
      )
      cairo_rectangle(cr, 0, 0, width, height)
      cairo_fill(cr)

      val pieceSize = width / 6

      val layout = PieceLayout(state.nextPiece, Piece.Rotation.CW0)

      cairo_translate(cr, pieceSize * 3, pieceSize * 2)

      for {
        gy <- 0 until layout.height
        gx <- 0 until layout.width
      } {
        if (layout(gx, gy)) {
          val outX = gx - layout.centerX
          val outY = gy - layout.centerY
          outlinedBlock(cr, outX, outY, pieceSize, cs.activeOutline, cs.activeFill)
        }
      }
  }

end TetrisRenderer
