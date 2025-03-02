package org.readutf.hermes.response

import org.readutf.hermes.Packet

public class ResponseDataPacket(
    public val success: Boolean,
    public val response: Any?,
    public val error: String?,
    internal val originalId: Int,
) : Packet<Unit>()
