object GameState:
  val init = GameState()

  def update(deltaT: Double, micros: Long, events: Seq[GameEvent], state: GameState): GameState = {
    if (events.nonEmpty) then println(events)

    GameState()
  }
end GameState

case class GameState()

