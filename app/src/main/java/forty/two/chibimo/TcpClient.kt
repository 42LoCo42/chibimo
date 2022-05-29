package forty.two.chibimo

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.Socket

/**
 * @author Leon Schumacher
 */
class TcpClient(
	private val addressAndPort: String,
	private val onError: (Exception) -> Unit,
) {
	lateinit var socket: Socket
	lateinit var sender: BufferedWriter
	lateinit var receiver: BufferedReader

	fun start() {
		try {
			val parts = addressAndPort.split(":")
			if(parts.size != 2) throw IllegalArgumentException("TcpClient requires address:port")
			val port = parts[1].toInt()

			val serverAddress = InetAddress.getByName(parts[0]);
			socket = Socket(serverAddress, port)
			sender = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
			receiver = BufferedReader(InputStreamReader(socket.getInputStream()))
		} catch(e: Exception) {
			onError(e)
		}
	}

	fun sendLine(line: String) {
		sender.write(line + "\n")
		sender.flush()
	}

	fun recvLine(): String = receiver.readLine()
}