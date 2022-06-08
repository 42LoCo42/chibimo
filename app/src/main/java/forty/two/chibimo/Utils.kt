package forty.two.chibimo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
fun DocumentFile.child(path: String): DocumentFile? {
	var current = this
	for(component in path.split("/")) {
		current = current.findFile(component) ?: return null
	}
	return current
}

fun Context.connectToPlayer(callback: (PlayerService) -> Unit) {
	val intent = Intent(this, PlayerService::class.java)
	startForegroundService(intent)
	bindService(intent, object: ServiceConnection {
		override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
			p1?.let {
				callback((p1 as PlayerService.MyBinder).playerService)
			}
		}

		override fun onServiceDisconnected(p0: ComponentName?) {}
	}, 0)
}

fun Context.connectToEmo(callback: suspend CoroutineScope.(Channel<EmoMsg>) -> Unit) {
	connectToPlayer { it.withEmo { c -> callback(c) } }
}

fun divMod(num: Int, modulus: Int): Pair<Int, Int> {
	return (num / modulus) to (num % modulus)
}

fun millisToTimeString(millis: Int): String {
	val (hours, r1) = divMod(millis / 1000, 3600)
	val (minutes, seconds) = divMod(r1, 60)
	return (if(hours > 0) "$hours h " else "") +
		(if(minutes > 0) "$minutes min " else "") +
		"$seconds s"
}