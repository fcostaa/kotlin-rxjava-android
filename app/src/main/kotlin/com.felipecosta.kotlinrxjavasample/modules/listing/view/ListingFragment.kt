package com.felipecosta.kotlinrxjavasample.modules.listing.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.felipecosta.kotlinrxjavasample.R
import com.felipecosta.kotlinrxjavasample.di.HasSubcomponentBuilders
import com.felipecosta.kotlinrxjavasample.modules.listing.di.ListingComponent
import com.felipecosta.kotlinrxjavasample.modules.listing.presentation.CharacterListViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ListingFragment : Fragment() {

    @Inject
    lateinit var viewModel: CharacterListViewModel

    lateinit var compositeDisposable: CompositeDisposable

    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity.application as HasSubcomponentBuilders).
                getSubcomponentBuilder<ListingComponent>(ListingComponent::class).
                build().
                inject(this)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.listing_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view!!.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }

    override fun onResume() {
        super.onResume()
        bind()
    }

    private fun bind() {
        compositeDisposable = CompositeDisposable()

        var disposable = viewModel.items.
                map(::CharacterItemRecyclerViewAdapter).
                subscribe { adapter ->
                    recyclerView?.adapter = adapter
                }

        compositeDisposable.add(disposable)

        disposable = viewModel.listCommand.execute().subscribe()

        compositeDisposable.addAll(disposable)
    }

    override fun onPause() {
        super.onPause()
        unbind()
    }

    private fun unbind() {
        compositeDisposable.dispose()
    }

    companion object {

        fun newInstance(): ListingFragment {
            return ListingFragment()
        }
    }
}