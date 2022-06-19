package forty.two.chibimo.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.documentfile.provider.DocumentFile
import forty.two.chibimo.media.PlayerService
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.expression.FunctionExpression
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */

/**
 * Find a child item of this directory.
 */
fun DocumentFile.child(path: String): DocumentFile? {
	var current = this
	for(component in path.split("/")) {
		current = current.findFile(component) ?: return null
	}
	return current
}

/**
 * Run something in an environment that can access the current [PlayerService] instance.
 */
fun Context.connectToPlayer(callback: (PlayerService) -> Unit) {
	val intent = Intent(this, PlayerService::class.java)
	startForegroundService(intent)
	bindService(intent, object: ServiceConnection {
		override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
			p1?.let {
				callback((p1 as PlayerService.MyBinder).playerService)
			}
		}

		override fun onServiceDisconnected(p0: ComponentName?) {}
	}, 0)
}

/**
 * Calculate the quotient and remainder of a division.
 */
fun divMod(num: Int, modulus: Int): Pair<Int, Int> {
	return (num / modulus) to (num % modulus)
}

/**
 * Create a human-readable string from the supplied millisecond duration.
 */
fun millisToTimeString(millis: Int): String {
	val (hours, r1) = divMod(millis / 1000, 3600)
	val (minutes, seconds) = divMod(r1, 60)
	return (if(hours > 0) "$hours h " else "") +
		(if(minutes > 0) "$minutes min " else "") +
		"$seconds s"
}

/**
 * Generate an SQL expression that will create the table if it doesn't already exist.
 */
fun <T: Entity<T>> Table<T>.mkCreationExpr(): String {
	val columns = columns.joinToString(", ") {
		"${it.name} ${it.sqlType.typeName} " + if(primaryKeys.contains(it)) "primary key" else "" + " not null"
	}
	return "create table if not exists $tableName ($columns);"
}

/**
 * Create the table if it doesn't already exist.
 */
fun <T: Entity<T>> Database.tryCreateTable(table: Table<T>): Boolean {
	useConnection {
		return it.prepareStatement(table.mkCreationExpr()).execute()
	}
}

/**
 * Return the value of this column or [right] if it is null.
 * Implementation from [ktorm-support-sqlite](https://github.com/kotlin-orm/ktorm/blob/master/ktorm-support-sqlite/src/main/kotlin/org/ktorm/support/sqlite/Functions.kt#L126)
 */
fun <T: Any> ColumnDeclaring<T>.ifNull(right: ColumnDeclaring<T>): FunctionExpression<T> {
	return FunctionExpression(
		functionName = "ifnull",
		arguments = listOf(this, right).map { it.asExpression() },
		sqlType = sqlType
	)
}

/**
 * Return the value of this column or [right] if it is null.
 * Implementation from [ktorm-support-sqlite](https://github.com/kotlin-orm/ktorm/blob/master/ktorm-support-sqlite/src/main/kotlin/org/ktorm/support/sqlite/Functions.kt#L138)
 */
fun <T: Any> ColumnDeclaring<T>.ifNull(right: T?): FunctionExpression<T> {
	return this.ifNull(wrapArgument(right))
}