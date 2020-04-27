package me.anon.lib.ext

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import me.anon.grow.R

/**
 * // TODO: Add class description
 */
public fun List<Entry>.rollingAverage(): List<Entry>
{
	val averageEntries = mutableListOf<Entry>()

	foldIndexed(0f) { index, acc, entry ->
		val ret = entry.y + acc
		averageEntries += Entry(this[index].x, ret / (index + 1))
		ret
	}

	return averageEntries
}

public fun LineChart.style()
{
	setDrawGridBackground(false)
	description = null
	isScaleYEnabled = false
	setDrawBorders(false)

	axisLeft.labelCount = 8
	axisLeft.setDrawTopYLabelEntry(true)
	axisLeft.setDrawZeroLine(true)
	axisLeft.setDrawGridLines(false)
	axisLeft.textColor = R.attr.colorOnSurface.resolveColor(context!!)

	axisRight.setDrawLabels(false)
	axisRight.setDrawGridLines(false)
	axisRight.setDrawAxisLine(false)

	xAxis.setDrawGridLines(false)
	xAxis.granularity = 1f
	xAxis.position = XAxis.XAxisPosition.BOTTOM
	xAxis.yOffset = 10f
	xAxis.textColor = R.attr.colorOnSurface.resolveColor(context!!)

	legend.form = Legend.LegendForm.CIRCLE
	legend.textColor = R.attr.colorOnSurface.resolveColor(context!!)
	legend.isWordWrapEnabled = true
}
