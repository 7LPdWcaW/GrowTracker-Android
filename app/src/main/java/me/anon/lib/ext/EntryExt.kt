package me.anon.lib.ext

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.scale
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import me.anon.grow.R

public fun Chart<*>.scaledBitmap(): Bitmap
{
	val returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
	val canvas = Canvas(returnedBitmap)
	var bgDrawable = background
	if (bgDrawable != null) bgDrawable.draw(canvas)
	else canvas.drawColor(Color.WHITE)

	draw(canvas)

	return returnedBitmap.scale((width.toFloat() / 1.5f).toInt(), (height.toFloat() / 1.5f).toInt()).also {
		returnedBitmap.recycle()
	}
}

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
