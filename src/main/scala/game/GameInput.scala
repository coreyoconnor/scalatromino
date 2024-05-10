package game

import glib.all.*

enum GameInput:
  case Left, Right, RotateCW, Drop

object GameInput:
  val keybindings: PartialFunction[Int, GameInput] = {
    // a and left
    case 0x061 | 0xff51 => GameInput.Left
    // d and right
    case 0x064 | 0xff53 => GameInput.Right
    // s and down
    case 0x073 | 0xff54 => GameInput.RotateCW
    // w, space, and up
    case 0x077 | 0x020 | 0xff52 => GameInput.Drop
  }

  def keyvalToInput(keyval: guint): Option[GameInput] = {
    keybindings.lift(keyval.value.toInt)
  }
end GameInput
