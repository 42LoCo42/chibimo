package forty.two.chibimo.db

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
interface Change: Entity<Change> {
	val path: String
	val change: Int
}

object Changes: Table<Change>("changes") {
	val path = text("path").primaryKey().bindTo { it.path }
	val change = int("count").bindTo { it.change }
}