package forty.two.chibimo.net

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
	private val addressAndPort: String,
	private val onConnected: () -> Unit,
	private val onError: (Exception) -> Unit,
) {
	var isOnline = false
		private set

	private lateinit var tcpClient: TcpClient
	private val queue = ArrayDeque<String>()

	suspend fun start() {
		tcpClient = TcpClient(addressAndPort, {
			isOnline = true
			onConnected()
		}) {
			isOnline = false
			onError(it)
		}
		tcpClient.start()

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
		val song = if(isOnline) {
			tcpClient.sendLine("next")
			tcpClient.recvLine()
		} else {
			"TODO"
		}
		queue.add(song)
		return song
	}

	private fun getQueue(): List<String> {
		return if(isOnline) {
			val queue = mutableListOf<String>()
			tcpClient.sendLine("queue")
			while(true) {
				val line = tcpClient.recvLine()
				if(line == "end") break
				queue.add(line.split(Regex(" "), 2)[1])
			}
			queue
		} else {
			queue
		}
	}

	private fun add(song: String) {
		if(isOnline) {
			tcpClient.sendLine("add $song")
			tcpClient.recvLine()
		}
		queue.add(song)
	}

	private fun repeat(song: String) {
		val queue = getQueue()
		if(queue.size >= 2 && queue[1] != song) {
			clear()
		}

		add(song)
	}

	private fun complete(song: String) {
		if(isOnline) {
			tcpClient.sendLine("complete $song")
		}
	}

	private fun clear() {
		if(isOnline) {
			tcpClient.sendLine("clear")
		}
		queue.clear()
	}
}