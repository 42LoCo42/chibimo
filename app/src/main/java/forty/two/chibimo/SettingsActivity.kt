package forty.two.chibimo

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity: AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.settings_activity)
		if(savedInstanceState == null) {
			supportFragmentManager
				.beginTransaction()
				.replace(R.id.settings, SettingsFragment())
				.commit()
		}
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	class SettingsFragment: PreferenceFragmentCompat() {
		private val fileSelector = registerForActivityResult(OpenDocumentTree()) {
			println(it)
			preferenceManager.sharedPreferences?.edit()?.putString("musicDirectory", it.toString())
		}

		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey)
			findPreference<Preference>("musicDirectory")?.setOnPreferenceClickListener {
				fileSelector.launch(Uri.EMPTY)
				true
			}
		}
	}
}