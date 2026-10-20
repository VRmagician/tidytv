package dev.tidytv.core.flow

enum class TvKind { ANDROID_TV, FIRE_TV, SAMSUNG_LG, NOT_SURE }

enum class Step { WELCOME, UNSUPPORTED, GET_READY, CONNECT, REVIEW, TIDYING, HOME_SCREEN, DONE }

enum class Mode { CLEAN, RESTORE }

data class Wizard(
    val step: Step = Step.WELCOME,
    val kind: TvKind? = null,
    val mode: Mode = Mode.CLEAN,
    /** A change is running on the TV: no navigation away until it ends. */
    val busy: Boolean = false,
)

sealed interface WizardEvent {
    data class Choose(val kind: TvKind) : WizardEvent
    data object RestoreTapped : WizardEvent
    data object Ready : WizardEvent
    /** [fireOs] comes from the TV itself and overrides what the user picked. */
    data class Connected(val fireOs: Boolean) : WizardEvent
    data object StartTidy : WizardEvent
    data class Busy(val busy: Boolean) : WizardEvent
    /** Clean-up ended. [stopped] keeps the user on the tidying screen with the stop message. */
    data class TidyEnded(val stopped: Boolean, val launcherPossible: Boolean) : WizardEvent
    data object HomeScreenDone : WizardEvent
    data object ToDone : WizardEvent
    data object Back : WizardEvent
    data object StartOver : WizardEvent
}

/** The six-screen flow as a pure reducer. [reduce] returns null when Back should leave the app. */
object WizardReducer {
    fun reduce(s: Wizard, e: WizardEvent): Wizard? = when (e) {
        is WizardEvent.Choose -> if (s.step != Step.WELCOME) s else when (e.kind) {
            TvKind.SAMSUNG_LG -> s.copy(step = Step.UNSUPPORTED, kind = e.kind, mode = Mode.CLEAN)
            else -> s.copy(step = Step.GET_READY, kind = e.kind, mode = Mode.CLEAN)
        }
        WizardEvent.RestoreTapped -> if (s.busy) s else Wizard(step = Step.GET_READY, mode = Mode.RESTORE)
        WizardEvent.Ready -> if (s.step == Step.GET_READY) s.copy(step = Step.CONNECT) else s
        is WizardEvent.Connected -> if (s.step != Step.CONNECT) s else {
            val kind = if (e.fireOs) TvKind.FIRE_TV else if (s.kind == TvKind.FIRE_TV) TvKind.ANDROID_TV else s.kind ?: TvKind.ANDROID_TV
            s.copy(step = if (s.mode == Mode.RESTORE) Step.DONE else Step.REVIEW, kind = kind)
        }
        WizardEvent.StartTidy -> if (s.step == Step.REVIEW) s.copy(step = Step.TIDYING, busy = true) else s
        is WizardEvent.Busy -> s.copy(busy = e.busy)
        is WizardEvent.TidyEnded -> when {
            s.step != Step.TIDYING -> s
            e.stopped -> s.copy(busy = false)
            // Fire TV: clean-up only, no home screen.
            e.launcherPossible && s.kind != TvKind.FIRE_TV -> s.copy(step = Step.HOME_SCREEN, busy = false)
            else -> s.copy(step = Step.DONE, busy = false)
        }
        WizardEvent.HomeScreenDone -> if (s.step == Step.HOME_SCREEN && !s.busy) s.copy(step = Step.DONE) else s
        WizardEvent.ToDone -> if (s.busy) s else s.copy(step = Step.DONE)
        WizardEvent.StartOver -> if (s.busy) s else Wizard()
        WizardEvent.Back -> back(s)
    }

    private fun back(s: Wizard): Wizard? {
        if (s.busy) return s
        return when (s.step) {
            Step.WELCOME -> null
            Step.UNSUPPORTED, Step.GET_READY -> Wizard()
            Step.CONNECT -> s.copy(step = Step.GET_READY)
            Step.REVIEW -> s.copy(step = Step.CONNECT)
            // Changes are made: going back can't undo them, so Back goes to the summary.
            Step.TIDYING, Step.HOME_SCREEN -> s.copy(step = Step.DONE)
            Step.DONE -> Wizard()
        }
    }
}
