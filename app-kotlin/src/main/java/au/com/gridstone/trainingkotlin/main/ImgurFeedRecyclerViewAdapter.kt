package au.com.gridstone.trainingkotlin.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import au.com.gridstone.trainingkotlin.main.ImgurFeedFragment.OnListFragmentInteractionListener
import au.com.gridstone.trainingkotlin.imgur.ImgurPost

import kotlinx.android.synthetic.main.list_item.view.*
import au.com.gridstone.trainingkotlin.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ImgurFeedRecyclerViewAdapter(
  private var values: List<ImgurPost>,
  private val listener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<ImgurFeedRecyclerViewAdapter.ViewHolder>() {

  private val mOnClickListener: View.OnClickListener

  init {
    mOnClickListener = View.OnClickListener { v ->
      val item = v.tag as ImgurPost
      listener?.onListFragmentInteraction(item)
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.list_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    val item = values[position]

    Picasso.with(holder.view.context)
        .load(item.link)
        .fit()
        .centerCrop()
        .into(holder.image, object : Callback {
          override fun onSuccess() {
            holder.progress.visibility = View.GONE
          }

          override fun onError() {}
        })

    holder.title.text = item.title

    with(holder.view) {
      tag = item
      setOnClickListener(mOnClickListener)
    }
  }

  fun swapData(datas: List<ImgurPost>?) {
    values = datas!!
    this.notifyDataSetChanged()
  }

  override fun getItemCount(): Int = values.size

  inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.post_item_image
    val title: TextView = view.post_item_title
    val progress: ProgressBar = view.post_item_progress

    override fun toString(): String {
      return super.toString() + " '" + title.text + "'"
    }
  }
}
