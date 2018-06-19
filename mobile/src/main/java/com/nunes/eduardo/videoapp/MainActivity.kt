package com.nunes.eduardo.videoapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import com.google.android.exoplayer2.offline.DownloadService.startWithAction
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nunes.eduardo.videoapp.model.Response
import org.jetbrains.anko.alert
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

private const val REQUEST_READ_PERSMISSION_CODE = 3
private const val REQUEST_WRITE_PERSMISSION_CODE = 2

class MainActivity : ExoPlayerIOFragment.OnFragmentInteractionListener, AppCompatActivity() {

    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGoogleIOExample()
    }

    private fun initGoogleIOExample() {
        val response = getResponseFromJson()

        val movie = response.categories?.first()?.videos?.first()

        val exoPlayerIOFragment = ExoPlayerIOFragment.newInstance(movie?.sources?.first(), movie?.title)

        supportFragmentManager
                ?.beginTransaction()
                ?.add(R.id.rootView, exoPlayerIOFragment)
                ?.commitNow()
    }

    private val defaultGsonBuilder: Gson
        get() = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create()

    private fun getResponseFromJson(): Response{
        val mediaResponseRaw = resources.openRawResource(R.raw.media, TypedValue())
        val mediaResponseJson = BufferedReader(InputStreamReader(mediaResponseRaw) as Reader?)
        return defaultGsonBuilder.fromJson<Response>(mediaResponseJson, object : TypeToken<Response>() {}.type)
    }

    override fun onFragmentInteraction(uri: Uri) {
        this.uri = uri
        if (isReadStoragePermissionGranted()) {
            isWriteStoragePermissionGranted()
        }
    }

    private fun startDownloadService() {
        val downloadAction = ProgressiveDownloadAction(this.uri, false,
                null, null)

        startWithAction(this@MainActivity, VideoDownloadService::class.java,
                downloadAction, true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_PERSMISSION_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("videoDownloader", "Permission: " + permissions[0] + "was " + grantResults[0])
                    //resume tasks needing this permission
                    isWriteStoragePermissionGranted()
                } else {
                    showIsNeedDialog()
                }
            }

            REQUEST_WRITE_PERSMISSION_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("videoDownloader", "Permission: " + permissions[0] + "was " + grantResults[0])
                    startDownloadService()
                } else {
                    showIsNeedDialog()
                }
            }
        }
    }

    private fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_PERSMISSION_CODE)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    private fun isWriteStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("videoDownloader", "Permission write external storage: is granted")
                startDownloadService()
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERSMISSION_CODE)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            startDownloadService()
            true
        }
    }

    private fun showIsNeedDialog() {
        alert("Voce precisa permitir o app ler e escrever no storage") {
            title = "Importante!"
            positiveButton("OK") {
                if (isReadStoragePermissionGranted()) {
                    isWriteStoragePermissionGranted()
                }
            }
            negativeButton("Cancelar") { it.dismiss() }
        }.show()
    }
}
