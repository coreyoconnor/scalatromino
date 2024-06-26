package game.tetris

import game.TetrisGame
import game.TetrisGame.{Input, InputStart, InputStop}

object GameState extends TetrisGame.Updater:
  enum Phase:
    case NewActive, MoveActive, FixActive, Score, GameOver
  val defaultWidth = 10
  val defaultHeight = 20
  val initialTickSpeed = 0.5 // seconds per tick
  val initialDescentSpeed = 1 // ticks per unit grid

  def init(startTime: Long) = GameState(
    ticks = 0,
    tickStart = startTime,
    tickSpeed = initialTickSpeed,
    descentSpeed = initialDescentSpeed,
    grid = Grid.empty(defaultWidth, defaultHeight),
    activePiece = None,
    nextPiece = randomPiece,
    phase = Phase.NewActive
  )

  def randomPiece: Piece = scala.util.Random.shuffle(Piece.values).head

  def apply(
      deltaT: Double,
      micros: Long,
      events: Seq[TetrisGame.Event],
      state: GameState
  ): GameState = {
    state.phase match {
      case Phase.NewActive => {
        val piece = Piece.Active(state.nextPiece, 4, 0, Piece.Rotation.CW0)

        if (state.grid.collides(piece)) {
          state.copy(phase = Phase.GameOver)
        } else {
          state.copy(
            activePiece = Some(piece),
            nextPiece = randomPiece,
            phase = Phase.MoveActive
          )
        }
      }

      case Phase.MoveActive =>
        if (state.tickTime(micros) >= state.tickSpeed) {
          val activePiece = state.activePiece.get
          if (state.grid.cannotDescend(activePiece)) {
            state.copy(
              phase = Phase.FixActive,
              ticks = state.ticks + 1,
              tickStart = micros
            )
          } else {
            state.copy(
              activePiece = Some(activePiece.copy(posY = activePiece.posY + 1)),
              ticks = state.ticks + 1,
              tickStart = micros
            )
          }
        } else {
          val activePiece = state.activePiece.get

          val newRotation = events.foldLeft(activePiece.rotation) {
            case (Piece.Rotation.CW0, InputStart(Input.RotateCW)) => Piece.Rotation.CW1
            case (Piece.Rotation.CW1, InputStart(Input.RotateCW)) => Piece.Rotation.CW2
            case (Piece.Rotation.CW2, InputStart(Input.RotateCW)) => Piece.Rotation.CW3
            case (Piece.Rotation.CW3, InputStart(Input.RotateCW)) => Piece.Rotation.CW0
            case (rotation, _)                              => rotation
          }

          val newPosX = events.foldLeft(activePiece.posX) {
            case (posX, InputStart(Input.Left))  => posX - 1
            case (posX, InputStart(Input.Right)) => posX + 1
            case (posX, _)                       => posX
          }

          val newPosY = events.foldLeft(activePiece.posY) {
            case (posY, InputStart(Input.Drop)) => {
              val (_, maxY) =
                (posY until state.grid.height).foldLeft((true, posY)) {
                  case ((priorFit, maxY), probeY) =>
                    if (
                      priorFit && !state.grid.collides(
                        activePiece.copy(posY = probeY)
                      )
                    ) then {
                      (true, probeY)
                    } else {
                      (false, maxY)
                    }
                }

              maxY
            }
            case (posY, _) => posY
          }

          val updatedActivePiece =
            activePiece.copy(
              rotation = newRotation,
              posX = newPosX,
              posY = newPosY
            )

          if (state.grid.collides(updatedActivePiece)) then state
          else {
            state.copy(activePiece = Some(updatedActivePiece))
          }
        }

      case Phase.FixActive => state.fixActivePiece(micros)

      case Phase.Score => {
        val lines = state.grid.lines
        val updatedLinesRev = lines.foldLeft(Seq.empty[state.grid.Line]) {
          (outLines, line) =>
            if (line.forall(_ != Grid.Cell.Empty)) {
              outLines
            } else {
              line +: outLines
            }
        }
        val updatedLines = updatedLinesRev.reverse

        state.copy(
          grid = state.grid.updateLines(updatedLines),
          phase = Phase.NewActive
        )
      }

      case Phase.GameOver => state
    }
  }
end GameState

case class GameState(
    ticks: Long,
    tickStart: Long,
    tickSpeed: Double,
    descentSpeed: Double,
    grid: Grid,
    activePiece: Option[Piece.Active],
    nextPiece: Piece,
    phase: GameState.Phase
) {
  def tickTime(micros: Long): Double = (micros - tickStart).toDouble / 1000000.0

  def fixActivePiece(time: Long): GameState =
    activePiece match {
      case None => this
      case Some(ap) =>
        copy(
          grid = grid.fixActivePiece(time, ap),
          activePiece = None,
          phase = GameState.Phase.Score
        )
    }

  def isActivePieceDescending: Boolean =
    activePiece.map(grid.cannotDescend).exists(_ == false)
}
