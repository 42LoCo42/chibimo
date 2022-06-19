package forty.two.chibimo.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * @author Leon Schumacher
 */

/**
 * A class wrapping a [Toast] object,
 * so that it can be canceled immediately when a new toast should be displayed.
 */
class ToastController(private val context: Context) {
	private val handler = Handler(Looper.getMainLooper())
	private lateinit var lastToast: Toast

	/**
	 * Show a toast.
	 * This will immediately cancel the last one.
	 */
	fun show(text: String, duration: Int = Toast.LENGTH_SHORT) {
		handler.post {
			cancel()
			lastToast = Toast.makeText(context, text, duration)
			lastToast.show()
		}
	}

	/**
	 * Cancel the last toast displayed.
	 */
	private fun cancel() {
		if(this::lastToast.isInitialized) lastToast.cancel()
	}
}