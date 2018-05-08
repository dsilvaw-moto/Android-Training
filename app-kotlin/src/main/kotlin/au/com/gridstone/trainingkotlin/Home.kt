package au.com.gridstone.trainingkotlin

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
import au.com.gridstone.trainingkotlin.GalleryResult.Error
import au.com.gridstone.trainingkotlin.GalleryResult.Loading
import au.com.gridstone.trainingkotlin.GalleryResult.Success
import au.com.gridstone.trainingkotlin.HomeUiEvent.RequestGallery
import au.com.gridstone.trainingkotlin.HomeUiEvent.ViewImage
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer

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
    disposables += GalleryRepository.results
        .map { result ->
          when (result) {
            is Loading -> HomeUiModel.Loading
            is Success -> HomeUiModel.Success(result.images)
            is Error -> HomeUiModel.Error(result.message)
          }
        }
        .subscribe(view)

    // Multicast view events because we want two subscriptions.
    val events: Observable<HomeUiEvent> = view.events.share()

    // Map RequestGallery Events to RequestGallery Actions.
    disposables += events.ofType<RequestGallery>().subscribe { GalleryRepository.refresh() }

    // Navigate to details view when user taps on images.
    disposables += events.ofType<ViewImage>().subscribe { (image) ->
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
class HomeView(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs), Consumer<HomeUiModel> {

  private val animator: ViewAnimator by bindView(R.id.home_animator)
  private val errorMessageView: TextView by bindView(R.id.home_error_message)
  private val retryButton: Button by bindView(R.id.home_retry_button)
  private val recyclerView: RecyclerView by bindView(R.id.home_recycler)
  private val itemSpacing = resources.getDimensionPixelSize(R.dimen.image_item_spacing)
  private val adapter = ImageAdapter()

  val events: Observable<HomeUiEvent> = Observable.merge(
      adapter.clicks.map { ViewImage(it) },
      Observable.defer { retryButton.clicks().map { RequestGallery } })

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

  override fun accept(model: HomeUiModel) {
    when (model) {
      is HomeUiModel.Loading -> {
        animator.displayedChild = 0
      }

      is HomeUiModel.Error -> {
        animator.displayedChild = 1
        errorMessageView.text = model.message
      }

      is HomeUiModel.Success -> {
        adapter.setItems(model.images)
        animator.displayedChild = 2
      }
    }
  }
}

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
  object Loading : HomeUiModel()
  data class Success(val images: List<Image>) : HomeUiModel()
  data class Error(val message: String) : HomeUiModel()
}

