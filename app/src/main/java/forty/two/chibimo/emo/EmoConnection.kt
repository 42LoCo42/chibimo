package forty.two.chibimo.emo

import android.accounts.NetworkErrorException
import android.content.Context
import androidx.preference.PreferenceManager
import forty.two.chibimo.db.Changes
import forty.two.chibimo.utils.EMO_URL
import forty.two.chibimo.zeolite.init
import forty.two.chibimo.zeolite.recv
import forty.two.chibimo.zeolite.send
import org.ktorm.database.Database
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
import org.ktorm.dsl.select

/**
 * @author Leon Schumacher
 */
fun restart(context: Context) {
	val addressAndPort = PreferenceManager.getDefaultSharedPreferences(context).getString(EMO_URL, null) ?: ""
	val parts = addressAndPort.split(":")
	if(parts.size != 2) throw java.lang.IllegalArgumentException("Invalid address: needs to be IP:Port")
	if(!init(parts[0], parts[1])) throw NetworkErrorException("Could not connect to emo")
}

/**
 * Upload & delete all pending changes.
 */
fun uploadChanges(db: Database) {
	send("mergeChanges\n")
	db
		.from(Changes)
		.select()
		.forEach {
			send(it[Changes.path] + "\t" + it[Changes.change])
		}
	db.deleteAll(Changes)
	send("\n")
}

/**
 * Download the song database.
 */
fun getSongs(): List<String> {
	send("getTable songs\n")
	val result = recv()
	if(result.isNullOrBlank()) throw java.lang.RuntimeException("Could not receive songs")
	return result.trim().split("\n")
}