import gio.all.*
import glib.all.*
import gtk.all.*
import gtk.fluent.*
import libcairo.all.*
import scala.scalanative.unsafe.*

class StateHolder:
  var state: Option[GameState] = None
  var priorMicros: Option[Long] = None
  var priorRenderMicros: Option[Long] = None
  val events: collection.mutable.Buffer[GameEvent] = collection.mutable.Buffer.empty
end StateHolder

@main def example =
  gtk_init()

  val activateCallback = CFuncPtr2.fromScalaFunction {
    (application: Ptr[GtkApplication], data: gpointer) =>
      val stateHolder = data.value.asPtr[StateHolder]

      val drawingArea = gtk_drawing_area_new().asPtr[GtkDrawingArea]

      val tickCallback = CFuncPtr3.fromScalaFunction {
        (widget: Ptr[GtkWidget], frameClock: Ptr[GdkFrameClock], data: gpointer) => {
          val stateHolder = data.value.asPtr[StateHolder]

          val micros = g_get_monotonic_time().value
          val deltaMicros = (!stateHolder).priorMicros match {
            case None => 20000
            case Some(priorMicros) => {
              micros - priorMicros
            }
          }
          val deltaT = deltaMicros.toDouble / 1000000.0
          (!stateHolder).priorMicros = Some(micros)

          (!stateHolder).state foreach { state =>
            val updatedState = GameState.update(
              deltaT,
              micros,
              (!stateHolder).events.toSeq,
              state
            )
            (!stateHolder).state = Some(updatedState)
          }

          (!stateHolder).events.clear()

          gtk_widget_queue_draw(widget)
          gboolean(1)
        }
      }

      gtk_widget_add_tick_callback(drawingArea.asPtr[GtkWidget],
                                   tickCallback.asInstanceOf[GtkTickCallback],
                                   data,
                                   GDestroyNotify(null))

      val drawingFunction = CFuncPtr5.fromScalaFunction {
        (drawingArea: Ptr[GtkDrawingArea], cr: Ptr[cairo_t], width: CInt, height: CInt, data: gpointer) => {
          val stateHolder = data.value.asPtr[StateHolder]

          (!stateHolder).priorMicros match {
            case None => {
              val micros = g_get_monotonic_time().value
              (!stateHolder).state foreach { state =>
                GameRenderer.render(drawingArea, cr, width, height, state, 0, micros)
              }
              (!stateHolder).priorMicros = Some(micros)
              (!stateHolder).priorRenderMicros = Some(micros)
            }
            case Some(micros) => {
              val deltaMicros = (!stateHolder).priorRenderMicros match {
                case None => 20000
                case Some(priorRenderMicros) => {
                  micros - priorRenderMicros
                }
              }
              val deltaT = deltaMicros.toDouble / 1000000.0
              (!stateHolder).priorRenderMicros = Some(micros)

              (!stateHolder).state foreach { state =>
                GameRenderer.render(drawingArea, cr, width, height, state, deltaT, micros)
              }
            }
          }
        }
      }

      gtk_drawing_area_set_draw_func(drawingArea,
                                     drawingFunction.asInstanceOf[GtkDrawingAreaDrawFunc],
                                     data, GDestroyNotify(null))

      val gameAreaKeyController = gtk_event_controller_key_new()

      val keyPressedCallback = CFuncPtr5.fromScalaFunction {
        (_: Ptr[GtkEventControllerKey], keyval: guint, _: guint, _: GdkModifierType, data: gpointer) => {
          val stateHolder = data.value.asPtr[StateHolder]
          GameInput.keyvalToInput(keyval) match {
            case Some(input) => {
              (!stateHolder).events += InputStart(input)
              gboolean(1)
            }
            case None => gboolean(0)
          }
        }
      }
      val keyReleasedCallback = CFuncPtr5.fromScalaFunction {
        (_: Ptr[GtkEventControllerKey], keyval: guint, _: guint, _: GdkModifierType, data: gpointer) => {
          val stateHolder = data.value.asPtr[StateHolder]
          GameInput.keyvalToInput(keyval) match {
            case Some(input) => {
              (!stateHolder).events += InputStop(input)
              gboolean(1)
            }
            case None => gboolean(0)
          }
        }
      }
      g_signal_connect(gameAreaKeyController, c"key-pressed", keyPressedCallback, data.value)
      g_signal_connect(gameAreaKeyController, c"key-released", keyReleasedCallback, data.value)

      gtk_event_controller_set_propagation_phase(gameAreaKeyController, GtkPropagationPhase.GTK_PHASE_CAPTURE)

      val window = gtk_application_window_new(application)
      gtk_widget_add_controller(window, gameAreaKeyController)

      val startGameButton = gtk_button_new_with_label(c"Start")

      val startGame = CFuncPtr2.fromScalaFunction {
        (widget: Ptr[GtkWidget], data: gpointer) => {
          val stateHolder = data.value.asPtr[StateHolder]
          val micros = g_get_monotonic_time().value
          (!stateHolder).state = Some(GameState.init(micros))
        }
      }

      g_signal_connect(startGameButton, c"clicked", startGame, data.value)

      UILayout.build(window, startGameButton, drawingArea)

      gtk_widget_show(window)
  }

  val app = gtk_application_new(
    c"org.gtk.example",
    GApplicationFlags.G_APPLICATION_FLAGS_NONE
  )

  val stateHolder = stackalloc[StateHolder](1)
  !stateHolder = new StateHolder

  g_signal_connect(
    app,
    c"activate",
    activateCallback,
    stateHolder.asPtr[Byte]
  )

  g_application_run(app.asPtr[GApplication], 0, null)
end example
