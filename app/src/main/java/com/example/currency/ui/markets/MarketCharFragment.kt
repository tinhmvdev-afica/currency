package com.example.currency.ui.markets

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.currency.R
import com.example.currency.data.model.CoinMarketItem
import com.example.currency.data.model.PricePoint
import com.example.currency.data.repository.CurrencyMockRepository
import com.example.currency.databinding.FragmentMarketChartBinding
import com.example.currency.helper.ChartHelper
import com.example.currency.helper.ChartHelper.getTimeFormatter
import com.example.currency.helper.ChartHelper.reducePoints
import com.example.currency.viewmodel.CoinViewModel
import com.github.mikephil.charting.components.XAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MarketChartFragment : Fragment() {
    private var _binding: FragmentMarketChartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CoinViewModel by activityViewModels()
    private lateinit var selectedCoin: CoinMarketItem
    private var days: Int = 1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setOnClickListener {
            binding.priceChart.highlightValue(null)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                selectedCoin = state.selectedCoin ?: return@collect
                setup(selectedCoin)
                viewModel.loadChart(selectedCoin.currency.id, days = days)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chartUiState.collect { state ->
                if (!state.isLoading) {
                    renderChart(state.chartData)
                }
            }
        }
        binding.priceChart.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Yêu cầu NestedScrollView/ViewGroup cha KHÔNG ĐƯỢC chặn/cướp touch
                    binding.priceChart.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Trả lại quyền cuộn cho NestedScrollView khi nhấc tay hoặc hủy chạm
                    binding.priceChart.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            // Trả về false để LineChart vẫn nhận được event và tự xử lý tính toán MarkerView của nó
            false
        }
    }

    private fun setup(coin: CoinMarketItem){
        val currency = coin.currency

        binding.ivCoinIcon.load(currency.iconUrl) {
            crossfade(true)
        }
        binding.tvCoinName.text = currency.name
        binding.tvCoinSymbol.text = currency.symbol
        coin.marketCapRank?.let { rank ->
            binding.tvMarketCapRank.text = getString(R.string.market_cap_rank, rank)
            binding.tvMarketCapRank.visibility = View.VISIBLE
        } ?: run {
            binding.tvMarketCapRank.visibility = View.GONE
        }

        binding.tvCurrentPrice.text = formatUsd(coin.currentPrice)
        binding.tvLow24h.text = formatUsd(coin.low24h)
        binding.tvHigh24h.text = formatUsd(coin.high24h)
        binding.tvMarketCap.text = ChartHelper.formatCompactCurrency(coin.marketCap)


        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        setupChartTabs()
    }
    private fun setupChartTabs() {
        binding.tab1D.setOnClickListener {
            selectChartRange(1)
        }
        binding.tab1W.setOnClickListener {
            selectChartRange(7)
        }
        binding.tab1M.setOnClickListener {
            selectChartRange(30)
        }
        binding.tab3M.setOnClickListener {
            selectChartRange(90)
        }
        binding.tab1Y.setOnClickListener {
            selectChartRange(365)
        }
        // mặc định chọn 1D
        updateSelectedTab(binding.tab1D)
    }
    private fun selectChartRange(newDays: Int) {
        binding.priceChart.highlightValue(null)
        if (days == newDays) return
        days = newDays

        val currency = viewModel.uiState.value.selectedCoin?.currency
            ?: return

        viewModel.loadChart(
            id = currency.id,
            days = days
        )


        when (days) {
            1 -> updateSelectedTab(binding.tab1D)
            7 -> updateSelectedTab(binding.tab1W)
            30 -> updateSelectedTab(binding.tab1M)
            90 -> updateSelectedTab(binding.tab3M)
            365 -> updateSelectedTab(binding.tab1Y)
        }
    }

    private fun formatUsd(value: Double?): String {
        return value?.let { "$" + CurrencyMockRepository.formatNumber(it) } ?: "—"
    }
    private fun updateSelectedTab(selectedTab: TextView) {

        val tabs = listOf(
            binding.tab1D,
            binding.tab1W,
            binding.tab1M,
            binding.tab3M,
            binding.tab1Y
        )
        tabs.forEach { tab ->
            if (tab == selectedTab) {
                tab.setBackgroundResource(
                    R.drawable.bg_chart_tab_selected
                )
                tab.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.brand_primary
                    )
                )
                tab.setTypeface(null, Typeface.BOLD)
            } else {
                tab.background = null
                tab.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.content_secondary
                    )
                )
                tab.setTypeface(null, Typeface.NORMAL)
            }
        }

    }
    private fun renderChart(pricePoints: List<PricePoint>) {
        if (pricePoints.isEmpty()) return
// Lấy giá đầu kỳ và giá cuối kỳ
        val startPrice = pricePoints.first().price
        val endPrice = pricePoints.last().price

        val diffAmount = endPrice - startPrice
        val diffPercent =
            if (startPrice > 0) {
                (diffAmount / startPrice) * 100
            } else {
                0.0
            }
        val isPos = diffAmount >= 0
        val arrow = if (isPos) "▲" else "▼"
        val sign = if (isPos) "+" else "-"
        val formattedDiff = ChartHelper.formatPrice(
            kotlin.math.abs(diffAmount).toFloat()
        )
        binding.tvPriceChange.text = String.format(
            Locale.US,
            "%s %s%s (%s%.2f%%)",
            arrow,
            sign,
            formattedDiff,
            sign,
            kotlin.math.abs(diffPercent)
        )

        binding.tvPriceChange.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isPos) {
                    R.color.status_positive
                } else {
                    R.color.status_negative
                }
            )
        )
        

        if (pricePoints.isEmpty()) return
        binding.tvStartPeriodPrice.text = getString(
            R.string.chart_start_price,
            ChartHelper.formatPrice(pricePoints.first().price.toFloat())
        )
        val reducedPoints = reducePoints(pricePoints, 80)
        // 1. Chuyển đổi dữ liệu: X = index mốc giờ, Y = Giá trị
        val entries = reducedPoints .mapIndexed { index, point ->
            Entry(
                index.toFloat(),
                point.price.toFloat()
            )
        }
        val timeFormat = ChartHelper.getTimeFormatter(days)
        val lineColor = ContextCompat.getColor(requireContext(), R.color.brand_primary)
        val fillColor = ContextCompat.getColor(requireContext(), R.color.brand_container)
        val textColor = ContextCompat.getColor(requireContext(), R.color.content_tertiary)
        val gridColor = ContextCompat.getColor(requireContext(), R.color.outline_default)
        val dataSet = LineDataSet(entries, "Price").apply {
            color = lineColor
            lineWidth = 2.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER // Đường cong mượt mà
            setDrawCircles(false)               // Ẩn chấm tròn từng điểm
            setDrawValues(false)                // Ẩn chữ số trên từng điểm
            setDrawFilled(true)                 // Đổ màu dải phía dưới
            this.fillColor = fillColor
            fillAlpha = 120
            highLightColor = lineColor
            isHighlightEnabled = true
            setDrawHighlightIndicators(true)
            setDrawVerticalHighlightIndicator(true)
            setDrawHorizontalHighlightIndicator(false) // Chỉ hiện đường dóng dọc
            highlightLineWidth = 1.2f
            enableDashedHighlightLine(10f, 5f, 0f)
        }
        val markerView = PriceMarkerView(requireContext(), reducedPoints).apply {
            chartView = binding.priceChart // Bắt buộc để tính toán toạ độ tràn viền
        }
        val lineData = LineData(dataSet)
        // Định dạng giờ (HH:mm)
        binding.priceChart.apply {
            marker = markerView
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_card))
            // ================= CỘT TRỤC Y (BÊN TRÁI): HIỂN THỊ GIÁ TRỊ ($) =================
            axisLeft.apply {
                this.textColor = textColor
                this.gridColor = gridColor
                setDrawAxisLine(false)
                setLabelCount(5, true)
                // Format giá trị hiển thị dạng tiền tệ (VD: $76,845 hoặc $2,519.49)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return ChartHelper.formatPrice(value)
                    }
                }
            }
            // ================= TRỤC X (BÊN DƯỚI): HIỂN THỊ GIỜ (HH:mm) =================
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                this.textColor = textColor
                this.gridColor = gridColor
                setDrawAxisLine(false)
                // Chỉ định khoảng cách để các nhãn giờ không đè lên nhau
                granularity = 1f
                setLabelCount(5, true)
                // Lấy timestamp từ pricePoints tương ứng với index để hiển thị GIỜ
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in reducedPoints .indices) {
                            timeFormat.format(Date(reducedPoints [index].timestamp))
                        } else {
                            ""
                        }
                    }
                }
            }
            // Cấu hình tương tác
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            // Hiệu ứng mượt khi vẽ
//            maxHighlightDistance = 30f
            animateX(500)
            invalidate()
        }
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.priceChart.highlightValue(null)
            }
            false
        }
    }
}
