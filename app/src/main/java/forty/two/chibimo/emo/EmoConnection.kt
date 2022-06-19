package forty.two.chibimo.emo

import android.content.Context
import androidx.preference.PreferenceManager
import forty.two.chibimo.db.Changes
import forty.two.chibimo.utils.EMO_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.Socket

/**
 * @author Leon Schumacher
 */
class EmoConnection(private val context: Context) {
	private suspend fun <T> withConnection(block: (BufferedReader, BufferedWriter) -> T): T {
		val addressAndPort = PreferenceManager.getDefaultSharedPreferences(context).getString(EMO_URL, null) ?: ""
		val parts = addressAndPort.split(":")
		if(parts.size != 2) throw java.lang.IllegalArgumentException("Invalid address: needs to be IP:Port")

		return withContext(Dispatchers.IO) {
			val socket = Socket(InetAddress.getByName(parts[0]), parts[1].toInt())

			block(
				BufferedReader(InputStreamReader(socket.getInputStream())),
				BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
			)
		}
	}

	private fun BufferedWriter.writeLine(line: String) {
		write(line + "\n")
		flush()
	}

	suspend fun uploadChanges(db: Database) = withConnection { _, writer ->
		writer.writeLine("mergeChanges")
		db
			.from(Changes)
			.select()
			.forEach {
				writer.writeLine(it[Changes.path] + "\t" + it[Changes.change])
			}
		db.deleteAll(Changes)
	}

	suspend fun getSongs() = withConnection { reader, writer ->
		writer.writeLine("getTable songs")
		val lines = mutableListOf<String>()
		while(true) {
			val line = reader.readLine()
			if(line.isNullOrBlank()) break
			lines.add(line)
		}
		lines
	}
}