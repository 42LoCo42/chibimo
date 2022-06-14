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
fun DocumentFile.child(path: String): DocumentFile? {
	var current = this
	for(component in path.split("/")) {
		current = current.findFile(component) ?: return null
	}
	return current
}

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

fun divMod(num: Int, modulus: Int): Pair<Int, Int> {
	return (num / modulus) to (num % modulus)
}

fun millisToTimeString(millis: Int): String {
	val (hours, r1) = divMod(millis / 1000, 3600)
	val (minutes, seconds) = divMod(r1, 60)
	return (if(hours > 0) "$hours h " else "") +
		(if(minutes > 0) "$minutes min " else "") +
		"$seconds s"
}

fun <T: Entity<T>> Table<T>.mkCreationExpr(): String {
	val columns = columns.joinToString(", ") {
		"${it.name} ${it.sqlType.typeName} " + if(primaryKeys.contains(it)) "primary key" else "" + " not null"
	}
	return "create table if not exists $tableName ($columns);"
}

fun <T: Entity<T>> Database.tryCreateTable(table: Table<T>): Boolean {
	useConnection {
		return it.prepareStatement(table.mkCreationExpr()).execute()
	}
}

// https://github.com/kotlin-orm/ktorm/blob/master/ktorm-support-sqlite/src/main/kotlin/org/ktorm/support/sqlite/Functions.kt#L126
fun <T: Any> ColumnDeclaring<T>.ifNull(right: ColumnDeclaring<T>): FunctionExpression<T> {
	return FunctionExpression(
		functionName = "ifnull",
		arguments = listOf(this, right).map { it.asExpression() },
		sqlType = sqlType
	)
}

// https://github.com/kotlin-orm/ktorm/blob/master/ktorm-support-sqlite/src/main/kotlin/org/ktorm/support/sqlite/Functions.kt#L138
fun <T: Any> ColumnDeclaring<T>.ifNull(right: T?): FunctionExpression<T> {
	return this.ifNull(wrapArgument(right))
}