package shell.control

import game.Game

class Session[G <: Game](val game: G)(
    val updater: game.Updater,
    val bindings: game.Bindings
):

  var state: Option[game.State] = None

  /** Simulation time
    */
  var priorMicros: Option[Long] = None

  /** Render time
    */
  var priorRenderMicros: Option[Long] = None

  val events: collection.mutable.Buffer[game.Event] =
    collection.mutable.Buffer.empty

  def update(
      deltaT: Double,
      micros: Long
  ): Unit = {
    state = state.map { s =>
      updater(
        deltaT,
        micros,
        events.toSeq,
        s
      )
    }
  }
end Session
