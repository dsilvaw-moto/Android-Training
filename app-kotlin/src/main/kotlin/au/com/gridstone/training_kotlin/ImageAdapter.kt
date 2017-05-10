package au.com.gridstone.training_kotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Adapt a `List` of images for a `RecyclerView` and expose image clicks as a stream.
 */
class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
  private val clickSubject = PublishSubject.create<Image>()
  private var items = emptyList<Image>()

  val clicks: Observable<Image> = clickSubject

  fun setItems(items: List<Image>) {
    if (this.items != items) {
      this.items = items
      notifyDataSetChanged()
    }
  }

  override fun getItemCount() = items.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val view = inflater.inflate(R.layout.image_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bindTo(items[position])
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView by bindView(R.id.item_image)
    private val titleView: TextView by bindView(R.id.item_title)

    private var image: Image? = null

    init {
      itemView.clicks().map { image!! }.subscribe(clickSubject)
    }

    fun bindTo(image: Image) {
      this.image = image

      Picasso.with(itemView.context)
          .load(image.link)
          .fit()
          .centerCrop()
          .into(imageView)

      titleView.text = image.title
    }
  }
}
