package shell.control

import game.Game

import scala.collection.mutable

type RendererId = String

class Session[G <: Game](val game: G)(
    val updater: game.Updater
):
  val renders: mutable.Map[RendererId, game.Renderer] = mutable.Map.empty

  def addRender(id: String)(renderer: game.Renderer): Unit =
    renders += id -> renderer

  var state: Option[game.State] = None

  /** Simulation time
    */
  var priorMicros: Option[Long] = None

  /** Render time
    */
  val priorRenderMicros: mutable.Map[RendererId, Long] = mutable.Map.empty

  def updateRenderMicros(id: RendererId, micros: Long): Unit =
    priorRenderMicros.update(id, micros)

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

  val inputSource: mutable.Map[String, game.Bindings] = mutable.Map.empty

  def addInputSource(name: String, bindings: game.Bindings): Unit =
    inputSource.update(name, bindings)

end Session
