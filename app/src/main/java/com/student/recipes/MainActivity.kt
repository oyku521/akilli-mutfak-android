package com.student.recipes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.webkit.WebViewAssetLoader

class MainActivity : Activity() {
    companion object {
        const val LIVE_URL = "https://oyku521.github.io/gastronomy-task3-yaz./"
        const val BUNDLED_URL = "https://appassets.androidplatform.net/assets/index.html"
        fun isLiveSite(uri: Uri): Boolean = uri.scheme == "https" &&
            uri.host == "oyku521.github.io" && uri.path?.startsWith("/gastronomy-task3-yaz./") == true
        fun isBundledSite(uri: Uri): Boolean = uri.scheme == "https" &&
            uri.host == "appassets.androidplatform.net" && uri.path?.startsWith("/assets/") == true
    }

    private lateinit var webView: WebView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private val mainHandler = Handler(Looper.getMainLooper())
    private var liveMode = true
    private var navigation = 0
    private var ready = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.recipeWebView)
        status = findViewById(R.id.modeStatus)
        progress = findViewById(R.id.pageProgress)
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            setSupportMultipleWindows(false)
            // Fresh HTTPS requests on each live visit. Android supplies its own offline snapshot.
            cacheMode = WebSettings.LOAD_NO_CACHE
            userAgentString = "$userAgentString SmartKitchenAndroid/2.0"
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (isLiveSite(uri) || isBundledSite(uri)) return false
                if (request.isForMainFrame && (uri.scheme == "https" || uri.scheme == "http")) openExternal(uri)
                return true
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val uri = Uri.parse(url)
                if (isLiveSite(uri) || isBundledSite(uri)) return false
                if (uri.scheme == "https" || uri.scheme == "http") openExternal(uri)
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                val uri = Uri.parse(url)
                liveMode = when {
                    isLiveSite(uri) -> true
                    isBundledSite(uri) -> false
                    else -> return
                }
                val token = ++navigation
                ready = false
                mainHandler.removeCallbacksAndMessages(null)
                progress.visibility = View.VISIBLE
                status.setText(if (liveMode) R.string.loading_live else R.string.loading_bundled)
                mainHandler.postDelayed({ if (token == navigation && !ready) loadFailed() }, 15000)
            }

            override fun onPageFinished(view: WebView, url: String) {
                val uri = Uri.parse(url)
                if ((liveMode && isLiveSite(uri)) || (!liveMode && isBundledSite(uri))) checkReady(navigation)
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: android.webkit.WebResourceError) {
                if (request.isForMainFrame && liveMode && isLiveSite(request.url)) showBundled()
            }

            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                if (request.isForMainFrame && liveMode && isLiveSite(request.url) && response.statusCode >= 400) showBundled()
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                // Never bypass TLS verification. The offline snapshot remains available.
                handler.cancel()
                if (liveMode) showBundled()
            }
        }
        findViewById<Button>(R.id.liveButton).setOnClickListener { showLive() }
        findViewById<Button>(R.id.offlineButton).setOnClickListener { showBundled() }
        // Visiting the live website on every launch fetches site updates without rebuilding the APK.
        showLive()
    }

    fun showLive() {
        liveMode = true
        navigation++
        ready = false
        mainHandler.removeCallbacksAndMessages(null)
        webView.stopLoading()
        progress.visibility = View.VISIBLE
        status.setText(R.string.loading_live)
        webView.loadUrl(LIVE_URL)
    }

    fun showBundled() {
        liveMode = false
        navigation++
        ready = false
        mainHandler.removeCallbacksAndMessages(null)
        webView.stopLoading()
        progress.visibility = View.VISIBLE
        status.setText(R.string.loading_bundled)
        webView.loadUrl(BUNDLED_URL)
    }

    private fun checkReady(token: Int) {
        if (token != navigation || ready || isFinishing || isDestroyed) return
        webView.evaluateJavascript("Boolean(document.querySelector('.recipe-card') || document.querySelector('.empty'))") { value ->
            if (token != navigation || isFinishing || isDestroyed) return@evaluateJavascript
            if (value == "true") {
                ready = true
                mainHandler.removeCallbacksAndMessages(null)
                progress.visibility = View.GONE
                status.setText(if (liveMode) R.string.mode_live else R.string.mode_bundled)
            } else {
                mainHandler.postDelayed({ checkReady(token) }, 250)
            }
        }
    }

    private fun loadFailed() {
        if (liveMode) showBundled() else {
            mainHandler.removeCallbacksAndMessages(null)
            progress.visibility = View.GONE
            status.setText(R.string.load_error)
        }
    }

    private fun openExternal(uri: Uri) {
        try { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        catch (_: ActivityNotFoundException) { Toast.makeText(this, R.string.open_error, Toast.LENGTH_LONG).show() }
    }

    @Deprecated("Legacy back callback supports Android 6 and newer")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        navigation++
        mainHandler.removeCallbacksAndMessages(null)
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
