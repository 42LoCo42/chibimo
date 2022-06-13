package forty.two.chibimo.db

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
interface Song: Entity<Song> {
	val path: String
	val count: Int
	val boost: Int
}

object Songs: Table<Song>("songs") {
	val path = text("path").primaryKey().bindTo { it.path }
	val count = int("count").bindTo { it.count }
	val boost = int("boost").bindTo { it.boost }
}