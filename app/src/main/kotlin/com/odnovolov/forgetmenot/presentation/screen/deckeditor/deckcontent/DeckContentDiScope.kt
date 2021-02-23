package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope

class DeckContentDiScope {
    val controller = DeckContentController(
        DeckExporter(),
        DeckEditorDiScope.getOrRecreate().screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        DeckEditorDiScope.getOrRecreate().screenStateProvider
    )

    val viewModel = DeckContentViewModel(
        DeckEditorDiScope.getOrRecreate().screenState,
        AppDiScope.get().fileImportStorage
    )

    companion object : DiScopeManager<DeckContentDiScope>() {
        override fun recreateDiScope() = DeckContentDiScope()

        override fun onCloseDiScope(diScope: DeckContentDiScope) {
            diScope.controller.dispose()
        }
    }
}