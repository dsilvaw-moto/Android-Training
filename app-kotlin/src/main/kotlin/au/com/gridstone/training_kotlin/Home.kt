package au.com.gridstone.training_kotlin

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.State
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewAnimator
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

/**
 * An event triggered on the home screen.
 */
sealed class HomeUiEvent {
  object RequestGallery : HomeUiEvent()
  data class ViewImage(val image: Image) : HomeUiEvent()
}

/**
 * A complete representation of the home screen's state.
 */
sealed class HomeUiModel {
  object Idle : HomeUiModel()
  object Loading : HomeUiModel()
  data class Success(val images: List<Image>) : HomeUiModel()
  data class Error(val message: String) : HomeUiModel()
}

/**
 * The navigation stack entry of the home screen; also the glue between the user interface and
 * model data.
 */
class HomeController : Controller() {
  private val disposables = CompositeDisposable()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
      inflater.inflate(R.layout.home, container, false)

  override fun onAttach(view: View) {
    if (view !is HomeView) throw AssertionError("View must be HomeView.")

    // Map Results to UiModels and pump them into the view.
    disposables += GalleryData.results
        .map { result ->
          when (result) {
            is GalleryResult.Idle -> HomeUiModel.Idle
            is GalleryResult.Loading -> HomeUiModel.Loading
            is GalleryResult.Success -> HomeUiModel.Success(result.images)
            is GalleryResult.Error -> HomeUiModel.Error(result.message)
          }
        }
        .subscribe { homeUiModel -> view.display(homeUiModel) }

    // Multicast view events because we want two subscriptions.
    val events = view.events.share()

    // Map RequestGallery Events to RequestGallery Actions.
    disposables += events.ofType<HomeUiEvent.RequestGallery>()
        .map { GalleryAction.RequestGallery }
        .subscribe(GalleryData.actions::onNext) // BLEH!

    // Navigate to details view when user taps on images.
    disposables += events.ofType<HomeUiEvent.ViewImage>()
        .subscribe { (image) ->
          router.pushController(RouterTransaction.with(DetailsController(image)))
        }
  }

  override fun onDetach(view: View) {
    disposables.clear()
  }
}

/**
 * Android View of the home screen, capable of rendering any HomeUiModel.
 */
class HomeView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
  private val animator: ViewAnimator by bindView(R.id.home_animator)
  private val errorMessageView: TextView by bindView(R.id.home_error_message)
  private val retryButton: Button by bindView(R.id.home_retry_button)
  private val recyclerView: RecyclerView by bindView(R.id.home_recycler)
  private val itemSpacing = resources.getDimensionPixelSize(R.dimen.image_item_spacing)
  private val adapter = ImageAdapter()

  val events: Observable<HomeUiEvent> = Observable.merge(
      adapter.clicks.map { HomeUiEvent.ViewImage(it) },
      Observable.defer { retryButton.clicks().map { HomeUiEvent.RequestGallery } })

  override fun onFinishInflate() {
    super.onFinishInflate()
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
      override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
        val position = recyclerView.getChildAdapterPosition(view)
        if (position != 0) outRect.top = itemSpacing
      }
    })
  }

  fun display(model: HomeUiModel) {
    when (model) {
      is HomeUiModel.Loading -> {
        animator.displayedChild = LOADING_INDEX
      }

      is HomeUiModel.Error -> {
        animator.displayedChild = ERROR_INDEX
        errorMessageView.text = model.message
      }

      is HomeUiModel.Success -> {
        adapter.setItems(model.images)
        animator.displayedChild = RECYCLER_INDEX
      }
    }
  }

  private companion object {
    const val LOADING_INDEX = 0
    const val ERROR_INDEX = 1
    const val RECYCLER_INDEX = 2
  }
}
