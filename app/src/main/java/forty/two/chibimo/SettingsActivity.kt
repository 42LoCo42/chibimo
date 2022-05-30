package forty.two.chibimo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

const val MUSIC_DIRECTORY = "musicDirectory"

fun getMusicDirUri(context: Context): Uri? =
	PreferenceManager.getDefaultSharedPreferences(context).getString(MUSIC_DIRECTORY, "")?.let {
		Uri.parse(it)
	}

fun getMusicDir(context: Context) = getMusicDirUri(context)?.let {
	if(it.toString().isBlank()) return null
	DocumentFile.fromTreeUri(context, it)
}

fun getUriSummary(context: Context): String {
	val notSet = context.getString(R.string.not_set)
	return getMusicDirUri(context)?.let {
		it.path?.replace(Regex("^[^:]*:"), "") ?: notSet
	} ?: notSet
}

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