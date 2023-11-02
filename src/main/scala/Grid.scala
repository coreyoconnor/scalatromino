enum GridState:
  case Empty, Occupied

/** Grid is: Increasing X is to the right. Increasing Y is down.
 */
object Grid:
  val defaultWidth = 10
  val defaultHeight = 20

  val empty: Grid = Grid(
    states = Seq.fill(defaultWidth * defaultHeight)(GridState.Empty),
    width = defaultWidth,
    height = defaultHeight
  )
end Grid

case class Grid(
  states: Seq[GridState],
  width: Int,
  height: Int
) {
  type Line = Seq[GridState]

  object Line:
    def empty: Line = Seq.fill(width)(GridState.Empty)
  end Line

  def apply(x: Int, y: Int): Option[GridState] =
    if ((x < 0) || (x >= width)) then None else {
      if (y < 0) then Some(GridState.Empty) else {
        if (y >= height) None else Some(states(y * width + x))
      }
    }

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
        if ((i < 0) || (i >= s.size)) then s else s.updated(i, GridState.Occupied)
      }
    )
  }

  def lines: Iterator[Line] = states.grouped(width)

  def updateLines(lines: Seq[Line]): Grid = {
    val count = height - lines.size
    val emptyLines = Seq.fill(count * width)(GridState.Empty)

    copy(
      states = emptyLines ++ lines.flatten
    )
  }

}

