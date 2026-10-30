package com.root3.comfortgauge.ui.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.model.ComfortModel
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Units
import com.root3.comfortgauge.ui.theme.LinkBlue
import com.root3.comfortgauge.ui.theme.Muted

/** Builder for prototype prose: **bold**, *italic*, colored bold and inline temperatures. */
private class Prose(val unit: TempUnit) {
    val b = AnnotatedString.Builder()
    fun t(s: String) = apply { b.append(s) }
    fun bold(s: String, color: Color? = null) = apply {
        b.withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color ?: Color.Unspecified)) { append(s) }
    }
    fun em(s: String) = apply { b.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(s) } }

    /** A `data-f` span: °F value shown in the display unit; [bare] drops the unit suffix. */
    fun f(value: Int, bare: Boolean = false) = apply { b.append(Units.format(value.toDouble(), unit, bare)) }
    fun done() = b.toAnnotatedString()
}

private fun link(text: String, url: String) = buildAnnotatedString {
    withLink(LinkAnnotation.Url(url, TextLinkStyles(SpanStyle(color = LinkBlue)))) { append(text) }
}

private val SOURCES = listOf(
    "ISO 7730:2005 — PMV/PPD thermal comfort model" to "https://www.iso.org/standard/39155.html",
    "ASHRAE Standard 55 — comfort zone, PMV ±0.5, 0.012 kg/kg humidity limit" to "https://en.wikipedia.org/wiki/ASHRAE_55",
    "NWS Heat Index equation" to "https://www.wpc.ncep.noaa.gov/html/heatindex_equation.shtml",
    "BOM Apparent Temperature" to "http://www.bom.gov.au/info/thermal_stress/#atapproximation",
    "Environment Canada — Humidex" to "https://climate.weather.gc.ca/glossary_e.html#humidex",
    "Sterling et al. 1985 — Criteria for human exposure to humidity (ASHRAE Trans.)" to
        "https://www.condair.com/humidity-health-wellbeing/scientific-studies/criteria-for-human-exposure-to-humidity-in-occupied-buildings",
    "Energy Vanguard — the Sterling chart and its caveats" to "https://www.energyvanguard.com/blog/humidity-health-and-sterling-chart",
    "Alduchov & Eskridge 1996 — Magnus dew-point coefficients" to
        "https://journals.ametsoc.org/view/journals/apme/35/4/1520-0450_1996_035_0601_imfaos_2_0_co_2.xml",
)

