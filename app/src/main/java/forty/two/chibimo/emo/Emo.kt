package forty.two.chibimo.emo

import android.util.Log
import forty.two.chibimo.db.Changes
import forty.two.chibimo.db.Songs
import forty.two.chibimo.utils.ifNull
import forty.two.chibimo.utils.randomWeighted
import org.ktorm.database.Database
import org.ktorm.dsl.*

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */

/**
 * A local emo implementation.
 * We don't need multi-client support, so we use a simple deque as a queue.
 */
class Emo(
	private val db: Database
) {
	private val queue = ArrayDeque<String>()

	/**
	 * Add a song to the queue
	 */
	fun add(song: String) {
		queue.add(song)
	}

	/**
	 * Get the next song from the queue or the generator.
	 */
	fun next(): String {
		if(queue.isEmpty()) {
			val dataset = db
				.from(Songs)
				.leftJoin(Changes, Songs.path eq Changes.path)
				.select(Songs.path, Songs.count + Songs.boost + Changes.change.ifNull(0))
				.map {
					it.getString(1)!! to it.getInt(2)
				}
			randomWeighted(dataset)?.let { add(it) } ?: throw IllegalArgumentException("Song database is empty!")
		}
		return queue.removeFirst()
	}

	/**
	 * Clear the queue
	 */
	fun clear() {
		queue.clear()
	}

	/**
	 * Signify completion of this song.
	 */
	fun complete(song: String) {
		Log.e("COMPLETION", song)
		db.update(Changes) {
			set(it.change, it.change + 1)
			where {
				it.path eq song
			}
		}

		db.insert(Changes) {
			set(it.path, song)
			set(it.change, 1)
		}

		db.from(Changes).select().forEach { println("${it[Changes.path]}: ${it[Changes.change]}") }
	}

	/**
	 * Overwrite the current song database with the provided values.
	 * This function is supposed to be called with the output of [getSongs]
	 */
	fun setSongsDBFromRawSongs(rawSongs: List<String>): Int {
		db.deleteAll(Songs)
		return db.batchInsert(Songs) {
			rawSongs.map { line ->
				val parts = line.split("\t")
				item {
					set(it.path, parts[0])
					set(it.count, parts[1].toInt())
					set(it.boost, parts[2].toInt())
				}
			}
		}.size
	}
}
