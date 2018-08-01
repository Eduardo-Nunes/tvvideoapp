package com.nunes.eduardo.playerLib

import android.content.Context
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.View
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.DefaultEventListener
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.custom_playback_control.*
import org.jetbrains.anko.startActivity
import java.io.IOException
import java.lang.Exception

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

private const val STATE_IDLE =      "ExoPlayer.STATE_IDLE      -"
private const val STATE_BUFFERING = "ExoPlayer.STATE_BUFFERING -"
private const val STATE_READY =     "ExoPlayer.STATE_READY     -"
private const val STATE_ENDED =     "ExoPlayer.STATE_ENDED     -"
private const val UNKNOWN_STATE =   "UNKNOWN_STATE             -"
private const val TAG = "player"

class PlayerActivity : AppCompatActivity() {

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hideSystemUi() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var player: SimpleExoPlayer? = null
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private var playWhenReady: Boolean = true
    private val bandwidthMeter = DefaultBandwidthMeter()
    private var componentListener: ComponentListener? = null
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initData()
        initListeners()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hideSystemUi() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initViews() {
        setContentView(R.layout.activity_player)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mVisible = true
    }

    private fun initData() {
        uri = Uri.parse(intent.extras.getString(EXTRA_MEDIA))
    }

    private fun initListeners() {
        // Set up the user interaction to manually showSystemUi or hideSystemUi the system UI.
        fullscreen_content.setOnClickListener { toggleSystemUi() }

        // Upon interacting with UI controls, delay any scheduled hideSystemUi()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummy_button.setOnTouchListener(mDelayHideTouchListener)

        componentListener = ComponentListener {

        }
    }

    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(this),
                DefaultTrackSelector(
                        AdaptiveTrackSelection.Factory(bandwidthMeter)
                ),
                DefaultLoadControl()
        )

        exoPlayerView.player = player

        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
        player?.addListener(componentListener)
        player?.addAnalyticsListener(componentListener)

        val mediaSource = buildMediaSource(uri)

        player?.prepare(mediaSource, false, false)
    }

    private fun buildMediaSource(uri: Uri): MediaSource{
        val manifestDataSourceFactory = DefaultHttpDataSourceFactory("ua")
        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(
                DefaultHttpDataSourceFactory("ua", bandwidthMeter)
        )

        return DashMediaSource.Factory(
                dashChunkSourceFactory,
                manifestDataSourceFactory
        ).createMediaSource(uri)
    }

    private fun releasePlayer(){
        player?.let { simpleExoPlayer ->
            playbackPosition = simpleExoPlayer.currentPosition
            currentWindow = simpleExoPlayer.currentWindowIndex
            playWhenReady = simpleExoPlayer.playWhenReady
            simpleExoPlayer.removeListener(componentListener)
            simpleExoPlayer.release()
            player = null
        }
    }

    private fun toggleSystemUi() {
        if (mVisible) {
            hideSystemUi()
        } else {
            showSystemUi()
        }
    }

    private fun hideSystemUi() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun showSystemUi() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hideSystemUi() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        const val EXTRA_MEDIA = "PlayerActivity.MEDIA_SOURCE"

        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300

        fun createIntent(context: Context, media: String) {
            context.startActivity<PlayerActivity>(EXTRA_MEDIA to media)
        }
    }

    class ComponentListener(val seekProceed: (eventTime: AnalyticsListener.EventTime?) -> Unit) : DefaultEventListener(), AnalyticsListener {

        override fun onPlayerStateChanged(eventTime: AnalyticsListener.EventTime?,
                                          playWhenReady: Boolean, playbackState: Int) {
            val stateString = when(playbackState){
                Player.STATE_IDLE -> STATE_IDLE
                Player.STATE_BUFFERING -> STATE_BUFFERING
                Player.STATE_READY -> STATE_READY
                Player.STATE_ENDED -> STATE_ENDED
                else -> UNKNOWN_STATE
            }

            Log.d(TAG,  "In"+ eventTime.toString() +
                    "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady)
        }

        override fun onPlaybackParametersChanged(eventTime: AnalyticsListener.EventTime?,
                                                 playbackParameters: PlaybackParameters?) {}

        override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime?) {
            seekProceed(eventTime)
        }

        override fun onTracksChanged(eventTime: AnalyticsListener.EventTime?,
                                     trackGroups: TrackGroupArray?,
                                     trackSelections: TrackSelectionArray?) {}

        override fun onPlayerError(eventTime: AnalyticsListener.EventTime?,
                                   error: ExoPlaybackException?) {}

        override fun onLoadingChanged(eventTime: AnalyticsListener.EventTime?, isLoading: Boolean) {}

        override fun onPositionDiscontinuity(eventTime: AnalyticsListener.EventTime?, reason: Int) {}

        override fun onRepeatModeChanged(eventTime: AnalyticsListener.EventTime?, repeatMode: Int) {}

        override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime?, reason: Int) {}

        override fun onSeekStarted(eventTime: AnalyticsListener.EventTime?) {}

        override fun onDownstreamFormatChanged(eventTime: AnalyticsListener.EventTime?,
                                               mediaLoadData: MediaSourceEventListener.MediaLoadData?) {}

        override fun onDrmKeysLoaded(eventTime: AnalyticsListener.EventTime?) {}

        override fun onMediaPeriodCreated(eventTime: AnalyticsListener.EventTime?) {}

        override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime?, surface: Surface?) {}

        override fun onReadingStarted(eventTime: AnalyticsListener.EventTime?) {}

        override fun onBandwidthEstimate(eventTime: AnalyticsListener.EventTime?, totalLoadTimeMs: Int,
                                         totalBytesLoaded: Long, bitrateEstimate: Long) {}

        override fun onNetworkTypeChanged(eventTime: AnalyticsListener.EventTime?,
                                          networkInfo: NetworkInfo?) {}

        override fun onViewportSizeChange(eventTime: AnalyticsListener.EventTime?, width: Int,
                                          height: Int) {}

        override fun onDrmKeysRestored(eventTime: AnalyticsListener.EventTime?) {}

        override fun onDecoderDisabled(eventTime: AnalyticsListener.EventTime?, trackType: Int,
                                       decoderCounters: DecoderCounters?) {}

        override fun onShuffleModeChanged(eventTime: AnalyticsListener.EventTime?, shuffleModeEnabled: Boolean) {}

        override fun onDecoderInputFormatChanged(eventTime: AnalyticsListener.EventTime?,
                                                 trackType: Int, format: Format?) {}

        override fun onAudioSessionId(eventTime: AnalyticsListener.EventTime?, audioSessionId: Int) {}

        override fun onDrmSessionManagerError(eventTime: AnalyticsListener.EventTime?, error: Exception?) {}

        override fun onLoadStarted(eventTime: AnalyticsListener.EventTime?,
                                   loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                   mediaLoadData: MediaSourceEventListener.MediaLoadData?) {}

        override fun onUpstreamDiscarded(eventTime: AnalyticsListener.EventTime?,
                                         mediaLoadData: MediaSourceEventListener.MediaLoadData?) {}

        override fun onLoadCanceled(eventTime: AnalyticsListener.EventTime?,
                                    loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                    mediaLoadData: MediaSourceEventListener.MediaLoadData?) {}

        override fun onMediaPeriodReleased(eventTime: AnalyticsListener.EventTime?) {}

        override fun onDecoderInitialized(eventTime: AnalyticsListener.EventTime?, trackType: Int,
                                          decoderName: String?, initializationDurationMs: Long) {}

        override fun onDroppedVideoFrames(eventTime: AnalyticsListener.EventTime?, droppedFrames: Int,
                                          elapsedMs: Long) {}

        override fun onDecoderEnabled(eventTime: AnalyticsListener.EventTime?, trackType: Int,
                                      decoderCounters: DecoderCounters?) {}

        override fun onVideoSizeChanged(eventTime: AnalyticsListener.EventTime?, width: Int,
                                        height: Int, unappliedRotationDegrees: Int,
                                        pixelWidthHeightRatio: Float) {}

        override fun onAudioUnderrun(eventTime: AnalyticsListener.EventTime?, bufferSize: Int,
                                     bufferSizeMs: Long, elapsedSinceLastFeedMs: Long) {}

        override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime?,
                                     loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                     mediaLoadData: MediaSourceEventListener.MediaLoadData?) {}

        override fun onDrmKeysRemoved(eventTime: AnalyticsListener.EventTime?) {}

        override fun onLoadError(eventTime: AnalyticsListener.EventTime?,
                                 loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                 mediaLoadData: MediaSourceEventListener.MediaLoadData?,
                                 error: IOException?, wasCanceled: Boolean) {}

        override fun onMetadata(eventTime: AnalyticsListener.EventTime?, metadata: Metadata?) {}
    }
}
