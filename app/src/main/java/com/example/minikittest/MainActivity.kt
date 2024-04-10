package com.example.minikittest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.minikittest.ui.theme.MiniKitTestTheme
import android.util.Log
import org.json.JSONObject
import android.content.Context
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AlertDialog
import android.app.Activity


class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniKitTestTheme {
                FullscreenWebView { webView = it }
            }
        }
        hideSystemUI()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }



    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}

@Composable
fun FullscreenWebView(modifier: Modifier = Modifier.fillMaxSize(), onWebViewCreated: (WebView) -> Unit = {}) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                addJavascriptInterface(WebAppInterface(context, webView = this), "Android")
                onWebViewCreated(this)
                loadUrl("https://b209-209-214-34-58.ngrok-free.app ") // Specify the URL here
            }
        },
        modifier = modifier
    )
}


class WebAppInterface(private val context: Context, private val webView: WebView) {
    @JavascriptInterface
    fun miniKit(data: String) {
        // Parse the JSON data
        val jsonObject = JSONObject(data)
        val activity = jsonObject.getString("activity")

        // Handle the data (e.g., start a new activity, show a dialog, etc.)
        // For demonstration, we're just logging the received data
        Log.i("WebAppInterface", "Activity: $activity")
        Log.i("WebAppInterface", "JSON: $jsonObject")

        // Optionally, perform actions based on the received data
        if(activity == "verify") {
            Log.i("Verify", "Activity: $activity")
            showVerificationModal()
            webView.loadURl(redirectUrl)
            // Execute your logic here
        }
    }
    private fun showVerificationModal() {
        Log.i("WebAppInterface", "Attempting to show verification modal.")
        val activityContext = context as? Activity
        if (activityContext != null) {
            activityContext.runOnUiThread {
                try {
                    AlertDialog.Builder(activityContext).apply {
                        setTitle("Verification Needed")
                        setMessage("Please verify your action.")
                        setPositiveButton("Verify") { dialog, which ->
                            webView.evaluateJavascript("""
                            (function() {
                                var event = new Event('MinikitPayment', {status: "hello"});
                                document.dispatchEvent(event);
                            })();
                        """, null)
                            Log.i("Verification", "User chose to verify and MinikitPayment called.")
                        }
                        setNegativeButton("Cancel", null)
                        show()
                    }
                } catch (e: Exception) {
                    Log.e("WebAppInterface", "Error showing verification modal", e)
                }
            }
        } else {
            Log.e("WebAppInterface", "Context is not an Activity or is null")
        }
    }


}