package au.com.gridstone.trainingkotlin.main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import au.com.gridstone.trainingkotlin.imgur.ImgurClient
import au.com.gridstone.trainingkotlin.imgur.ImgurPost
import au.com.gridstone.trainingkotlin.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_imgur_feed_list.*
import kotlinx.android.synthetic.main.fragment_imgur_feed_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ImgurFeedFragment.OnListFragmentInteractionListener] interface.
 */
class ImgurFeedFragment() : Fragment(),
    SwipeRefreshLayout.OnRefreshListener {

  private var columnCount = 1

  private var listener: OnListFragmentInteractionListener? = null

  private var list: RecyclerView? = null
  private var feed: List<ImgurPost>? = ArrayList<ImgurPost>();
  private var feed_adapter: ImgurFeedRecyclerViewAdapter? = null;

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      columnCount = it.getInt(ARG_COLUMN_COUNT)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_imgur_feed_list, container, false)
    feed_adapter = ImgurFeedRecyclerViewAdapter(feed!!, listener);
    if (view.list is RecyclerView) {
      list = view.list;
      with(view.list) {
        layoutManager = when {
          columnCount <= 1 -> LinearLayoutManager(context)
          else -> GridLayoutManager(context, columnCount)
        }
        adapter = feed_adapter;
      }
    }
    if (savedInstanceState != null)
      onRestoreInstanceState(savedInstanceState);
    return view
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(KEY_SCROLL_POSITION, list!!.layoutManager.onSaveInstanceState())
    if (feed != null) outState.putParcelableArray(KEY_FEED, feed!!.toTypedArray())
  }

  private fun onRestoreInstanceState(inState: Bundle) {
    list!!.layoutManager.onRestoreInstanceState(inState.getParcelable(KEY_SCROLL_POSITION))
    feed = inState.getParcelableArrayList(KEY_FEED)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    swiperefresh.setOnRefreshListener(this)
    showProgressBar()
    loadData()
  }

  override fun onRefresh() {
    Toast.makeText(this.context, "Loading", Toast.LENGTH_SHORT)
    loadData()
  }

  private fun loadData() {
    ImgurClient.getInterface()
        .getLatestImages()!!
        .map { request -> request.data }
        .map { list -> list.filter { !it.is_album && it.type != "video/mp4" } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::handleResponse, this::handleError)

  }

  private fun hideProgressBar() {
    progress.visibility = View.GONE
  }

  private fun showProgressBar() {
    progress.visibility = View.VISIBLE
  }

  private fun handleResponse(imgurRequest: List<ImgurPost>?) {
    hideProgressBar()
    swiperefresh.isRefreshing = false
    feed = imgurRequest!!
    feed_adapter!!.swapData(feed)
  }

  private fun handleError(error: Throwable) {
    Log.d("DATA", error.localizedMessage)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnListFragmentInteractionListener) {
      listener = context
    } else {
      throw RuntimeException(
          context.toString() + " must implement OnListFragmentInteractionListener"
      )
    }
  }

  override fun onDetach() {
    super.onDetach()
    listener = null
  }

  interface OnListFragmentInteractionListener {
    fun onListFragmentInteraction(item: ImgurPost?)
  }

  companion object {

    const val ARG_COLUMN_COUNT = "column-count"
    const val KEY_SCROLL_POSITION = "scroll-position"
    const val KEY_FEED = "feed"

    @JvmStatic
    fun newInstance(columnCount: Int) =
      ImgurFeedFragment().apply {
        arguments = Bundle().apply {
          putInt(ARG_COLUMN_COUNT, columnCount)
        }
      }
  }
}
