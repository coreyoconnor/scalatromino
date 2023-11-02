import glib.all.*

enum GameInput:
  case Left, Right, RotateCW, Drop

object GameInput:
  val keybindings: PartialFunction[Int, GameInput] = {
    // a and left
    case 0x061 | 0x8fb => GameInput.Left
    // d and right
    case 0x064 | 0x8fd => GameInput.Right
    // s and down
    case 0x073 | 0x8fe => GameInput.RotateCW
    // w, space, and up
    case 0x077 | 0x020 | 0x8fc => GameInput.Drop
  }

  def keyvalToInput(keyval: guint): Option[GameInput] = {
    keybindings.lift(keyval.value.toInt)
  }
end GameInput

