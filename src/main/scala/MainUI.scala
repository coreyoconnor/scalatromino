import gio.all.*
import glib.all.*
import gtk.all.*
import gtk.fluent.*
import libcairo.all.*
import scala.scalanative.unsafe.*

object MainUI:
  val activate = CFuncPtr2.fromScalaFunction {
    (application: Ptr[GtkApplication], data: gpointer) =>
      val stateHolder = data.value.asPtr[StateHolder]

      val mainArea = gtk_drawing_area_new().asPtr[GtkDrawingArea]
      val nextPieceArea = gtk_drawing_area_new().asPtr[GtkDrawingArea]

      val refreshWidget = CFuncPtr3.fromScalaFunction {
        (widget: Ptr[GtkWidget], _: Ptr[GdkFrameClock], _: gpointer) =>
          {
            gtk_widget_queue_draw(widget)
            gboolean(1)
          }
      }

      gtk_widget_add_tick_callback(
        mainArea.asPtr[GtkWidget],
        refreshWidget.asInstanceOf[GtkTickCallback],
        gpointer(null),
        GDestroyNotify(null)
      )

      gtk_widget_add_tick_callback(
        nextPieceArea.asPtr[GtkWidget],
        refreshWidget.asInstanceOf[GtkTickCallback],
        gpointer(null),
        GDestroyNotify(null)
      )

      val drawMainArea = CFuncPtr5.fromScalaFunction {
        (
            mainArea: Ptr[GtkDrawingArea],
            cr: Ptr[cairo_t],
            width: CInt,
            height: CInt,
            data: gpointer
        ) =>
          {
            val stateHolder = data.value.asPtr[StateHolder]

            (!stateHolder).priorMicros match {
              case None => {
                val micros = g_get_monotonic_time().value
                (!stateHolder).state foreach { state =>
                  GameRenderer.render(
                    mainArea,
                    cr,
                    width,
                    height,
                    state,
                    0,
                    micros
                  )
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
                  GameRenderer.render(
                    mainArea,
                    cr,
                    width,
                    height,
                    state,
                    deltaT,
                    micros
                  )
                }
              }
            }
          }
      }

      val drawNextPiece = CFuncPtr5.fromScalaFunction {
        (
            nextPiece: Ptr[GtkDrawingArea],
            cr: Ptr[cairo_t],
            width: CInt,
            height: CInt,
            data: gpointer
        ) =>
          {
            val stateHolder = data.value.asPtr[StateHolder]

            (!stateHolder).state foreach { state =>
              GameRenderer.renderNextPiece(nextPiece, cr, width, height, state)
            }
          }
      }

      gtk_drawing_area_set_draw_func(
        mainArea,
        drawMainArea.asInstanceOf[GtkDrawingAreaDrawFunc],
        data,
        GDestroyNotify(null)
      )

      gtk_drawing_area_set_draw_func(
        nextPieceArea,
        drawNextPiece.asInstanceOf[GtkDrawingAreaDrawFunc],
        data,
        GDestroyNotify(null)
      )

      val gameAreaKeyController = gtk_event_controller_key_new()

      val keyPressedCallback = CFuncPtr5.fromScalaFunction {
        (
            _: Ptr[GtkEventControllerKey],
            keyval: guint,
            _: guint,
            _: GdkModifierType,
            data: gpointer
        ) =>
          {
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
        (
            _: Ptr[GtkEventControllerKey],
            keyval: guint,
            _: guint,
            _: GdkModifierType,
            data: gpointer
        ) =>
          {
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
      g_signal_connect(
        gameAreaKeyController,
        c"key-pressed",
        keyPressedCallback,
        data.value
      )
      g_signal_connect(
        gameAreaKeyController,
        c"key-released",
        keyReleasedCallback,
        data.value
      )

      gtk_event_controller_set_propagation_phase(
        gameAreaKeyController,
        GtkPropagationPhase.GTK_PHASE_CAPTURE
      )

      val window = gtk_application_window_new(application)
      gtk_widget_add_controller(window, gameAreaKeyController)

      gtk_widget_add_tick_callback(
        window.asPtr[GtkWidget],
        UpdateStateHolder.tick.asInstanceOf[GtkTickCallback],
        data,
        GDestroyNotify(null)
      )

      val startGameButton = gtk_button_new_with_label(c"Start")

      val startGame = CFuncPtr2.fromScalaFunction {
        (_: Ptr[GtkWidget], data: gpointer) =>
          {
            val stateHolder = data.value.asPtr[StateHolder]
            val micros = g_get_monotonic_time().value
            (!stateHolder).state = Some(GameState.init(micros))
          }
      }

      g_signal_connect(startGameButton, c"clicked", startGame, data.value)

      layout(window, startGameButton, mainArea, nextPieceArea)

      gtk_widget_show(window)
  }

  def layout(
      window: Ptr[GtkWidget],
      startGameButton: Ptr[GtkWidget],
      mainArea: Ptr[GtkDrawingArea],
      nextPieceArea: Ptr[GtkDrawingArea]
  ): Unit = {

    gtk_drawing_area_set_content_width(mainArea, GameRenderer.minWidth)
    gtk_drawing_area_set_content_height(mainArea, GameRenderer.minHeight)

    gtk_drawing_area_set_content_width(
      nextPieceArea,
      GameRenderer.nextPieceWidth
    )
    gtk_drawing_area_set_content_height(
      nextPieceArea,
      GameRenderer.nextPieceHeight
    )

    gtk_window_set_title(
      window.asInstanceOf[Ptr[GtkWindow]],
      c"Scalatromino"
    )

    val gameControls = gtk_box_new(GtkOrientation.GTK_ORIENTATION_HORIZONTAL, 0)
    gtk_widget_set_halign(gameControls, GtkAlign.GTK_ALIGN_START)
    gtk_widget_set_valign(gameControls, GtkAlign.GTK_ALIGN_START)
    gtk_box_append(gameControls.asPtr[GtkBox], startGameButton)

    val topLevel = gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL, 0)
    gtk_widget_set_halign(topLevel, GtkAlign.GTK_ALIGN_CENTER)
    gtk_widget_set_valign(topLevel, GtkAlign.GTK_ALIGN_CENTER)

    gtk_box_append(topLevel.asPtr[GtkBox], gameControls.asPtr[GtkWidget])

    val gameArea = gtk_box_new(GtkOrientation.GTK_ORIENTATION_HORIZONTAL, 0)

    val sidebar = gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL, 0)

    val shortHelp = gtk_label_new(c"")
    gtk_label_set_markup(
      shortHelp.asPtr[GtkLabel],
      c"""
<tt>
A &#x2190;         Move left
D &#x2192;         Move right
S &#x2193;         Rotate clockwise
W &#x2191; `space` Drop
</tt>
    """
    )

    gtk_box_append(sidebar.asPtr[GtkBox], nextPieceArea.asPtr[GtkWidget])
    gtk_box_append(sidebar.asPtr[GtkBox], shortHelp.asPtr[GtkWidget])

    gtk_box_append(gameArea.asPtr[GtkBox], sidebar.asPtr[GtkWidget])
    gtk_box_append(gameArea.asPtr[GtkBox], mainArea.asPtr[GtkWidget])

    gtk_box_append(topLevel.asPtr[GtkBox], gameArea.asPtr[GtkWidget])
    gtk_window_set_child(window.asPtr[GtkWindow], topLevel)
  }
end MainUI
