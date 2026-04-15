package com.vishaltelangre.nerdcalci.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

object IntentUtils {
    /**
     * Opens the specified URL in a web browser.
     */
    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("IntentUtils", "No app can open this link: $url", e)
            Toast.makeText(context, "No app can open this link", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("IntentUtils", "Could not open link: $url", e)
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }
}
