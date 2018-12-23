package com.github.felipehjcosta.marvelapp.detail.presentation

import com.felipecosta.rxaction.RxAction
import com.felipecosta.rxaction.RxCommand
import com.github.felipehjcosta.marvelapp.base.character.data.pojo.Character
import com.github.felipehjcosta.marvelapp.detail.datamodel.DetailDataModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CharacterDetailViewModel @Inject constructor(
        private val characterId: Int,
        private val dataModel: DetailDataModel
) {

    private val asyncLoadCharacterAction: RxAction<Any, Character> = RxAction {
        dataModel.character(characterId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
    }

    val characterCommand: RxCommand<Any> get() = asyncLoadCharacterAction

    private val characterObservable: Observable<Character> = asyncLoadCharacterAction.elements.share()

    val name: Observable<String>
        get() = characterObservable.map { it.name }

    val description: Observable<String>
        get() = characterObservable.map { it.description }

    val thumbnailUrl: Observable<String>
        get() = characterObservable.map { it.thumbnail.url }

    val comicsCount: Observable<Int>
        get() = characterObservable.map { it.comics.items.count() }

    val eventsCount: Observable<Int>
        get() = characterObservable.map { it.events.items.count() }

    val seriesCount: Observable<Int>
        get() = characterObservable.map { it.series.items.count() }

    val storiesCount: Observable<Int>
        get() = characterObservable.map { it.stories.items.count() }

}
