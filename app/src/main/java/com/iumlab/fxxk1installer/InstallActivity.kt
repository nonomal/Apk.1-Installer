package com.iumlab.fxxk1installer

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.alibaba.fastjson.JSON
import com.iumlab.fxxk1installer.ui.components.setSystemBar
import com.iumlab.fxxk1installer.ui.theme.AppTheme
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class InstallActivity : ComponentActivity() {
    private val TAG = this.javaClass.name.toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.Transparent.hashCode()
        window.navigationBarColor = Color.Transparent.hashCode()

        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                setSystemBar()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Home()
                }
            }
        }
        if (intent != null) {
            handleShared(intent)
            Log.e(TAG, JSON.toJSONString(intent))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e(TAG, "onNewIntent: " )
        handleShared(intent)
    }

    private fun handleShared(i: Intent?) {
        Log.e(TAG, "handleShared: ${i == null}" )
        val intent: Intent? = i ?: intent
        val action: String? = intent?.action
        val type: String? = intent?.type
        val uri: Uri? = intent?.data

        val filePath =  uri?.encodedPath
        if (filePath== null) {
            return
        }
        if (filePath!!.endsWith("apk.1")){
            copyFile(uri)
        } else {
            install(i)
        }

    }

    open fun copyFile(uri : Uri) {
        try {
            val inputStream: InputStream =
                contentResolver.openInputStream(uri) ?: return
            val path = this.getExternalFilesDir("cache")!!.absolutePath+ "/temp.apk"
            val outputStream: OutputStream = FileOutputStream(path)
            copyStream(inputStream, outputStream, path)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun copyStream(input: InputStream, output: OutputStream, path: String) { //文件存储
        val BUFFER_SIZE = 1024 * 2
        val buffer = ByteArray(BUFFER_SIZE)
        val in0 = BufferedInputStream(input, BUFFER_SIZE)
        val out = BufferedOutputStream(output, BUFFER_SIZE)
        var count = 0
        var n = 0
        try {
            while (in0.read(buffer, 0, BUFFER_SIZE).also { n = it } != -1) {
                out.write(buffer, 0, n)
                count += n
            }
            out.flush()
            out.close()
            in0.close()
            val id = application.packageName
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = FileProvider.getUriForFile(
                this,
                "$id.provider",
                File(path)
            )
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            install(intent)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun install(apk : File) {
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        val contentUri = FileProvider.getUriForFile(this, mAuthority, apk)
//        intent.setDataAndType(contentUri, INTENT_TYPE)
//        mActivity.startActivity(intent)
    }

    protected open fun install(intent: Intent?) {
        if (intent == null) {
            return
        }
        val dataUri = intent.data
//        val intent = packageManager.getLaunchIntentForPackage("com.android.packageinstaller")!!
        val intent = Intent(Intent.ACTION_VIEW)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION /*   | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/
        )
        intent.setDataAndType(dataUri, "application/vnd.android.package-archive")
        startActivity(intent)
    }

    private fun install2 (intent: Intent?) {

        startActivity(intent)
    }


    private fun checkPermissions() {
        val b = packageManager.canRequestPackageInstalls()
        if (b) {
            handleShared(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES),
                100
            )
        }

    }
}
