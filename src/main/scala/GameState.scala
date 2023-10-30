/** Grid is: Increasing X is to the right. Increasing Y is down.
 */
object Grid:
  val defaultWidth = 10
  val defaultHeight = 20

  val init: Grid = Grid(
    states = Seq.fill(defaultWidth * defaultHeight)(GridState.Empty),
    width = defaultWidth,
    height = defaultHeight
  )
end Grid

enum GridState:
  case Empty, Occupied

case class Grid(
  states: Seq[GridState],
  width: Int,
  height: Int
) {
  def collides(piece: ActivePiece): Boolean =
    collides(PieceLayout(piece.piece, piece.rotation), piece.posX, piece.posY)

  def collides(layout: PieceLayout, posX: Int, posY: Int): Boolean = {
    val occupancy = for {
      y <- 0 until layout.height
      x <- 0 until layout.width
    } yield {
      val pieceOccupies = layout(x, y)
      val gridX = x + posX - layout.centerX
      val gridY = y + posY - layout.centerY
      pieceOccupies && (this(gridX, gridY) != Some(GridState.Empty))
    }

    occupancy.contains(true)
  }

  def apply(x: Int, y: Int): Option[GridState] =
    if ((x < 0) || (x >= width)) then None else {
      if (y < 0) then Some(GridState.Empty) else {
        if (y >= height) None else Some(states(y * width + x))
      }
    }

  def cannotDescend(piece: ActivePiece): Boolean =
    collides(PieceLayout(piece.piece, piece.rotation), piece.posX, piece.posY + 1)

  def fixActivePiece(piece: ActivePiece): Grid = {
    val layout = PieceLayout(piece.piece, piece.rotation)
    val toAdd = for {
      y <- 0 until layout.height
      x <- 0 until layout.width
      if layout(x, y)
    } yield {
      val gridX = x + piece.posX - layout.centerX
      val gridY = y + piece.posY - layout.centerY
      (gridX, gridY)
    }

    copy(
      states = toAdd.foldLeft(states){ case (s, (x, y)) =>
        val i = y * width + x
        if ((i < 0) || (i >= s.size)) then s else s.updated(y * width + x, GridState.Occupied)
      }
    )
  }
}

object GameState:

  val tickSpeed = 2.0 // seconds per tick
  val descendSpeed = 1 // ticks per unit grid

  def init(startTime: Long) = GameState(
    ticks = 0,
    tickStart = startTime,
    grid = Grid.init,
    activePiece = None,
    phase = GamePhase.NewActive
  )

  def update(deltaT: Double, micros: Long, events: Seq[GameEvent], state: GameState): GameState = {
    state.phase match {
      case GamePhase.NewActive => {
        val piece = ActivePiece(Piece.S, 4, 0, Rotation.CW0)

        if (state.grid.collides(piece)) {
          state.copy(phase = GamePhase.GameOver)
        } else {
          state.copy(activePiece = Some(piece), phase = GamePhase.MoveActive)
        }
      }

      case GamePhase.MoveActive =>
        if (state.tickTime(micros) >= tickSpeed) {
          val activePiece = state.activePiece.get
          if (state.grid.cannotDescend(activePiece)) {
            state.copy(phase = GamePhase.FixActive, ticks = state.ticks + 1, tickStart = micros)
          } else {
            state.copy(
              activePiece = Some(activePiece.copy(posY = activePiece.posY + 1)),
              ticks = state.ticks + 1,
              tickStart = micros
            )
          }
        } else {
          state
        }

      case GamePhase.FixActive => state.fixActivePiece

      case GamePhase.GameOver => state
    }
  }
end GameState

case class GameState(
  ticks: Long,
  tickStart: Long,
  grid: Grid,
  activePiece: Option[ActivePiece],
  phase: GamePhase
) {
  def tickTime(micros: Long): Double = (micros - tickStart).toDouble / 1000000.0

  def fixActivePiece: GameState =
    activePiece match {
      case None => this
      case Some(ap) =>
        copy(
          grid = grid.fixActivePiece(ap),
          activePiece = None,
          phase = GamePhase.NewActive
        )
    }

  def isActivePieceDescending: Boolean = activePiece.map(grid.cannotDescend).exists(_ == false)
}

