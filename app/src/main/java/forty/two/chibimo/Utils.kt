package forty.two.chibimo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.documentfile.provider.DocumentFile

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