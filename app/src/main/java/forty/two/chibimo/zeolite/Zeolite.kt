package forty.two.chibimo.zeolite

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
external fun init(addr: String, port: String): Boolean
external fun send(msg: String): Boolean
external fun recv(): String?