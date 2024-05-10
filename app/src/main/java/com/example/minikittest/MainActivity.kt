package com.example.minikittest

import android.annotation.SuppressLint
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
data class VerifyEventPayload(val status: String, val proof: String, val nullifier_hash: String, val verification_level: String, val merkle_root: String)
data class VerifyErrorPayload(val status: String, val error_code: String)

data class PaymentSuccessPayload(val status: String, val transaction_id: String, val from: String, val chain: String, val timestamp: String, val transaction_status: String, val reference: String)
data class PaymentErrorPayload(val status: String, val error_code: String)

data class WalletAuthSuccessPayload(val status: String, val message: String, val address: String, val signature: String)
data class WalletAuthErrorPayload(val status: String, val error_code: String, val address: String, val details: String)

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

    fun triggerMessage(action: String, payload: VerifyErrorPayload) {
        val gson = Gson()
        val jsonData = gson.toJson(payload)
        Handler(Looper.getMainLooper()).post {
            Log.i("json", "msg $jsonData")
            val jsCode = "MiniKit.trigger('${action}', ${jsonData});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    fun triggerMessage(action: String, payload: PaymentSuccessPayload) {
        val gson = Gson()
        val jsonData = gson.toJson(payload)
        Handler(Looper.getMainLooper()).post {
            Log.i("json", "msg $jsonData")
            val jsCode = "MiniKit.trigger('${action}', ${jsonData});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    fun triggerMessage(action: String, payload: PaymentErrorPayload) {
        val gson = Gson()
        val jsonData = gson.toJson(payload)
        Handler(Looper.getMainLooper()).post {
            Log.i("json", "msg $jsonData")
            val jsCode = "MiniKit.trigger('${action}', ${jsonData});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    fun triggerMessage(action: String, payload: WalletAuthSuccessPayload) {
        val gson = Gson()
        val jsonData = gson.toJson(payload)
        Handler(Looper.getMainLooper()).post {
            Log.i("json", "msg $jsonData")
            val jsCode = "MiniKit.trigger('${action}', ${jsonData});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    fun triggerMessage(action: String, payload: WalletAuthErrorPayload) {
        val gson = Gson()
        val jsonData = gson.toJson(payload)
        Handler(Looper.getMainLooper()).post {
            Log.i("json", "msg $jsonData")
            val jsCode = "MiniKit.trigger('${action}', ${jsonData});"
            webView.evaluateJavascript(jsCode, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                // Valid Production
                val verifyPayload = VerifyEventPayload("success", "0x030e27504d0c11eecd76e5442d3abc454f896ad18300c75f60f304eaf107b8772773c39fa7324beedcb9bcbfb4c765993305e612aa7919c36667e2c7fcb2477418b2cc5a4602246ba5f79a10043a12206e71ae906180ef61d19ab833d589ecd31c4c4721798c1cfbec223adb662cb2f07b3a3535c995ae313749a871ab30d8e719ea33ceff7cb2d42dcc470e500e0cda186452c5367ac0c6432ab67d6d08c3892ceb0044b198965e732eeeb0b189de2e152c80efd2d472a7711d7c6f4d2e06f8118b6915d1f0629870aee765f26c52bbd58c64288f990eb885f6898d218784ec20caac6276b22f8651c702ccd97ec3041860cf7d09f52a4f2e812623862da377", "0x22bc3b2045f7561f45bfa15b90ebf6b3bfb984d0eb05102044b2c8f972d5ae86", "orb", "0x050124e5bd32ad6a443b4f7650e10984faf1c05dcef28c27036e7ef9743833a9")
                // Valid Staging
//                val verifyPayload = VerifyEventPayload("success", "0x144211f7d68fe98749f3d46d3d230729556a61bf75d8535ada55e0062d3284d91a4d0eb7e8395920a7c6bc9560c1f9e7bdae82038bf8ec8728b5c62f64a3a5091932bb7f6f4f216dd5fbd13b63e92bfffa46e2e64f1523713b3e7f8d7b05b4c81e16be74b52871d1cdb77caf7f21120b3be79b8e03422eeb618ee953c8ee96842ac218d531abcd3e11f76e7b9267d8cf4fc2b8adf7fba6affa8ebf6d706bafa7181bb4703d79338a9f71cda817870f305aa51e350260aebc68acd13c34dfe0ec10b220deaa80a9b9f546686800bdda6dd8208c068471e1cde5f00ccd4a3fa5ba272ee325825274c1c6091a2311e24dedc3f0f7896ea59009c7f9520ed304c8bd", "0x19410e85a0b8e321b236de30e3a225e11f6cc1a79a1d4b65d2146d9698c9cbba", "orb", "0x2250fdb75d9073c1e022e14b8ba989b10fac99601fbb40e2abbaf84d6cbf99e8")
//              Test Error
//              val errorPayload = VerifyErrorPayload("error", "invaliffd_network")
                triggerMessage("miniapp-verify-action", verifyPayload);
                Log.i("WebView", "Delayed Verify Command Executed with payload $verifyPayload")
            }
        }

        if(command == "pay") {
            val payloadString = jsonObject.getString("payload")
            Log.i("WebView", "Pay payload: $payloadString")
            val payload = jsonObject.getJSONObject("payload")
            delayFunctionCallTimer(3000) { // Delay the response by 3000 milliseconds
                val currentTimestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                // Valid Payload
                val paymentInitiatedPayload = PaymentSuccessPayload("success", "98591778-f3d5-4feb-b027-ec55aa22f40e", "0x1203921032u30123231wefwef11231231231", "optimism", currentTimestamp, "submitted", payload.get("reference").toString())
                // Error Payload
                val paymentErrorPayload = PaymentErrorPayload("error", "invalid_receiver")
                triggerMessage("miniapp-payment", paymentInitiatedPayload);
                Log.i("WebView", "Delayed Verify Command Executed with payload $paymentInitiatedPayload")
            }
        }

        if(command == "wallet-auth") {
            val payload = jsonObject.getString("payload")
            Log.i("WebView", "Wallet Auth payload: $payload")
            delayFunctionCallTimer(3000) { // Delay the response by 3000 milliseconds
                // Valid Production
                val walletAuth = WalletAuthSuccessPayload("success", "https://test.com wants you to sign in with your Ethereum account:\n" +
                        "0x52fcC6871c8CF3BfD8A5E455E1CF125d3d0AD558\n" +
                        "\n" +
                        "statement\n" +
                        "\n" +
                        "URI: https://test.com\n" +
                        "Version: 1\n" +
                        "Chain ID: 10\n" +
                        "Nonce: 12345678\n" +
                        "Issued At: 2024-05-10T02:14:34.298Z\n" +
                        "Expiration Time: 2024-05-17T02:14:34.298Z\n" +
                        "Not Before: 2024-05-03T00:00:00Z\n" +
                        "Request ID: 0", "0x52fcC6871c8CF3BfD8A5E455E1CF125d3d0AD558", "0xf50c4ed9cab084b27ecd788b49fe2ca96c97bba7c18f770a82e51921d4de195537375cc7d49f36006bed488cf501d18bf1d115090fe3bb2cc7b7d8979169adf31b")
                // Valid Staging
//                val verifyPayload = VerifyEventPayload("success", "0x144211f7d68fe98749f3d46d3d230729556a61bf75d8535ada55e0062d3284d91a4d0eb7e8395920a7c6bc9560c1f9e7bdae82038bf8ec8728b5c62f64a3a5091932bb7f6f4f216dd5fbd13b63e92bfffa46e2e64f1523713b3e7f8d7b05b4c81e16be74b52871d1cdb77caf7f21120b3be79b8e03422eeb618ee953c8ee96842ac218d531abcd3e11f76e7b9267d8cf4fc2b8adf7fba6affa8ebf6d706bafa7181bb4703d79338a9f71cda817870f305aa51e350260aebc68acd13c34dfe0ec10b220deaa80a9b9f546686800bdda6dd8208c068471e1cde5f00ccd4a3fa5ba272ee325825274c1c6091a2311e24dedc3f0f7896ea59009c7f9520ed304c8bd", "0x19410e85a0b8e321b236de30e3a225e11f6cc1a79a1d4b65d2146d9698c9cbba", "orb", "0x2250fdb75d9073c1e022e14b8ba989b10fac99601fbb40e2abbaf84d6cbf99e8")
//              Test Error
//              val errorPayload = VerifyErrorPayload("error", "invaliffd_network")
                triggerMessage("miniapp-wallet-auth", walletAuth);
                Log.i("WebView", "Delayed Verify Command Executed with payload $walletAuth")
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
        loadUrl("https://515e-2800-810-446-795-197e-58dd-b1aa-58cf.ngrok-free.app") // Specify the URL here
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