enum GamePhase:
  case NewActive, MoveActive, FixActive, GameOver

case class ActivePiece(
  piece: Piece,
  posX: Int,
  posY: Int,
  rotation: Rotation
)

enum Piece:
  case I, O, T, S, Z, J, L

enum Rotation:
  case CW0, CW1, CW2, CW3

case class PieceLayout(
  grid: Seq[Boolean],
  centerX: Int,
  centerY: Int
) {
  val width = 4
  val height = 4

  def apply(x: Int, y: Int): Boolean = grid(y * width + x)
}

object PieceLayout:
  val allLayouts: Map[(Piece, Rotation), PieceLayout] = {
    for {
      piece <- Seq(Piece.I, Piece.O, Piece.T, Piece.S, Piece.Z, Piece.J, Piece.L)
      rotation <- Seq(Rotation.CW0, Rotation.CW1, Rotation.CW2, Rotation.CW3)
      desc = piece match {
        case Piece.I => layoutI(rotation)
        case Piece.O => layoutO(rotation)
        case Piece.T => layoutT(rotation)
        case Piece.S => layoutS(rotation)
        case Piece.Z => layoutZ(rotation)
        case Piece.J => layoutJ(rotation)
        case Piece.L => layoutL(rotation)
      }
    } yield (piece, rotation) -> PieceLayout(desc)
  }.toMap

  def apply(piece: Piece, rot: Rotation): PieceLayout = allLayouts((piece, rot))

  type Desc = String

  def apply(desc: Desc): PieceLayout =
    PieceLayout(
      grid = desc.filterNot(_.isSpaceChar).map(_ != '.'),
      centerX = 2,
      centerY = 2,
    )

  def layoutI(rot: Rotation): Desc =
    rot match {
      case Rotation.CW0 | Rotation.CW2 => """
        |....
        |....
        |IIII
        |....
        |"""

      case Rotation.CW1 | Rotation.CW3 => """
        |..I.
        |..I.
        |..I.
        |..I.
        |"""
    }

  def layoutO(rot: Rotation): Desc = """
      |....
      |.OO.
      |.OO.
      |....
      |"""

  def layoutT(rot: Rotation): Desc =
    rot match {
      case Rotation.CW0 => """
        |....
        |.T..
        |TTT.
        |....
        |"""
      case Rotation.CW1 => """
        |....
        |.T..
        |.TT.
        |.T..
        |"""
      case Rotation.CW2 => """
        |....
        |....
        |TTT.
        |.T..
        |"""
      case Rotation.CW3 => """
        |....
        |.T..
        |TT..
        |.T..
        |"""
    }

  def layoutS(rot: Rotation): Desc =
    rot match {
      case Rotation.CW0 | Rotation.CW2 => """
        |....
        |..SS
        |.SS.
        |....
        |"""
      case Rotation.CW1 | Rotation.CW3 => """
        |....
        |.S..
        |.SS.
        |..S.
        |"""
    }

  def layoutZ(rot: Rotation): Desc =
    rot match {
      case Rotation.CW0 | Rotation.CW2 => """
        |....
        |.ZZ.
        |..ZZ
        |....
        |"""
      case Rotation.CW1 | Rotation.CW3 => """
        |....
        |..Z.
        |.ZZ.
        |.Z..
        |"""
    }
  def layoutJ(rot: Rotation): Desc =
    rot match {
      case Rotation.CW0 => """
        |....
        |.J..
        |.JJJ
        |....
        |"""
      case Rotation.CW1 => """
        |....
        |..JJ
        |..J.
        |..J.
        |"""
      case Rotation.CW2 => """
        |....
        |....
        |.JJJ
        |...J
        |"""
      case Rotation.CW3 => """
        |....
        |..J.
        |..J.
        |.JJ.
        |"""
    }

  def layoutL(rot: Rotation): Desc =
    rot match {
      case Rotation.CW0 => """
        |....
        |...L
        |.LLL
        |....
        |"""
      case Rotation.CW1 => """
        |....
        |..L.
        |..L.
        |..LL
        |"""
      case Rotation.CW2 => """
        |....
        |....
        |.LLL
        |.L..
        |"""
      case Rotation.CW3 => """
        |....
        |.LL.
        |..L.
        |..L.
        |"""
    }

end PieceLayout

