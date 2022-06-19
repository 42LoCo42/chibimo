package forty.two.chibimo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import forty.two.chibimo.R
import forty.two.chibimo.utils.MUSIC_DIRECTORY
import forty.two.chibimo.utils.getUriSummary

/**
 * The Settings UI.
 * From here, important parameters of chibimo can be configured.
 */
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
			preferenceManager.sharedPreferences?.edit()?.apply {
				putString(MUSIC_DIRECTORY, it.toString())
				apply()
			}
			findPreference<Preference>(MUSIC_DIRECTORY)?.summary = getUriSummary(requireContext())
		}

		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey)
			findPreference<Preference>(MUSIC_DIRECTORY)?.apply {
				setOnPreferenceClickListener {
					fileSelector.launch(Uri.EMPTY)
					true
				}
				summary = getUriSummary(context)
			}
		}
	}
}