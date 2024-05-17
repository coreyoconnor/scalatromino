package shell

import gtk.all.*
import libcairo.all.*
import scala.scalanative.unsafe.*

trait Interactive {
  type Input
  type State

  sealed trait Event
  case class InputStart(input: Input) extends Event
  case class InputStop(input: Input) extends Event

  @FunctionalInterface
  trait Renderer {
    def apply(
        cr: Ptr[cairo_t],
        width: CInt,
        height: CInt,
        state: State,
        deltaT: Double,
        micros: Long
    ): Unit
  }

  @FunctionalInterface
  trait Updater:
    def apply(
        deltaT: Double,
        micros: Long,
        events: Seq[Event],
        state: State
    ): State

  @FunctionalInterface
  trait Bindings:
    val keyBindings: PartialFunction[Int, Input]
}
