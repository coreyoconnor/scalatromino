package shell.control

import game.Game

class Session[G <: Game](val game: G)(
    val updater: game.Updater,
    val bindings: game.Bindings,
    val renderer: game.Renderer
):
  var state: Option[game.State] = None
  var priorMicros: Option[Long] = None
  var priorRenderMicros: Option[Long] = None
  val events: collection.mutable.Buffer[game.Event] =
    collection.mutable.Buffer.empty
end Session
