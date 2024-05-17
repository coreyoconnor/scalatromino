package shell

import scala.collection.mutable

type ControllerId = String
type RendererId = String

class Session[I <: Interactive](val interactive: I)(
    val updater: interactive.Updater
):
  val renders: mutable.Map[RendererId, interactive.Renderer] = mutable.Map.empty

  def addRender(id: String)(renderer: interactive.Renderer): Unit =
    renders += id -> renderer

  var state: Option[interactive.State] = None

  /** Simulation time
    */
  var priorMicros: Option[Long] = None

  /** Render time
    */
  val priorRenderMicros: mutable.Map[RendererId, Long] = mutable.Map.empty

  def updateRenderMicros(id: RendererId, micros: Long): Unit =
    priorRenderMicros.update(id, micros)

  val events: collection.mutable.Buffer[interactive.Event] =
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

  val inputSource: mutable.Map[ControllerId, interactive.Bindings] = mutable.Map.empty

  def addInputSource(id: ControllerId, bindings: interactive.Bindings): Unit =
    inputSource.update(id, bindings)

  val currentInputState: mutable.Map[(ControllerId, interactive.Input), interactive.Event] =
    mutable.Map.empty

  def emitInputStart(id: ControllerId, input: interactive.Input): Unit = {
    val event = interactive.InputStart(input)
    currentInputState.get((id, input)) match {
      case None                              => events += event
      case Some(current) if current != event => events += event
      case Some(_)                           => ()
    }
    currentInputState.update((id, input), event)
  }

  def emitInputStop(id: ControllerId, input: interactive.Input): Unit = {
    val event = interactive.InputStop(input)
    currentInputState.get((id, input)) match {
      case None                              => events += event
      case Some(current) if current != event => events += event
      case Some(_)                           => ()
    }
    currentInputState.update((id, input), event)
  }

end Session
