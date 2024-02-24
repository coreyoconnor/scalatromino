/** Grid is: Increasing X is to the right. Increasing Y is down.
  */
object Grid:
  sealed trait Cell

  object Cell:
    case object Empty extends Cell
    case class Occupied(startTime: Long, piece: Piece) extends Cell

  def empty(width: Int, height: Int): Grid = Grid(
    states = Seq.fill(width * height)(Cell.Empty),
    width = width,
    height = height
  )
end Grid

case class Grid(
    private val states: Seq[Grid.Cell],
    width: Int,
    height: Int
) {

  type Line = Seq[Grid.Cell]

  object Line:
    def empty: Line = Seq.fill(width)(Grid.Cell.Empty)
  end Line

  def apply(x: Int, y: Int): Option[Grid.Cell] =
    if ((x < 0) || (x >= width)) then None
    else {
      if (y < 0) then Some(Grid.Cell.Empty)
      else {
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
      pieceOccupies && (this(gridX, gridY) != Some(Grid.Cell.Empty))
    }

    occupancy.contains(true)
  }

  def cannotDescend(piece: ActivePiece): Boolean =
    collides(
      PieceLayout(piece.piece, piece.rotation),
      piece.posX,
      piece.posY + 1
    )

  def fixActivePiece(startTime: Long, piece: ActivePiece): Grid = {
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
      states = toAdd.foldLeft(states) { case (s, (x, y)) =>
        val i = y * width + x
        if ((i < 0) || (i >= s.size)) then s
        else s.updated(i, Grid.Cell.Occupied(startTime, piece.piece))
      }
    )
  }

  def lines: Iterator[Line] = states.grouped(width)

  def updateLines(lines: Seq[Line]): Grid = {
    val count = height - lines.size
    val emptyLines = Seq.fill(count * width)(Grid.Cell.Empty)

    copy(
      states = emptyLines ++ lines.flatten
    )
  }

}
