package forty.two.chibimo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import forty.two.chibimo.R

/**
 * The Help / first-start message describing all functionality of chibimo.
 */
class HelpActivity: AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_help)

		findViewById<Button>(R.id.btnOpenSettings).setOnClickListener {
			startActivity(Intent(this, SettingsActivity::class.java))
		}
	}
}