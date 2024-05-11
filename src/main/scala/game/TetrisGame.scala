package game

object TetrisGame extends Game {
  enum Input:
    case Left, Right, RotateCW, Drop

  type State = game.tetris.GameState
}
