package forty.two.chibimo

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

/**
 * @author Leon Schumacher
 */
class ToastController(private val context: Context) {
	private lateinit var lastToast: Toast

	fun show(stringID: Int, duration: Int = Toast.LENGTH_SHORT) {
		show(context.getString(stringID), duration)
	}

	@SuppressLint("ShowToast")
	fun show(text: String, duration: Int = Toast.LENGTH_SHORT) {
		cancel()
		lastToast = Toast.makeText(context, text, duration)
		lastToast.show()
	}

	private fun cancel() {
		if(this::lastToast.isInitialized) lastToast.cancel()
	}
}