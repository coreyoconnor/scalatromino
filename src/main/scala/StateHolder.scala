class StateHolder:
  var state: Option[GameState] = None
  var priorMicros: Option[Long] = None
  var priorRenderMicros: Option[Long] = None
  val events: collection.mutable.Buffer[GameEvent] =
    collection.mutable.Buffer.empty
end StateHolder
