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
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.fragment_exo_player_io.*

private const val ARG_PARAM1 = "source"
private const val ARG_PARAM2 = "title"

class ExoPlayerIOFragment : Fragment() {
    private var player: SimpleExoPlayer? = null
    private var videoSource: String? = null
    private var videoTitle: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoSource = it.getString(ARG_PARAM1)
            videoTitle = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exo_player_io, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player = ExoPlayerFactory.newSimpleInstance(activity, DefaultTrackSelector())

        exoPlayerView.player = player

        val dataSourceFactory = DefaultDataSourceFactory(activity,
                Util.getUserAgent(activity, "exo-demo"))

        videoSource?.let {
            ExtractorMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(Uri.decode(it)))
        }

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
