package org.readutf.hermes.response

import org.readutf.hermes.Packet

class ResponseDataPacket(
    val response: Any,
    val originalId: Int,
) : Packet<Unit>()
