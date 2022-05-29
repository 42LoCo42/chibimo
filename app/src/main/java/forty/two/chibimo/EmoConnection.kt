package forty.two.chibimo

/**
 * @author Leon Schumacher
 */
class EmoConnection(
	private val addressAndPort: String,
	private val onError: (Exception) -> Unit,
) {
	private val tcpClient = TcpClient(addressAndPort, onError).apply {
		start()
	}

	fun getNext(): String {
		tcpClient.sendLine("next")
		return tcpClient.recvLine()
	}

	fun getQueue(): List<String> {
		val queue = mutableListOf<String>()
		tcpClient.sendLine("queue")
		while(true) {
			val line = tcpClient.recvLine()
			if(line == "end") break
			queue.add(line.split(Regex(" "), 1)[1])
		}
		return queue
	}

	fun add(song: String) {
		tcpClient.sendLine("next $song")
		tcpClient.recvLine()
	}

	fun repeat(song: String) {
		val queue = getQueue()
		if(queue.size < 2 || queue[1] != song) {
			clear()
		}

		add(song)
	}

	fun clear() {
		tcpClient.sendLine("clear")
	}
}