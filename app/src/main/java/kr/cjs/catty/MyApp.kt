package kr.cjs.catty

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}