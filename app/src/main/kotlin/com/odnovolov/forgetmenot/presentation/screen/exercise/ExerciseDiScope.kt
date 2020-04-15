package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference.WalkingModePreferenceProvider
import com.odnovolov.forgetmenot.persistence.shortterm.ExerciseStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardController
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.CoroutineScope

class ExerciseDiScope private constructor(
    initialExerciseState: Exercise.State? = null
) {
    private val exerciseStateProvider = ExerciseStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val exerciseState: Exercise.State =
        initialExerciseState ?: exerciseStateProvider.load()

    private val walkingModePreferenceProvider = WalkingModePreferenceProvider(
        AppDiScope.get().database
    )

    private val walkingModePreference: WalkingModePreference = walkingModePreferenceProvider.load()

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app
    )

    private val exercise = Exercise(
        exerciseState,
        speakerImpl
    )

    val controller = ExerciseController(
        exercise,
        walkingModePreference,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        exerciseStateProvider
    )

    val viewModel = ExerciseViewModel(
        exerciseState,
        walkingModePreference
    )

    private val offTestExerciseCardController = OffTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver
    )

    private val manualTestExerciseCardController = ManualTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver
    )

    private val quizTestExerciseCardController = QuizTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver
    )

    private val entryTestExerciseCardController = EntryTestExerciseCardController(
        exercise,
        AppDiScope.get().longTermStateSaver
    )

    fun getExerciseCardAdapter(coroutineScope: CoroutineScope) = ExerciseCardAdapter(
        coroutineScope,
        offTestExerciseCardController,
        manualTestExerciseCardController,
        quizTestExerciseCardController,
        entryTestExerciseCardController
    )

    companion object : DiScopeManager<ExerciseDiScope>() {
        fun create(initialExerciseState: Exercise.State) = ExerciseDiScope(initialExerciseState)

        fun shareExercise(): Exercise {
            return diScope?.exercise ?: error("ExerciseDiScope is not opened")
        }

        override fun recreateDiScope() = ExerciseDiScope()

        override fun onCloseDiScope(diScope: ExerciseDiScope) {
            with(diScope) {
                speakerImpl.shutdown()
                controller.dispose()
                offTestExerciseCardController.dispose()
                manualTestExerciseCardController.dispose()
                quizTestExerciseCardController.dispose()
                entryTestExerciseCardController.dispose()
            }
        }
    }
}