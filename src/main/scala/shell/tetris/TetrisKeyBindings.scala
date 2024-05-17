package shell.tetris

import game.TetrisGame
import glib.all.*

object TetrisKeyBindings extends TetrisGame.Bindings:
  val keyBindings: PartialFunction[Int, TetrisGame.Input] = {
    // a and left
    case 0x061 | 0xff51 => TetrisGame.Input.Left
    // d and right
    case 0x064 | 0xff53 => TetrisGame.Input.Right
    // s and down
    case 0x073 | 0xff54 => TetrisGame.Input.RotateCW
    // w, space, and up
    case 0x077 | 0x020 | 0xff52 => TetrisGame.Input.Drop
  }
end TetrisKeyBindings
