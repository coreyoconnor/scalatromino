object GameState:
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
    phase = GamePhase.NewActive
  )

  def randomPiece: Piece = scala.util.Random.shuffle(Piece.values).head

  def update(
      deltaT: Double,
      micros: Long,
      events: Seq[GameEvent],
      state: GameState
  ): GameState = {
    state.phase match {
      case GamePhase.NewActive => {
        val piece = ActivePiece(state.nextPiece, 4, 0, Rotation.CW0)

        if (state.grid.collides(piece)) {
          state.copy(phase = GamePhase.GameOver)
        } else {
          state.copy(
            activePiece = Some(piece),
            nextPiece = randomPiece,
            phase = GamePhase.MoveActive
          )
        }
      }

      case GamePhase.MoveActive =>
        if (state.tickTime(micros) >= state.tickSpeed) {
          val activePiece = state.activePiece.get
          if (state.grid.cannotDescend(activePiece)) {
            state.copy(
              phase = GamePhase.FixActive,
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
            case (Rotation.CW0, InputStart(GameInput.RotateCW)) => Rotation.CW1
            case (Rotation.CW1, InputStart(GameInput.RotateCW)) => Rotation.CW2
            case (Rotation.CW2, InputStart(GameInput.RotateCW)) => Rotation.CW3
            case (Rotation.CW3, InputStart(GameInput.RotateCW)) => Rotation.CW0
            case (rotation, _)                                  => rotation
          }

          val newPosX = events.foldLeft(activePiece.posX) {
            case (posX, InputStart(GameInput.Left))  => posX - 1
            case (posX, InputStart(GameInput.Right)) => posX + 1
            case (posX, _)                           => posX
          }

          val newPosY = events.foldLeft(activePiece.posY) {
            case (posY, InputStart(GameInput.Drop)) => {
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

      case GamePhase.FixActive => state.fixActivePiece(micros)

      case GamePhase.Score => {
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
          phase = GamePhase.NewActive
        )
      }

      case GamePhase.GameOver => state
    }
  }
end GameState

case class GameState(
    ticks: Long,
    tickStart: Long,
    tickSpeed: Double,
    descentSpeed: Double,
    grid: Grid,
    activePiece: Option[ActivePiece],
    nextPiece: Piece,
    phase: GamePhase
) {
  def tickTime(micros: Long): Double = (micros - tickStart).toDouble / 1000000.0

  def fixActivePiece(time: Long): GameState =
    activePiece match {
      case None => this
      case Some(ap) =>
        copy(
          grid = grid.fixActivePiece(time, ap),
          activePiece = None,
          phase = GamePhase.Score
        )
    }

  def isActivePieceDescending: Boolean =
    activePiece.map(grid.cannotDescend).exists(_ == false)
}

enum GamePhase:
  case NewActive, MoveActive, FixActive, Score, GameOver
