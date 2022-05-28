package forty.two.chibimo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

const val MUSIC_DIRECTORY = "musicDirectory"

class SettingsActivity: AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.settings_activity)
		if(savedInstanceState == null) {
			supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
		}
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	class SettingsFragment: PreferenceFragmentCompat() {
		private val fileSelector = registerForActivityResult(OpenDocumentTree()) {
			context?.contentResolver?.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
			Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
			preferenceManager.sharedPreferences?.edit()?.apply {
				putString(MUSIC_DIRECTORY, it.toString())
				apply()
			}
		}

		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey)
			findPreference<Preference>(MUSIC_DIRECTORY)?.apply {
				setOnPreferenceClickListener {
					fileSelector.launch(Uri.EMPTY)
					true
				}

				setSummaryProvider {
					preferenceManager.sharedPreferences?.getString(MUSIC_DIRECTORY, getString(R.string.not_set))
				}
			}
		}
	}
}