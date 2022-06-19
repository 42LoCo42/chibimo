package forty.two.chibimo.zeolite

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
const val success: ZeoliteError = 0

typealias Zeolite = Long
typealias ZeoliteChannel = Long
typealias ZeoliteError = Long

external fun zeoliteInit(): Int
external fun zeoliteCreate(): Zeolite
external fun zeoliteCreateChannel(z: Zeolite, fd: Int): ZeoliteChannel
external fun zeoliteChannelSend(c: ZeoliteChannel, str: String): ZeoliteError
external fun zeoliteChannelRecv(c: ZeoliteChannel): String?