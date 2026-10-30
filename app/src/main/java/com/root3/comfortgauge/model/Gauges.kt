package com.root3.comfortgauge.model

/** A gauge band; values below [to] (°F) belong to it. */
data class Zone(val to: Double, val label: String, val color: Long)

data class GaugeConfig(val min: Double, val max: Double, val zones: List<Zone>) {
    fun zoneOf(v: Double): Zone = zones.firstOrNull { v < it.to } ?: zones.last()

    fun clamp(v: Double): Double = v.coerceIn(min, max)

    /** 0..1 position of [v] along the arc. */
    fun frac(v: Double): Double = (clamp(v) - min) / (max - min)

    /** Interior zone boundaries followed by the two ends, as the prototype's big gauge labels them. */
    val boundaryTicks: List<Double>
        get() = zones.map { it.to }.filter { it > min && it < max } + listOf(min, max)
}

object Gauges {
    val FEELS = GaugeConfig(
        min = 0.0, max = 135.0,
        zones = listOf(
            Zone(32.0, "Freezing", 0xFF1D4ED8),
            Zone(55.0, "Cold", 0xFF3B82F6),
            Zone(68.0, "Cool", 0xFF93C5FD),
            Zone(80.0, "Comfortable", 0xFF16A34A),
            Zone(90.0, "Warm", 0xFFFDE047),
            Zone(103.0, "Hot", 0xFFF97316),
            Zone(125.0, "Danger", 0xFFDC2626),
            Zone(Double.POSITIVE_INFINITY, "Extreme", 0xFF7F1D1D),
        ),
    )

    val DEW = GaugeConfig(
        min = 0.0, max = 90.0,
        zones = listOf(
            Zone(30.0, "Very dry", 0xFF7A3D10),
            Zone(40.0, "Dry", 0xFFD9A066),
            Zone(60.0, "Comfortable", 0xFF16A34A),
            Zone(65.0, "Sticky", 0xFFC4B5FD),
            Zone(70.0, "Muggy", 0xFF8B5CF6),
            Zone(75.0, "Oppressive", 0xFF5B21B6),
            Zone(Double.POSITIVE_INFINITY, "Miserable", 0xFF3B0764),
        ),
    )
}
