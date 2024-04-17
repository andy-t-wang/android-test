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
import android.webkit.JavascriptInterface
import android.os.Handler
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment


class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniKitTestTheme {
                val showWebView = remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = {
                        Log.i("WebAppInterface", "Button click!")
                        showWebView.value = true
                    }) {
                        Text("Load WebView")
                    }
                }

                if (showWebView.value) {
                    FullscreenWebView(onClose = {showWebView.value = false}) {webView = it}
                }
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FullscreenWebView(modifier: Modifier = Modifier.fillMaxSize(),  onClose: () -> Unit, onWebViewCreated: (WebView) -> Unit = {} ) {
    val context = LocalContext.current
    val webView = WebView(context)

    // Intercept browser console.logs
    webView.webChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            Log.d("WebView Console", consoleMessage.message())
            return true
        }
    }

    fun triggerMessage() {

        val randomNumber = (100..999).random()
        val proof = "0x$randomNumber"

        Handler(Looper.getMainLooper()).post {
            val jsCode = "MiniKit.trigger('miniapp-verify-action', {payload: {proof: '$proof'}});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    val jsInterface = JsInterface { handleEventMessage(message = it, onClose, triggerMessage = ::triggerMessage) }

    val webViewApply = webView.apply {
        webViewClient = WebViewClient()
        settings.javaScriptEnabled = true
        addJavascriptInterface(jsInterface, "Android")
        onWebViewCreated(this)
        loadUrl("https://9ad8-209-214-34-58.ngrok-free.app") // Specify the URL here
    }

    AndroidView(
        factory = { webViewApply },
        modifier = modifier
    )
}

class JsInterface(val onWebViewEvent: (String) -> Unit) {
    //This wat minikit is callable on the js side too, but I didn't figured out how create it here to be property on JS side.
    @JavascriptInterface
    fun minikit() = Minikit(onWebViewEvent)

    inner class Minikit(private val onWebViewEvent: (String) -> Unit) {
        @JavascriptInterface
        fun sendEvent(message: String) {
            onWebViewEvent(message)
        }
    }
}

private fun handleEventMessage(message: String, onClose: () -> Unit, triggerMessage: () -> Unit) {
    val jsonObject = JSONObject(message)
    val command = jsonObject.getString("command")

    // Handle the data (e.g., start a new activity, show a dialog, etc.)
    // For demonstration, we're just logging the received data
    Log.i("WebAppInterface", "Command: $command")
    Log.i("WebAppInterface", "JSON: $jsonObject")

    if(command == "close") {
        triggerMessage()
//        onClose()
    }

    if(command == "verify") {
        val payload = jsonObject.getString("payload")
        Log.i("WebView", "Verify payload: $payload")
    }

    if(command == "pay") {
        val app_id = jsonObject.getString("app_id")
        Log.i("WebView", "Pay app_id: $app_id")

        val payload = jsonObject.getString("payload")
        Log.i("WebView", "Pay payload: $payload")
    }

    if(command == "trigger") {
        triggerMessage()
    }
}
