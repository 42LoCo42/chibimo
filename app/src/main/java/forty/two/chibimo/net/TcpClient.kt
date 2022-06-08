package forty.two.chibimo.net

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
	private val onConnected: () -> Unit,
	private val onError: (Exception) -> Unit,
) {
	private lateinit var socket: Socket
	private lateinit var sender: BufferedWriter
	private lateinit var receiver: BufferedReader

	fun start() {
		try {
			val parts = addressAndPort.split(":")
			if(parts.size != 2) throw IllegalArgumentException("TcpClient requires address:port")
			val port = parts[1].toInt()

			val serverAddress = InetAddress.getByName(parts[0])
			socket = Socket(serverAddress, port)
			sender = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
			receiver = BufferedReader(InputStreamReader(socket.getInputStream()))
			onConnected()
		} catch(e: Exception) {
			onError(e)
		}
	}

	fun sendLine(line: String) {
		try {
			sender.write(line + "\n")
			sender.flush()
		} catch(e: Exception) {
			onError(e)
		}
	}

	fun recvLine(): String = try {
		receiver.readLine()
	} catch(e: Exception) {
		onError(e)
		""
	}
}