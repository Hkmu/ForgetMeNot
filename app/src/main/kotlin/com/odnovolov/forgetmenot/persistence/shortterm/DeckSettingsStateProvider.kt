package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckSettingsStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = DeckSettings.State::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckSettings.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckSettings.State) = SerializableState(state.deck.id)

    override fun toOriginal(serializableState: SerializableState): DeckSettings.State {
        val deck: Deck = globalState.decks.first { it.id == serializableState.deckId }
        return DeckSettings.State(deck)
    }
}