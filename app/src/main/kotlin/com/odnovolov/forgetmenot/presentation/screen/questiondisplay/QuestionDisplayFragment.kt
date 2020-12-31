package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.HelpButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.QuestionDisplaySwitchToggled
import kotlinx.android.synthetic.main.fragment_question_display.*
import kotlinx.android.synthetic.main.fragment_question_display.appBar
import kotlinx.android.synthetic.main.fragment_question_display.backButton
import kotlinx.android.synthetic.main.fragment_question_display.contentScrollView
import kotlinx.coroutines.launch

class QuestionDisplayFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        QuestionDisplayDiScope.reopenIfClosed()
    }

    private var controller: QuestionDisplayController? = null
    private lateinit var viewModel: QuestionDisplayViewModel
    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_question_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = QuestionDisplayDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        helpButton.setOnClickListener {
            controller?.dispatch(HelpButtonClicked)
        }
        questionDisplayFrame.setOnClickListener {
            controller?.dispatch(QuestionDisplaySwitchToggled)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            isQuestionDisplayed.observe { isQuestionDisplayed: Boolean ->
                questionDisplaySwitch.text = getString(
                    if (isQuestionDisplayed)
                        R.string.on else
                        R.string.off
                )
                questionDisplaySwitch.isChecked = isQuestionDisplayed
                questionDisplaySwitch.uncover()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scrollListener = object : ViewTreeObserver.OnScrollChangedListener {
            private var canScrollUp = true

            override fun onScrollChanged() {
                val canScrollUp = contentScrollView?.canScrollVertically(-1) ?: return
                if (this.canScrollUp != canScrollUp) {
                    this.canScrollUp = canScrollUp
                    appBar?.isActivated = canScrollUp
                }
            }
        }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        scrollListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            QuestionDisplayDiScope.close()
        }
    }
}