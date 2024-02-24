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
