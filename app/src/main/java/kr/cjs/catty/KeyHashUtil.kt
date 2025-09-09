package kr.cjs.catty

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

object KeyHashUtil {
    fun getHashKey(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signatures = packageInfo.signingInfo!!.apkContentsSigners
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val hashKey = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                    Log.d("KeyHash", "Hash key: $hashKey")
                }
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "Error getting hash key", e)
        }
    }
}