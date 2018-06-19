package com.nunes.eduardo.videoapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.fragment_exo_player_io.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

private const val ARG_PARAM1 = "source"
private const val ARG_PARAM2 = "title"

class ExoPlayerIOFragment : Fragment() {
    private var player: SimpleExoPlayer? = null
    private var adsLoader: ImaAdsLoader? = null
    private var videoSrc: String? = null
    private var videoTitle: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoSrc = it.getString(ARG_PARAM1)
            videoTitle = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exo_player_io, container, false)
    }

    override fun onStart() {
        super.onStart()
        player = ExoPlayerFactory.newSimpleInstance(activity, DefaultTrackSelector())

        val reader: Reader = BufferedReader( InputStreamReader(resources.openRawResource(R.raw.tag), "UTF8"))

        reader.use {
            adsLoader = ImaAdsLoader(activity, Uri.parse(Uri.decode(it.readText())))
        }

        exoPlayerView.player = player
        exoPlayerView.useController = true
        exoPlayerView.controllerHideOnTouch = true
        exoPlayerView.controllerAutoShow = true

        val dataSourceFactory = DefaultDataSourceFactory(activity,
                Util.getUserAgent(activity, "exo-demo"))

        var mediaSource: ExtractorMediaSource? = null
        videoSrc?.let {
            mediaSource = ExtractorMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(Uri.decode(it)))
        }

        val adsMediaSource = AdsMediaSource(mediaSource, dataSourceFactory, adsLoader,
                exoPlayerView.overlayFrameLayout)

        player?.prepare(adsMediaSource)

//        mediaSource?.let {
//            player?.prepare(it)
//        }

        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()

        exoPlayerView.player = null
        player?.release()
        player = null
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        @JvmStatic
        fun newInstance(source: String?, title: String?) =
                ExoPlayerIOFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, source)
                        putString(ARG_PARAM2, title)
                    }
                }
    }
}
