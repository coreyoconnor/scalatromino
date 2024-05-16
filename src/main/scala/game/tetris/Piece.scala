package game.tetris

object Piece:
  case class Active(
      piece: Piece,
      posX: Int,
      posY: Int,
      rotation: Rotation
  )

  enum Rotation:
    case CW0, CW1, CW2, CW3

enum Piece:
  case I, O, T, S, Z, J, L
