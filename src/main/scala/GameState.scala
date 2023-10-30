import scala.runtime.AbstractFunction4
enum GridState:
  case Empty, Occupied

case class Grid(
  states: Seq[GridState],
  width: Int,
  height: Int
)

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

object GameState:

  val tickSpeed = 2.0 // seconds per tick
  val descendSpeed = 1 // ticks per unit grid

  def init(startTime: Long) = GameState(
    ticks = 0,
    micros = startTime,
    grid = Grid.init,
    activePiece = None
  )

  def update(deltaT: Double, micros: Long, events: Seq[GameEvent], state: GameState): GameState = {
    ???
  }
end GameState

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

case class ActivePiece(
  piece: Piece,
  posX: Int,
  posY: Int,
  rotation: Rotation
)

case class GameState(
  ticks: Long,
  micros: Long,
  grid: Grid,
  activePiece: Option[ActivePiece]
)

