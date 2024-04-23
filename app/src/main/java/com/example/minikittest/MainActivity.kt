package com.example.minikittest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.google.gson.Gson
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
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
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

// Igor: You will need to create classes for the errors
data class VerifyEventPayload(val status: String, val proof: String, val action: String, val nullifier_hash: String, val verification_level: String, val merkle_root: String)
data class PaymentInitiatedPayload(val transaction_hash: String, val status: String, val from: String, val chain: String, val timestamp: String, val signature: String)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FullscreenWebView(modifier: Modifier = Modifier.fillMaxSize(), onClose: () -> Unit, onWebViewCreated: (WebView) -> Unit = {}) {
    val context = LocalContext.current
    val webView = WebView(context)

    // Intercept browser console.logs
    webView.webChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            Log.d("WebView Console", consoleMessage.message())
            return true
        }
    }

    fun delayFunctionCallTimer(i: Long, function: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(function, i)
    }

    fun triggerMessage(action: String, payload: VerifyEventPayload) {
        val gson = Gson()
        val jsonData = gson.toJson(payload)
        Handler(Looper.getMainLooper()).post {
            Log.i("json", "msg $jsonData")
            val jsCode = "MiniKit.trigger('${action}', ${jsonData});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    fun triggerMessage(action: String, payload: PaymentInitiatedPayload) {
        val gson = Gson()
        val jsonData = gson.toJson(payload)
        Handler(Looper.getMainLooper()).post {
            Log.i("json", "msg $jsonData")
            val jsCode = "MiniKit.trigger('${action}', ${jsonData});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    fun handleEventMessage(message: String, onClose: () -> Unit) {
        val jsonObject = JSONObject(message)
        val command = jsonObject.getString("command")

        // Handle the data (e.g., start a new activity, show a dialog, etc.)
        // For demonstration, we're just logging the received data
        Log.i("WebAppInterface", "Command: $command")
        Log.i("WebAppInterface", "JSON: $jsonObject")

        if(command == "verify") {
            val payload = jsonObject.getString("payload")
            Log.i("WebView", "Verify payload: $payload")
            delayFunctionCallTimer(3000) { // Delay the response by 3000 milliseconds
                val verifyPayload = VerifyEventPayload("success", "0x", "miniapp-verify-action", "0x", "device", "0x")
                triggerMessage("miniapp-verify-action", verifyPayload);
                Log.i("WebView", "Delayed Verify Command Executed with payload $verifyPayload")
            }
        }

        if(command == "pay") {
            val payload = jsonObject.getString("payload")
            Log.i("WebView", "Pay payload: $payload")
            delayFunctionCallTimer(3000) { // Delay the response by 3000 milliseconds
                val paymentInitiatedPayload = PaymentInitiatedPayload("0x1234123", "initiated", "0x1231231", "optimism", "123123124124", "0x1231231231")
                triggerMessage("miniapp-payment", paymentInitiatedPayload);
                Log.i("WebView", "Delayed Verify Command Executed with payload $paymentInitiatedPayload")
            }
        }
    }

    val jsInterface = JsInterface { handleEventMessage(message = it, onClose) }

    val webViewApply = webView.apply {
        settings.javaScriptEnabled = true
        addJavascriptInterface(jsInterface, "Android")

        // Set a custom WebViewClient
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true // Return true means the host application handles the URL
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Optional: Handle any functionality once page loads
            }
        }

        onWebViewCreated(this)
        loadUrl("https://whole-poets-listen.loca.lt") // Specify the URL here
    }


    AndroidView(
        factory = { webViewApply },
        modifier = modifier
    )
}

class JsInterface(val onWebViewEvent: (String) -> Unit) {
    @JavascriptInterface
    fun postMessage(message: String) {
        onWebViewEvent(message)
    }
}

