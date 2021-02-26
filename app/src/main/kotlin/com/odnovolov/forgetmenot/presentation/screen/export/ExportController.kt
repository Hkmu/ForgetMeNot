package com.odnovolov.forgetmenot.presentation.screen.export

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckExporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.export.ExportController.Command
import com.odnovolov.forgetmenot.presentation.screen.export.ExportController.Command.CreateFiles
import com.odnovolov.forgetmenot.presentation.screen.export.ExportEvent.GotFilesCreationResult
import com.odnovolov.forgetmenot.presentation.screen.export.ExportEvent.GotFilesCreationResult.FileCreationResult
import com.odnovolov.forgetmenot.presentation.screen.export.ExportEvent.SelectedTheFileFormat
import java.io.OutputStream

class ExportController(
    private val deckExporter: DeckExporter,
    private val dialogState: ExportDialogState,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<ExportDialogState>
) : BaseController<ExportEvent, Command>() {
    sealed class Command {
        class CreateFiles(val deckNames: List<String>, val extension: String) : Command()
    }

    override fun handle(event: ExportEvent) {
        when (event) {
            is SelectedTheFileFormat -> {
                dialogState.fileFormat = event.fileFormat
                val deckNames: List<String> = dialogState.decks.map { deck: Deck -> deck.name }
                sendCommand(CreateFiles(deckNames, event.fileFormat.extension))
                dialogState.stage = Stage.WaitingForDestination
            }

            is GotFilesCreationResult -> {
                val fileFormat: FileFormat = dialogState.fileFormat ?: kotlin.run {
                    dialogState.stage = Stage.WaitingForFileFormat
                    return
                }
                dialogState.stage = Stage.Exporting
                val exportedDeckNames: MutableList<String> = ArrayList()
                val failedDeckNames: MutableList<String> = ArrayList()
                for (fileCreationResult: FileCreationResult in event.filesCreationResult) {
                    val (deckName: String, outputStream: OutputStream?) = fileCreationResult
                    if (outputStream == null) {
                        failedDeckNames.add(deckName)
                        continue
                    }
                    val deck = globalState.decks.first { deck: Deck -> deck.name == deckName }
                    val success: Boolean = deckExporter.export(deck, fileFormat, outputStream)
                    if (success) {
                        exportedDeckNames.add(deckName)
                    } else {
                        failedDeckNames.add(deckName)
                    }
                }
                dialogState.stage = Stage.Finished(exportedDeckNames, failedDeckNames)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}