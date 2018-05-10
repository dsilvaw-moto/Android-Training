package au.com.gridstone.trainingkotlin.main

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import au.com.gridstone.trainingkotlin.R
import au.com.gridstone.trainingkotlin.R.string
import au.com.gridstone.trainingkotlin.imgur.ImgurClient
import au.com.gridstone.trainingkotlin.imgur.ImgurClient.State.Handled
import au.com.gridstone.trainingkotlin.imgur.ImgurClient.State.Waiting
import au.com.gridstone.trainingkotlin.imgur.ImgurPost
import au.com.gridstone.trainingkotlin.main.ImgurFeedRecyclerViewAdapter.ListInteractionListener
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.fragment_imgur_feed_list.view.feed_list
import kotlinx.android.synthetic.main.fragment_imgur_feed_list.view.progress
import kotlinx.android.synthetic.main.fragment_imgur_feed_list.view.swiperefresh


class ImgurFeedController : Controller(), SwipeRefreshLayout.OnRefreshListener, ListInteractionListener, ImgurClient.Callback {


  private var adapter: ImgurFeedRecyclerViewAdapter? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.fragment_imgur_feed_list, container, false)

    val recycler: RecyclerView = view.findViewById(R.id.feed_list)

    if (view.feed_list is RecyclerView) {
      view.feed_list.layoutManager = LinearLayoutManager(this.activity)
      adapter = ImgurFeedRecyclerViewAdapter(ArrayList<ImgurPost>());
      view.feed_list.adapter = adapter
    }
    if (view.swiperefresh is SwipeRefreshLayout) {
      view.swiperefresh.setOnRefreshListener(this)
    }
    return view;
  }

  override fun onDestroy() {
    super.onDestroy()
    ImgurClient.clear(this)
  }

  override fun onRefresh() {
    Toast.makeText(
        activity, activity!!.baseContext.getString(string.pull_latest),
        Toast.LENGTH_LONG
    ).show()

    ImgurClient.loadGalley()
    when(ImgurClient.state){
      Waiting -> {
        ImgurClient.attach(this)
      }
      Handled -> {
        ImgurClient.get(this)
      }
    }
  }

  override fun onDataReady(data:List<ImgurPost>?) {
    if (adapter != null) {
      adapter!!.swapData(data)
      adapter!!.notifyDataSetChanged()
      showProgress(false)
      hideRefresh()
    }
  }

  override fun onDataError(error: Throwable) {

  }

  fun hideRefresh() {
    view!!.swiperefresh.isRefreshing = false;
  }

  override fun onListInteraction(interacted: ImgurPost) {
    if (interacted != null) {
      router.pushController(RouterTransaction.with(ImgurPostController(interacted)))
    }
  }

  private fun showProgress(set: Boolean) {
    view!!.progress.visibility = if (set) View.VISIBLE else View.GONE
  }

  override fun onSaveViewState(view: View, outState: Bundle) {
    outState.putParcelable(KEY_LAYOUT_MANAGER, view.feed_list.layoutManager.onSaveInstanceState())
    super.onSaveInstanceState(outState)
  }

  override fun onRestoreViewState(view: View, savedViewState: Bundle) {
    val state = savedViewState.getParcelable<Parcelable>(KEY_LAYOUT_MANAGER)
    view.feed_list.layoutManager.onRestoreInstanceState(state)
    super.onRestoreInstanceState(savedViewState)
  }

  companion object {
    const val KEY_LAYOUT_MANAGER = "layout_manager"
  }

}
