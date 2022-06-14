package forty.two.chibimo.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import forty.two.chibimo.R

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
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