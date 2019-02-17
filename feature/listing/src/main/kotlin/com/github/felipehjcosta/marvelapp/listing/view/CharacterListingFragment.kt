package com.github.felipehjcosta.marvelapp.listing.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.felipehjcosta.marvelapp.base.imageloader.ImageLoader
import com.github.felipehjcosta.marvelapp.base.navigator.AppNavigator
import com.github.felipehjcosta.marvelapp.base.rx.plusAssign
import com.github.felipehjcosta.marvelapp.listing.R
import com.github.felipehjcosta.marvelapp.listing.di.setupDependencyInjection
import com.github.felipehjcosta.marvelapp.listing.presentation.CharacterListViewModel
import com.github.felipehjcosta.recyclerviewdsl.onRecyclerView
import com.jakewharton.rxbinding3.recyclerview.RecyclerViewScrollEvent
import com.jakewharton.rxbinding3.recyclerview.scrollEvents
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import io.reactivex.Observable.combineLatest
import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import com.github.felipehjcosta.marvelapp.base.R as RBase
import com.github.felipehjcosta.marvelapp.listing.R.drawable.ic_arrow_back_white_24dp as navigationIconResId
import kotlinx.android.synthetic.main.listing_fragment.loading_view as loadingView
import kotlinx.android.synthetic.main.listing_fragment.recycler_view as recyclerView
import kotlinx.android.synthetic.main.listing_fragment.swipe_refresh_view as swipeRefreshView


class CharacterListingFragment : Fragment() {

    @Inject
    lateinit var viewModel: CharacterListViewModel

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var appNavigator: AppNavigator

    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDependencyInjection()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.listing_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linearLayoutManger = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManger

        bind(linearLayoutManger, loadingView, swipeRefreshView)
    }

    private fun bind(
        linearLayoutManger: LinearLayoutManager,
        contentLoadingProgressBar: ContentLoadingProgressBar,
        swipeRefresh: SwipeRefreshLayout
    ) {
        compositeDisposable = CompositeDisposable()

        compositeDisposable += viewModel.items
            .subscribe {
                onRecyclerView(recyclerView) {
                    bind(R.layout.listing_fragment_item) {
                        withItems(it) {

                            on<TextView>(R.id.title) {
                                it.view?.text = it.item?.name
                            }

                            on<ImageView>(R.id.image) {
                                val imageUrl = it.item?.image
                                val imageView = it.view
                                if (imageUrl != null && imageView != null) {
                                    val radius = RBase.dimen.image_default_color_radius
                                    val cornerRadius = imageView.resources
                                        .getDimensionPixelSize(radius)
                                    imageLoader
                                        .loadRoundedImage(imageUrl, imageView, cornerRadius)
                                }
                            }

                            onClick { _, item ->
                                activity?.let {
                                    appNavigator.showDetail(it, item?.id ?: 0)
                                }
                            }
                        }
                    }
                }
            }

        compositeDisposable += viewModel.showLoading
            .map {
                if (it)
                    recyclerView to contentLoadingProgressBar
                else
                    contentLoadingProgressBar to recyclerView
            }
            .subscribe { crossFade(it.first, it.second) }

        compositeDisposable += viewModel.showLoading.subscribe { swipeRefresh.isRefreshing = it }

        compositeDisposable +=  swipeRefresh.refreshes()
            .flatMapCompletable { viewModel.loadItemsCommand.execute() }
            .subscribe()

        compositeDisposable += viewModel.loadItemsCommand.execute().subscribe()

        compositeDisposable += viewModel.newItems
            .subscribe {
                onRecyclerView(recyclerView) {
                    bind(R.layout.listing_fragment_item) {
                        addExtraItems(it)
                    }
                }
            }

        val loadMoreCommand = viewModel.loadMoreItemsCommand

        compositeDisposable += combineLatest(recyclerView.scrollEvents(), just(linearLayoutManger),
            BiFunction { event: RecyclerViewScrollEvent, layoutManager: LinearLayoutManager ->
                if (event.dy > 0) {
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findLastVisibleItemPosition()
                    (totalItemCount) < (firstVisibleItem + VISIBLE_THRESHOLD)
                } else {
                    false
                }
            })
            .debounce(DEBOUNCE_SCROLL_TIMEOUT, MILLISECONDS, mainThread())
            .withLatestFrom(viewModel.showLoadingMore.startWith(false),
                BiFunction { shouldLoadNewItems: Boolean, showLoadingMore: Boolean ->
                    if (showLoadingMore) false else shouldLoadNewItems
                })
            .filter { it == true }
            .flatMapCompletable { loadMoreCommand.execute() }
            .subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbind()
    }

    private fun unbind() {
        compositeDisposable.dispose()
    }

    private fun crossFade(fromView: View, toView: View) {

        fromView.visibility = View.VISIBLE

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        toView.alpha = 0.0f
        toView.visibility = View.VISIBLE

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.

        val shortAnimationDuration = resources
            .getInteger(android.R.integer.config_shortAnimTime)
            .toLong()

        toView.animate()
            .alpha(1f)
            .setDuration(shortAnimationDuration)
            .setListener(null)

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        fromView.animate()
            .alpha(0f)
            .setDuration(shortAnimationDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fromView.visibility = View.GONE
                }
            })
    }

    companion object {

        private const val DEBOUNCE_SCROLL_TIMEOUT = 400L
        private const val VISIBLE_THRESHOLD = 5

        fun newInstance(): CharacterListingFragment {
            return CharacterListingFragment()
        }
    }
}
