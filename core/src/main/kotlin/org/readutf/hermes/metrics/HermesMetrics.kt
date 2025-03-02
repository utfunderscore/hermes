package org.readutf.hermes.metrics

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry

public object HermesMetrics {
    private lateinit var registry: MeterRegistry

    public fun init(registry: MeterRegistry) {
        if (!::registry.isInitialized) {
            this.registry = registry
        }
    }

    public val packetSize: DistributionSummary by
        lazy {
            DistributionSummary
                .builder("hermes.packets")
                .description("The throughput of packets sent and received by Hermes")
                .baseUnit("bytes")
                .register(registry)
        }

    public val sentPackets: DistributionSummary by
        lazy {
            DistributionSummary
                .builder("hermes.sent_packets")
                .description("The throughput of packets sent by Hermes")
                .baseUnit("packets")
                .register(registry)
        }

    public val failedTransfers: DistributionSummary by
        lazy {
            DistributionSummary
                .builder("hermes.failed_packets")
                .description("The throughput of packets failed to be send by Hermes")
                .baseUnit("packets")
                .register(registry)
        }

    public val receivedPackets: DistributionSummary by
        lazy {
            DistributionSummary
                .builder("hermes.received_packets")
                .description("The throughput of packets received by Hermes")
                .baseUnit("packets")
                .register(registry)
        }
}
