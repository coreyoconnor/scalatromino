package game

import shell.Interactive

object TetrisGame extends Interactive {
  enum Input:
    case Left, Right, RotateCW, Drop

  type State = game.tetris.GameState
}
