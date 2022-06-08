package forty.two.chibimo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * @author Leon Schumacher
 */

class ToastController(private val context: Context) {
	private val handler = Handler(Looper.getMainLooper())
	private lateinit var lastToast: Toast

	fun show(stringID: Int, duration: Int = Toast.LENGTH_SHORT) {
		show(context.getString(stringID), duration)
	}

	@SuppressLint("ShowToast")
	fun show(text: String, duration: Int = Toast.LENGTH_SHORT) {
		handler.post {
			cancel()
			lastToast = Toast.makeText(context, text, duration)
			lastToast.show()
		}
	}

	private fun cancel() {
		if(this::lastToast.isInitialized) lastToast.cancel()
	}
}