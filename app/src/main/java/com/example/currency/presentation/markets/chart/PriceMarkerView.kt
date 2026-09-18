package com.example.currency.presentation.markets.chart

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.example.currency.R
import com.example.currency.domain.model.PricePoint
import com.example.currency.presentation.common.chart.ChartHelper
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ViewConstructor")
class PriceMarkerView(
    context: Context,
    private val pricePoints: List<PricePoint>
) : MarkerView(context, R.layout.view_chart_marker) {

    private val tvPrice: TextView = findViewById(R.id.tvPrice)
    private val tvTime: TextView = findViewById(R.id.tvTime)
    private val timeFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val index = it.x.toInt()
            if (index in pricePoints.indices) {
                val point = pricePoints[index]
                // Hiển thị giá tương tự format trục Y
                tvPrice.text = ChartHelper.formatPrice(
                    point.price.toFloat()
                )

                // Format thời gian
                tvTime.text = timeFormat.format(
                    Date(point.timestamp)
                )
            }
        }
        super.refreshContent(e, highlight)
    }

    // Tự động điều chỉnh vị trí để tooltip không bị cắt mép màn hình
    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val offset = MPPointF()
        val chart = chartView ?: return offset

        // Căn giữa theo trục ngang
        offset.x = -width / 2f
        // Nằm phía trên điểm chạm 15px
        offset.y = -height.toFloat() - 15f

        // Xử lý tràn mép trái
        if (posX + offset.x < 0) {
            offset.x = -posX
        }
        // Xử lý tràn mép phải
        if (posX + width + offset.x > chart.width) {
            offset.x = chart.width - posX - width
        }
        // Xử lý tràn mép trên (chuyển tooltip xuống dưới ngón tay)
        if (posY + offset.y < 0) {
            offset.y = 15f
        }

        return offset
    }
}
