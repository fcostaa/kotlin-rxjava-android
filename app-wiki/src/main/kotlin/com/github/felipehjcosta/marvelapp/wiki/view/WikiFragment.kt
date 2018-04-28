package com.github.felipehjcosta.marvelapp.wiki.view


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.felipehjcosta.layoutmanager.GalleryLayoutManager
import com.github.felipehjcosta.marvelapp.base.R
import com.github.felipehjcosta.marvelapp.base.rx.plusAssign
import com.github.felipehjcosta.marvelapp.base.util.bindView
import com.github.felipehjcosta.marvelapp.base.util.findBy
import com.github.felipehjcosta.marvelapp.base.util.navigateToDetail
import com.github.felipehjcosta.marvelapp.base.util.navigateToListing
import com.github.felipehjcosta.marvelapp.wiki.di.setupDependencyInjection
import com.github.felipehjcosta.marvelapp.wiki.presentation.HighlightedCharactersViewModel
import com.github.felipehjcosta.marvelapp.wiki.presentation.OthersCharactersViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class WikiFragment : Fragment() {

    @Inject
    lateinit var highlightedCharactersViewModel: HighlightedCharactersViewModel

    @Inject
    lateinit var othersCharactersViewModel: OthersCharactersViewModel

    private lateinit var compositeDisposable: CompositeDisposable

    private lateinit var othersAdapter: OthersCharacterItemRecyclerViewAdapter

    private lateinit var highlightedAdapter: HighlightedCharacterItemRecyclerViewAdapter

    private lateinit var highlightedCharactersLayoutManager: GalleryLayoutManager

    private val toolbar: Toolbar by bindView(R.id.highlighted_characters_toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDependencyInjection()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wiki, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view?.let {
            var recyclerView: RecyclerView = it.findBy(R.id.others_characters_recycler_view)

            recyclerView.layoutManager = GridLayoutManager(context, 3)
            othersAdapter = OthersCharacterItemRecyclerViewAdapter()
            recyclerView.adapter = othersAdapter

            recyclerView = it.findBy(R.id.highlighted_characters_recycler_view)
            highlightedAdapter = HighlightedCharacterItemRecyclerViewAdapter()

            highlightedCharactersLayoutManager = GalleryLayoutManager()
            highlightedCharactersLayoutManager.attach(recyclerView)

            recyclerView.adapter = highlightedAdapter

            toolbar.inflateMenu(R.menu.wiki_toolbar_menu)
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.wiki_menu_search) {
                    activity?.let { navigateToListing(it) }
                    true
                } else {
                    false
                }
            }

            bind()
        }
    }

    private fun bind() {
        compositeDisposable = CompositeDisposable()

        compositeDisposable += highlightedCharactersViewModel.items
                .doOnNext {
                    val highlightedCharactersContainer: ViewGroup = view.findBy(R.id.highlighted_characters_container)
                    WikiGalleryCallbacksHandler(it, highlightedCharactersContainer).apply {
                        highlightedCharactersLayoutManager.itemTransformer = this
                        highlightedCharactersLayoutManager.onItemSelectedListener = this
                    }
                }
                .subscribe { highlightedAdapter.replaceItems(it) }

        compositeDisposable += highlightedAdapter.onItemSelected
                .subscribe { itemSelectedId ->
                    activity?.let { navigateToDetail(it, itemSelectedId) }
                }

        compositeDisposable += highlightedCharactersViewModel.loadItemsCommand.execute().subscribe()

        compositeDisposable += othersCharactersViewModel.items
                .subscribe { othersAdapter.replaceItems(it) }

        compositeDisposable += othersAdapter.onItemSelected
                .subscribe { itemSelectedId ->
                    activity?.let { navigateToDetail(it, itemSelectedId) }
                }

        compositeDisposable += othersCharactersViewModel.loadItemsCommand.execute().subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbind()
    }

    private fun unbind() {
        compositeDisposable.dispose()
    }

    companion object {
        fun newInstance() = WikiFragment()
    }

}