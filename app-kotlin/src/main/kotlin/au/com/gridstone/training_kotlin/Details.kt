package au.com.gridstone.training_kotlin

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.squareup.picasso.Picasso

/**
 * The navigation stack entry of the details screen; also the glue between the user interface and
 * model data.
 */
class DetailsController(args: Bundle) : Controller(args) {
  constructor(image: Image) : this(bundleImage(image))

  val image: Image = args.getParcelable(KEY_IMAGE)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
      inflater.inflate(R.layout.details, container, false)

  override fun onAttach(view: View) {
    if (view !is DetailsView) throw AssertionError("View must be DetailsView.")
    view.display(image)
  }

  private companion object {
    const val KEY_IMAGE = "DetailsController.Image"

    fun bundleImage(image: Image) = Bundle().apply {
      putParcelable(KEY_IMAGE, image)
    }
  }
}

/**
 * Android View capable of rendering image details.
 */
class DetailsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
  private val imageView: ImageView by bindView(R.id.details_image)
  private val titleView: TextView by bindView(R.id.details_title)
  private val dateView: TextView by bindView(R.id.details_date)
  private val widthView: TextView by bindView(R.id.details_width)
  private val heightView: TextView by bindView(R.id.details_height)
  private val viewCountView: TextView by bindView(R.id.details_view_count)

  fun display(image: Image) {
    val dateTimeMillis = image.datetime * 1000
    val timeAgo = DateUtils.getRelativeTimeSpanString(dateTimeMillis)

    Picasso.with(context).load(image.link).fit().centerCrop().into(imageView)
    titleView.text = image.title
    dateView.text = timeAgo
    widthView.text = resources.getString(R.string.details_dimen_format, image.width)
    heightView.text = resources.getString(R.string.details_dimen_format, image.height)
    viewCountView.text = image.views.toString()
  }
}
