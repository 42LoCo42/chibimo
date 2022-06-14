package forty.two.chibimo.emo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.Socket

/**
 * @author Leon Schumacher
 */
class EmoConnection(
	private val addressAndPort: String
) {
	private suspend fun <T> withConnection(block: (BufferedReader, BufferedWriter) -> T): T {
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