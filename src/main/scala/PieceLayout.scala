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
      piece <- Seq(
        Piece.I,
        Piece.O,
        Piece.T,
        Piece.S,
        Piece.Z,
        Piece.J,
        Piece.L
      )
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
      grid = desc.stripMargin.filterNot(_.isWhitespace).map(_ != '.'),
      centerX = 2,
      centerY = 2
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
