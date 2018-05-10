package au.com.gridstone.trainingkotlin

import android.app.Application
import com.squareup.leakcanary.LeakCanary

class ImgurApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }
    LeakCanary.install(this)
  }
}
