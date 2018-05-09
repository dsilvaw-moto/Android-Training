package au.com.gridstone.trainingkotlin.main

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import au.com.gridstone.trainingkotlin.imgur.ImgurPost
import au.com.gridstone.trainingkotlin.R
import au.com.gridstone.trainingkotlin.main.MainActivity.State.FEED
import au.com.gridstone.trainingkotlin.main.MainActivity.State.POST

class MainActivity : AppCompatActivity(),
    ImgurFeedFragment.OnListFragmentInteractionListener,
    ImgurPostFragment.OnFragmentInteractionListener {

  enum class State { FEED,
    POST
  }

  private val fragmentManager = supportFragmentManager

  private var state = State.FEED
  private var feed: Fragment? = null
  private var post: Fragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (savedInstanceState == null) {
      feed = ImgurFeedFragment.newInstance(1)
      showFeed()
    }
  }

  fun transitFragment(
    toTransit: Fragment,
    addToBackStack: Boolean
  ) {
    val fragmentTransaction = fragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.frameContainer, toTransit)
    if (addToBackStack) fragmentTransaction.addToBackStack(toTransit.toString())
    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    fragmentTransaction.commit()
  }

  private fun showFeed() {
    state = FEED
    transitFragment(feed!!, false)
  }

  private fun showPost(addToBackStack: Boolean) {
    state = POST
    transitFragment(post!!, addToBackStack)
  }

  override fun onListFragmentInteraction(item: ImgurPost?) {
    post = ImgurPostFragment.newInstance(item!!)
    showPost(true)
  }

  override fun onFragmentInteraction(uri: Uri) {

  }

  override fun onSaveInstanceState(
    outState: Bundle?
  ) {
    if (outState != null)
      outState.putInt(KEY_STATE, state.ordinal)
    fragmentManager.putFragment(outState, KEY_FEED_FRAGMENT, feed)
    if (post != null && post!!.isAdded)
      fragmentManager.putFragment(outState, KEY_POST_FRAGMENT, post)
    super.onSaveInstanceState(outState)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    if (savedInstanceState != null) {
      feed = fragmentManager.getFragment(savedInstanceState, KEY_FEED_FRAGMENT)
      val tempPost = fragmentManager.getFragment(savedInstanceState, KEY_POST_FRAGMENT)
      if (tempPost != null && tempPost is ImgurPostFragment) {
        post = tempPost
      }
      val index = savedInstanceState.getInt(KEY_STATE, 0)
      state = State.values()[index]
      when (state) {
        FEED -> showFeed()
        POST -> showPost(true)
      }
    }
  }

  override fun onBackPressed() {
    state = FEED
    super.onBackPressed()
  }

  companion object {
    const val KEY_FEED_FRAGMENT = "feed_fragment"
    const val KEY_POST_FRAGMENT = "post_fragment"
    const val KEY_STATE = "state"

  }
}
