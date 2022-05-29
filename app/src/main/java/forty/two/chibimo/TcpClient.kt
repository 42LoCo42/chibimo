package forty.two.chibimo

import java.io.*
import java.net.InetAddress
import java.net.Socket

/**
 * @author Leon Schumacher
 */
class TcpClient(
	private val addressAndPort: String,

	private val onError: (Exception) -> Unit
) {
	lateinit var sender: PrintWriter
	lateinit var receiver: BufferedReader

	fun start() {
		try {
			val parts = addressAndPort.split(":")
			if(parts.size != 2) throw IllegalArgumentException("TcpClient requires address:port")
			val port = parts[1].toInt()

			val serverAddress = InetAddress.getByName(parts[0]);
			Socket(serverAddress, port).use { socket ->
				sender = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())))
				receiver = BufferedReader(InputStreamReader(socket.getInputStream()))

				sender.println("foobar")
				sender.flush()
			}
		} catch(e: Exception) {
			onError(e)
		}
	}
}