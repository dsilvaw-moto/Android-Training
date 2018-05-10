package au.com.gridstone.trainingkotlin.main

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.gridstone.trainingkotlin.R
import au.com.gridstone.trainingkotlin.imgur.ImgurPost
import com.bluelinelabs.conductor.Controller
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_description
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_height
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_image
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_progress
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_time
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_title
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_views
import kotlinx.android.synthetic.main.fragment_imgur_post.view.post_width

class ImgurPostController(args: Bundle) : Controller(args) {

  private lateinit var post: ImgurPost

  constructor(toShow: ImgurPost) : this(prepareArguments(toShow)) {
    post = toShow;
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.fragment_imgur_post, container, false)
    initView(view)
    return view
  }

  private fun initView(view: View) {
    view.post_time.text = DateUtils.getRelativeDateTimeString(
        view.context,
        post.datetime.time,
        DateUtils.SECOND_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS, 0
    )
    view.post_title.text = post.title
    view.post_description.text = post.description
    view.post_width.text = post.width.toString()
    view.post_height.text = post.height.toString()
    view.post_views.text = post.views.toString()

    Picasso.with(view.context)
        .load(post.link)
        .into(view.post_image, object : Callback {
          override fun onSuccess() {
            view.post_progress.visibility = View.GONE
          }

          override fun onError() {}
        })
  }

  companion object {
    const val KEY_POST = "post"

    fun prepareArguments(post: ImgurPost): Bundle {
      val bundle = Bundle()
      bundle.putParcelable(KEY_POST, post)
      return bundle
    }
  }

}
