package forty.two.chibimo

import kotlinx.coroutines.channels.Channel

/**
 * @author Leon Schumacher
 */
sealed class EmoMsg {
	object GetNext: EmoMsg()
	data class RespNext(val next: String): EmoMsg()

	object GetQueue: EmoMsg()
	data class RespQueue(val queue: List<String>): EmoMsg()

	data class Add(val song: String): EmoMsg()
	data class Repeat(val song: String): EmoMsg()
	data class Complete(val song: String): EmoMsg()
	object Clear: EmoMsg()
}

class EmoConnection(
	private val channel: Channel<EmoMsg>,
	addressAndPort: String,
	onConnected: () -> Unit,
	onError: (Exception) -> Unit,
) {
	private val tcpClient = TcpClient(addressAndPort, onConnected, onError)

	suspend fun start() {
		tcpClient.start()
		println("client started")
		while(true) {
			when(val msg = channel.receive()) {
				EmoMsg.GetNext -> channel.send(EmoMsg.RespNext(getNext()))
				EmoMsg.GetQueue -> channel.send(EmoMsg.RespQueue(getQueue()))
				is EmoMsg.Add -> add(msg.song)
				is EmoMsg.Repeat -> repeat(msg.song)
				is EmoMsg.Complete -> complete(msg.song)
				EmoMsg.Clear -> clear()
				else -> {}
			}
		}
	}

	private fun getNext(): String {
		tcpClient.sendLine("next")
		return tcpClient.recvLine()
	}

	private fun getQueue(): List<String> {
		val queue = mutableListOf<String>()
		tcpClient.sendLine("queue")
		while(true) {
			val line = tcpClient.recvLine()
			if(line == "end") break
			queue.add(line.split(Regex(" "), 2)[1])
		}
		return queue
	}

	private fun add(song: String) {
		tcpClient.sendLine("add $song")
		tcpClient.recvLine()
	}

	private fun repeat(song: String) {
		val queue = getQueue()
		println("repeating $song")
		println(queue)
		if(queue.size >= 2 && queue[1] != song) {
			clear()
		}

		add(song)
	}

	private fun complete(song: String) {
		tcpClient.sendLine("complete $song")
	}

	private fun clear() {
		tcpClient.sendLine("clear")
	}
}