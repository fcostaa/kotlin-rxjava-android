package com.felipecosta.kotlinrxjavasample.modules.detail.di

import com.felipecosta.kotlinrxjavasample.data.DataRepository
import com.felipecosta.kotlinrxjavasample.data.pojo.Character
import com.felipecosta.kotlinrxjavasample.modules.detail.datamodel.DetailContentDataModel
import com.felipecosta.kotlinrxjavasample.modules.detail.datamodel.DetailDataModel
import com.felipecosta.kotlinrxjavasample.modules.detail.presentation.CharacterDetailViewModel
import com.felipecosta.kotlinrxjavasample.di.IOScheduler
import com.felipecosta.kotlinrxjavasample.di.MainScheduler
import com.felipecosta.kotlinrxjavasample.rx.AsyncCommand
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler

@Module
class DetailModule(val characterId: Int) {
    @DetailScope
    @Provides
    fun provideDetailDataModel(dataRepository: DataRepository): DetailDataModel = DetailContentDataModel(dataRepository)

    @DetailScope
    @Provides
    fun provideAsyncCommand(detailContentDataModel: DetailDataModel, @IOScheduler ioScheduler: Scheduler, @MainScheduler mainScheduler: Scheduler): AsyncCommand<Character> = AsyncCommand {
        detailContentDataModel
                .character(characterId)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
    }

    @DetailScope
    @Provides
    fun provideDetailViewModel(asyncCommand: AsyncCommand<Character>) = CharacterDetailViewModel(asyncCommand)
}