@Composable
internal fun InfoContent(unit: TempUnit) {
    fun p() = Prose(unit)

    H2("What this tool does")
    P(p().t("You set a temperature and a humidity. It tells you how comfortable that air would be for most people — and ")
        .em("why").t(" — by answering two separate questions:").done())
    Bullets(listOf(
        p().bold("Is it the right temperature?").t(" (the thermal axis)").done(),
        p().bold("Is the air too dry or too damp?").t(" (the humidity axis)").done(),
    ))
    P(p().t("Comfort is a function of both. Air can be the perfect temperature and still be miserable because it's bone-dry or muggy, and \"feels-like\" numbers like Heat Index can't see that — they're built to measure heat stress, and to them dry air is simply ")
        .em("cooler").t(". That's why this tool uses a map instead of a single number.").done())

    H2("The map")
    P(p().t("Temperature runs left→right (the map shows ").f(50, bare = true).t("–").f(100)
        .t("; outside that it's uniformly too cold or too hot, and the marker pins to the edge with an arrow), humidity bottom→top. The green island is where most people are comfortable. Color tells you the cause when you're outside it: ")
        .bold("blue = too cold", Color(0xFF3B82F6)).t(", ").bold("orange/red = too hot", Color(0xFFF97316)).t(", ")
        .bold("tan/brown = too dry", Color(0xFFD9A066)).t(", ").bold("purple = too humid", Color(0xFFA78BFA))
        .t(". Darker means worse. Drag anywhere on the map to set both values at once.").done())

    H2("The thermal axis — what temperature humans like")
    P(p().t("We use the PMV/PPD model (Fanger), the standard HVAC engineers use worldwide (ISO 7730, ASHRAE 55). It predicts the ")
        .bold("percentage of people who would say they're uncomfortable")
        .t(" at a given condition. ASHRAE calls a space compliant when that's 10% or less.").done())
    InfoCard("Predicted % dissatisfied vs. temperature (50% RH, seated, still air)") { TempChart(unit) }
    P(p().t("There is no single comfortable temperature — it depends on clothing. The tool assumes people dress sensibly for indoors (0.5–1.0 clo, i.e. shorts-and-tee up to trousers-and-sweater) and takes the best case, which is how ASHRAE defines its comfort zone. Even so, ")
        .f(65).t(" leaves about 1 in 4 people cold.").done())

    H2("Why humidity barely moves the thermal number")
    InfoCard("Predicted % dissatisfied vs. humidity, at three temperatures") { RhChart(unit) }
    P(p().t("At room temperature, humidity changes thermal comfort by only a few percent. At ").f(90)
        .t(" it matters a lot — sweat can't evaporate. At ").f(65)
        .t(" it barely registers. So dryness has to be judged on its own terms, not through a feels-like temperature.").done())

    H2("The humidity axis — what air humans like")
    P(p().t("Research on indoor air puts the sweet spot at ").bold("40–60% relative humidity")
        .t(". Below ~30% people report dry eyes, skin and airways, and static shocks; the air also lets flu virus survive longer. Above the line, dust mites and mold take off, and the air starts to feel sticky.").done())
    InfoCard("Relative humidity") {
        BandBar(
            listOf(
                BandSeg(10f, 0xFF7A3D10, "Extreme", true), BandSeg(10f, 0xFFB26A2E, "Very dry", true),
                BandSeg(10f, 0xFFD9A066, "Dry"), BandSeg(10f, 0xFF84CC16, "OK"),
                BandSeg(20f, 0xFF16A34A, "Ideal 40–60%", true), BandSeg(5f, 0xFF84CC16),
                BandSeg(10f, 0xFFC4B5FD, "Humid"), BandSeg(25f, 0xFF8B5CF6, "Damp / mold risk", true),
            ),
            listOf("0%", "20", "30", "40", "60", "75", "100%"),
        )
    }
    P(p().t("On the muggy side, ").bold("dew point")
        .t(" is the better measure — it's the actual amount of moisture in the air, independent of temperature. ASHRAE's upper comfort limit works out to a dew point of about ")
        .f(62).t(". Meteorologists call ").f(65).t("+ muggy and ").f(70).t("+ oppressive.").done())
    InfoCard("Dew point") {
        BandBar(
            listOf(
                BandSeg(40f, 0xFFD9A066, "Dry"), BandSeg(20f, 0xFF16A34A, "Comfortable", true),
                BandSeg(5f, 0xFFC4B5FD), BandSeg(5f, 0xFFA78BFA, "Sticky"),
                BandSeg(5f, 0xFF8B5CF6, "Muggy", true), BandSeg(15f, 0xFF5B21B6, "Oppressive", true),
            ),
            listOf(
                Units.format(0.0, unit), Units.format(40.0, unit, true), Units.format(60.0, unit, true),
                Units.format(65.0, unit, true), Units.format(70.0, unit, true), Units.format(90.0, unit),
            ),
        )
    }

    H2("The four small gauges")
    Bullets(listOf(
        p().bold("Heat Index").t(" — the US National Weather Service \"feels like\" for heat. Only defined from ").f(80)
            .t(" up; below that it just equals the air temperature.").done(),
        p().bold("Apparent Temperature").t(" — Australian Bureau of Meteorology model; covers all temperatures. Wind set to zero here.").done(),
        p().bold("Humidex").t(" — Environment Canada's version. Runs hotter than Heat Index in muggy air.").done(),
        p().bold("Dew Point").t(" — moisture in the air, as the temperature it would start condensing at.").done(),
    ))
    P(p().t("These are different models that disagree with each other — that's expected, and it's why all four are shown.").done())

    H2("Assumptions & limits")
    Bullets(
        listOf(
            p().t("Seated / light activity (1.1 met), still air (0.1 m/s), surfaces at air temperature, clothing 0.5–1.0 clo. Sun, wind and radiant heat aren't modeled.").done(),
            p().t("PMV is validated roughly from ").f(50, bare = true).t("–").f(86)
                .t(". Outside that the tool extrapolates; the \"feels like\" indices cover the extremes.").done(),
            p().t("The 40–60% health band comes from a review (Sterling 1985) whose own authors call it suggestive rather than precise. Below ~30% is the firmer line.").done(),
            p().t("Heat Index is only valid to about ").f(135).t("; past that it's clamped.").done(),
        ),
        color = Muted, size = 12.sp,
    )

    H2("Sources")
    Bullets(SOURCES.map { (text, url) -> link(text, url) }, size = 12.sp)
}

@Composable
private fun TempChart(unit: TempUnit) {
    val (xs, labels) = remember(unit) {
        if (unit == TempUnit.F) {
            val t = (0..16).map { 55 + it * 2.5 }
            t to t.map { if (it % 5 != 0.0) "" else "${it.toInt()}°" }
        } else {
            val c = (13..35).toList()
            c.map { Units.c2f(it.toDouble()) } to c.map { if (it % 2 != 0) "" else "$it°" }
        }
    }
    val series = remember(unit) {
        listOf("Light clothes (0.5 clo)" to 0.5, "Typical (0.7)" to 0.7, "Winter (1.0)" to 1.0)
            .map { (name, clo) -> Series(name, xs.map { ppd(it, clo, 50.0) }) }
    }
    LineChart(labels, series, "Air temperature (°${unit.name})", "Chart: predicted percent dissatisfied versus air temperature for three clothing levels")
}

@Composable
private fun RhChart(unit: TempUnit) {
    val rhs = (0..100 step 10).toList()
    val series = remember(unit) {
        listOf(
            Series("${Units.format(65.0, unit)}, winter clothes", rhs.map { ppd(65.0, 1.0, it.toDouble()) }),
            Series("${Units.format(78.0, unit)}, light", rhs.map { ppd(78.0, 0.5, it.toDouble()) }),
            Series("${Units.format(90.0, unit)}, light", rhs.map { ppd(90.0, 0.5, it.toDouble()) }),
        )
    }
    LineChart(rhs.map { "$it%" }, series, "Relative humidity", "Chart: predicted percent dissatisfied versus relative humidity at three temperatures")
}

/** PPD at air = radiant temperature [f] °F, seated (1.1 met), still air, RH floored at 0.5%. */
private fun ppd(f: Double, clo: Double, rh: Double): Double {
    val t = Units.f2c(f)
    return ComfortModel.pmvPpd(t, t, 0.1, maxOf(rh, 0.5), 1.1, clo).ppd
}
