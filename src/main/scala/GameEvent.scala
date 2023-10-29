enum GameInput:
  case Left, Right, Down, Drop

sealed trait GameEvent
case class InputStart(input: GameInput) extends GameEvent
case class InputStop(input: GameInput) extends GameEvent
