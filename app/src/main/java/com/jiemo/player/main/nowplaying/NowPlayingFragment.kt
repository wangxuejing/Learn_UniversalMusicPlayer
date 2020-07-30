package com.jiemo.player.main.nowplaying

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.android.uamp.R
import com.jiemo.player.main.utils.InjectorUtils
import com.jiemo.player.main.nowplaying.NowPlayingFragmentViewModel.NowPlayingMetadata
import com.jiemo.player.main.MainActivityViewModel

/**
 * A fragment representing the current media item being played.
 */
class NowPlayingFragment : Fragment() {
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var nowPlayingViewModel: NowPlayingFragmentViewModel
    private lateinit var positionTextView: TextView
    private lateinit var mSeekBar: SeekBar

    companion object {
        fun newInstance() = NowPlayingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_nowplaying, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Always true, but lets lint know that as well.
        val context = activity ?: return

        // Inject our activity and view models into this fragment
        mainActivityViewModel = ViewModelProviders
                .of(context, InjectorUtils.provideMainActivityViewModel(context))
                .get(MainActivityViewModel::class.java)
        nowPlayingViewModel = ViewModelProviders
                .of(context, InjectorUtils.provideNowPlayingFragmentViewModel(context))
                .get(NowPlayingFragmentViewModel::class.java)

        // Attach observers to the LiveData coming from this ViewModel
        nowPlayingViewModel.mediaMetadata.observe(this,
                Observer { mediaItem -> updateUI(view, mediaItem) })
        nowPlayingViewModel.mediaButtonRes.observe(this,
                Observer { res -> view.findViewById<ImageView>(R.id.media_button).setImageResource(res) })
        nowPlayingViewModel.mediaPosition.observe(this,
                Observer { pos ->
                    positionTextView.text =
                            NowPlayingMetadata.timestampToMSS(context, pos)
                    updateProgress(pos)
                })
        nowPlayingViewModel.repeatButtonRes.observe(this,
                Observer { res -> view.findViewById<ImageView>(R.id.repeat_mode_button).setImageResource(res) })


        // Setup UI handlers for buttons
        view.findViewById<ImageButton>(R.id.media_button).setOnClickListener {
            nowPlayingViewModel.mediaMetadata.value?.let { mainActivityViewModel.playMediaId(it.id) }
        }

        view.findViewById<ImageButton>(R.id.repeat_mode_button).setOnClickListener {
            nowPlayingViewModel.repeatMode.value?.let { mainActivityViewModel.setRepeatMode((it + 1) % 3) }
        }

        view.findViewById<ImageButton>(R.id.next).setOnClickListener {
            nowPlayingViewModel.skipNext()
        }

        view.findViewById<ImageButton>(R.id.previous).setOnClickListener {
            nowPlayingViewModel.skipToPrevious()
        }

        mSeekBar = view.findViewById<SeekBar>(R.id.seekBar)
                .apply {
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            var duration = nowPlayingViewModel.mediaDuration.value ?: 0
                            if (duration <= 0) {
                                return
                            }
                            val position = seekBar?.progress?.times(duration / 100) ?: 0
                            mainActivityViewModel.seekTo(position)
                        }

                    })
                }

        // Initialize playback duration and position to zero
        view.findViewById<TextView>(R.id.duration).text =
                NowPlayingMetadata.timestampToMSS(context, 0L)

        positionTextView = view.findViewById<TextView>(R.id.position)
                .apply { text = NowPlayingMetadata.timestampToMSS(context, 0L) }
    }

    private fun updateProgress(pos: Long) {
        var duration = nowPlayingViewModel.mediaDuration.value ?: 1
        if (duration <= 0) {
            return
        }
        val progress = (pos.times(100) / duration).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mSeekBar.setProgress(progress, false)
        } else {
            mSeekBar.progress = progress
        }
    }

    /**
     * Internal function used to update all UI elements except for the current item playback
     */
    private fun updateUI(view: View, metadata: NowPlayingFragmentViewModel.NowPlayingMetadata) {
        val albumArtView = view.findViewById<ImageView>(R.id.albumArt)
        if (metadata.albumArtUri == Uri.EMPTY) {
            albumArtView.setImageResource(R.drawable.ic_album_black_24dp)
        } else {
            Glide.with(view)
                    .load(metadata.albumArtUri)
                    .into(albumArtView)
        }
        view.findViewById<TextView>(R.id.title).text = metadata.title
        view.findViewById<TextView>(R.id.subtitle).text = metadata.subtitle
        view.findViewById<TextView>(R.id.duration).text = metadata.duration
    }
}


