package forty.two.chibimo.zeolite

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
const val success: ZeoliteError = 0

typealias Zeolite = Long
typealias ZeoliteChannel = Long
typealias ZeoliteError = Int

/**
 * Load the zeolite library.
 * Returns [success] on success or any other value on failure.
 */
external fun zeoliteInit(): ZeoliteError

/**
 * Create a new zeolite identity.
 */
external fun zeoliteCreate(): Zeolite

/**
 * Create & initialize a zeolite channel.
 */
external fun zeoliteCreateChannel(z: Zeolite, fd: Int): ZeoliteChannel

/**
 * Send a message over a zeolite channel.
 */
external fun zeoliteChannelSend(c: ZeoliteChannel, str: String): ZeoliteError

/**
 * Receive a message from a zeolite channel.
 */
external fun zeoliteChannelRecv(c: ZeoliteChannel): String?