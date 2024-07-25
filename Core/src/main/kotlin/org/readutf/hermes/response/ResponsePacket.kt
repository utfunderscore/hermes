package org.readutf.hermes.response

import org.readutf.hermes.Packet

class ResponsePacket(
    val response: Any,
    val originalId: Int,
) : Packet()
