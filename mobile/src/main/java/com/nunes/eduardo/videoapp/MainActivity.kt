package com.nunes.eduardo.videoapp

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import java.io.BufferedReader
import java.io.InputStreamReader
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nunes.eduardo.videoapp.model.Response


class MainActivity : ExoPlayerIOFragment.OnFragmentInteractionListener, AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val mediaResponseJson = BufferedReader(InputStreamReader(mediaResponseRaw))
        return defaultGsonBuilder.fromJson<Response>(mediaResponseJson, object : TypeToken<Response>() {}.type)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}
