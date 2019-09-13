package com.odnovolov.forgetmenot.exercise.exercisecard.withouttest

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecard.withouttest.ExerciseCardWithoutTestEvent.ShowAnswerButtonClicked
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries

class ExerciseCardWithoutTestController(private val id: Long) :
    BaseController<ExerciseCardWithoutTestEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: ExerciseCardWithoutTestEvent) {
        return when (event) {
            ShowAnswerButtonClicked -> {
                queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
                queries.setAnswerCorrect(true, id)
            }
        }
    }
}