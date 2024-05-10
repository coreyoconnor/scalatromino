package shell.control

class Session[S, E](val updater: StateUpdater[S, E]):
  var state: Option[S] = None
  var priorMicros: Option[Long] = None
  var priorRenderMicros: Option[Long] = None
  val events: collection.mutable.Buffer[E] = collection.mutable.Buffer.empty
end Session