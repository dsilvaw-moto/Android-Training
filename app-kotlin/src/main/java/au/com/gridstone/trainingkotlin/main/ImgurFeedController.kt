package au.com.gridstone.trainingkotlin.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.gridstone.trainingkotlin.R
import com.bluelinelabs.conductor.Controller

class ImgurFeedController : Controller() {



  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup
  ): View {
    val view = inflater.inflate(R.layout.fragment_imgur_feed_list, container, false)
    initView(view)
    return view;
  }

  private fun initView(view: View?) {

  }

}