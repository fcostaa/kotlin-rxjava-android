package com.felipecosta.kotlinrxjavasample.modules.wiki.presentation

import com.felipecosta.kotlinrxjavasample.data.pojo.Character
import com.felipecosta.kotlinrxjavasample.modules.wiki.datamodel.HighlightedCharactersDataModel
import com.felipecosta.kotlinrxjavasample.modules.listing.presentation.CharacterItemViewModel
import com.felipecosta.rxcommand.AsyncCommand
import com.felipecosta.rxcommand.Command
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HighlightedCharactersViewModel(dataModel: HighlightedCharactersDataModel) {

    private val asyncLoadItemsCommand: AsyncCommand<List<Character>> = AsyncCommand {
        dataModel.getHighlightedCharacters()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
    }

    val loadItemsCommand: Command
        get() = asyncLoadItemsCommand

    val items: Observable<List<CharacterItemViewModel>>
        get() = asyncLoadItemsCommand.execution.map {
            it.map {
                CharacterItemViewModel(it.id, it.name, it.thumbnail.url)
            }
        }
}