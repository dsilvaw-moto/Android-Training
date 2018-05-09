package au.com.gridstone.trainingkotlin.main

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.gridstone.trainingkotlin.imgur.ImgurPost
import au.com.gridstone.trainingkotlin.R
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

private const val ARG_PARAM = "POST"

class ImgurPostFragment : Fragment() {

  private var toShow: ImgurPost? = null
  private var listener: OnFragmentInteractionListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      toShow = it.getParcelable(ARG_PARAM)
    }
    toShow = arguments?.getParcelable(ARG_PARAM)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val toReturn = inflater.inflate(R.layout.fragment_imgur_post, container, false)
    if (savedInstanceState != null) onRestoreInstanceState(savedInstanceState)
    showData(toShow!!, toReturn)
    return toReturn
  }

  fun showData(
    toShow: ImgurPost,
    toReturn: View
  ) {
    Picasso.with(context)
        .load(toShow.link)
        .into(toReturn.post_image, object : Callback {
          override fun onSuccess() {
            toReturn.post_progress.visibility = View.GONE
          }

          override fun onError() {}
        })
    toReturn.post_title.text = toShow.title
    val test = DateUtils.getRelativeDateTimeString(
        toReturn.context,
        toShow.datetime.time,
        DateUtils.SECOND_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS, 0
    )
    toReturn.post_time.text = test
    toReturn.post_description.text = toShow.description
    toReturn.post_width.text = toShow.width.toString()
    toReturn.post_height.text = toShow.height.toString()
    toReturn.post_views.text = toShow.views.toString()
  }

  fun onButtonPressed(uri: Uri) {
    listener?.onFragmentInteraction(uri)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      listener = context
    } else {
      throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    listener = null
  }

  interface OnFragmentInteractionListener {
    fun onFragmentInteraction(uri: Uri)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(KEY_POST, toShow)
  }

  private fun onRestoreInstanceState(inState: Bundle) {
    toShow = inState.getParcelable(KEY_POST)
  }

  companion object {

    const val KEY_POST = "post"

    @JvmStatic
    fun newInstance(toShow: ImgurPost) =
      ImgurPostFragment().apply {
        arguments = Bundle().apply {
          putParcelable(ARG_PARAM, toShow)
        }
      }
  }
}
