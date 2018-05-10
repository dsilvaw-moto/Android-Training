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
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.activity_main.frameContainer

class MainActivity : AppCompatActivity() {

  enum class State { FEED,
    POST
  }

  private var state = State.FEED

  private lateinit var router: Router;

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // Conductor
    router = Conductor.attachRouter(this, frameContainer, savedInstanceState)
    if (!router.hasRootController()) {
      router.setRoot(RouterTransaction.with(ImgurFeedController()))
    }
  }

  override fun onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed()
    }
  }

  companion object {
    const val KEY_FEED_FRAGMENT = "feed_fragment"
    const val KEY_POST_FRAGMENT = "post_fragment"
    const val KEY_STATE = "state"
  }
}
