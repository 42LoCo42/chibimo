package forty.two.chibimo

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
enum class MediaPlayerState {
	Idle, Initialized,
	Preparing, Prepared,
	Started, Stopped, Paused,
	PlaybackComplete,
	End, Error
}

class StatefulMediaPlayer: MediaPlayer() {
	var state: MediaPlayerState = MediaPlayerState.Idle

	override fun setDataSource(context: Context, uri: Uri) {
		super.setDataSource(context, uri)
		state = MediaPlayerState.Initialized
	}

	override fun prepareAsync() {
		super.prepareAsync()
		state = MediaPlayerState.Preparing
	}

	override fun prepare() {
		super.prepare()
		state = MediaPlayerState.Prepared
	}

	override fun start() {
		super.start()
		state = MediaPlayerState.Started
	}

	override fun stop() {
		super.stop()
		state = MediaPlayerState.Stopped
	}

	override fun pause() {
		super.pause()
		state = MediaPlayerState.Paused
	}

	override fun release() {
		super.release()
		state = MediaPlayerState.End
	}

	override fun setOnErrorListener(listener: OnErrorListener?) {
		super.setOnErrorListener { p0, p1, p2 ->
			state = MediaPlayerState.Error
			listener?.onError(p0, p1, p2) ?: false
		}
	}

	override fun setOnPreparedListener(listener: OnPreparedListener?) {
		super.setOnPreparedListener {
			state = MediaPlayerState.Prepared
			listener?.onPrepared(this)
		}
	}

	override fun setOnCompletionListener(listener: OnCompletionListener?) {
		super.setOnCompletionListener {
			state = MediaPlayerState.PlaybackComplete
			listener?.onCompletion(this)
		}
	}
}