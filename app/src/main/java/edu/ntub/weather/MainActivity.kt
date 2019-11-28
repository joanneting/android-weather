package edu.ntub.weather

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import edu.ntub.weather.dto.Data
import edu.ntub.weather.dto.Location
import edu.ntub.weather.dto.Point
import edu.ntub.weather.dto.Weather
import edu.ntub.weather.helper.AxisHelper
import edu.ntub.weather.helper.DataHelper
import edu.ntub.weather.helper.LegendHelper
import edu.ntub.weather.network.OkHttpConfig
import edu.ntub.weather.network.RetrofitConfig
import edu.ntub.weather.network.URLConnectionConfig
import edu.ntub.weather.set.ColorSet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert_dialog_weather.view.*
import java.net.URLConnection
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var locations: List<Location> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.locations = getLocations()
        createSpnDate()
        createRecycleViewWeather()
    }

    private fun createSpnDate() {
        // String array
        val dates = getDates()

        // Adapter for spinner
        spnDate.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dates)

        // item selected listener for spinner
        spnDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val adapter = recycleViewWeather.adapter as WeatherListAdapter
                adapter.setTimeIndex(position * 2)
                recycleViewWeather.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun getLocations(): List<Location> {
        var data: Data? = null
        val networkThread = Thread(Runnable {
//            data = OkHttpConfig().get("/json/weather.json", Data::class.java)//java data.class
            data = Data()
            data!!.content= DataHelper.getWeather(this@MainActivity,"data")
        })
        networkThread.start()
        networkThread.join()

        val weather = if (data != null) {
            data!!.content
        } else {
            DataHelper.getWeather(this@MainActivity, "data")
        }

        return weather.dataSet.locations
    }

    private fun getDates(): List<String> {
        return locations[0].weatherElements[0].times.map {
            it.middleLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        }.distinct().dropLast(1)
    }

    private fun createRecycleViewWeather() {
        recycleViewWeather.layoutManager = LinearLayoutManager(this)

        val adapter = WeatherListAdapter(this.locations, this)
        adapter.updateData(this.locations)
        adapter.setListener(object : WeatherListAdapter.OnLocationClickListener {
            override fun onClick(location: Location) {
                val builder = AlertDialog.Builder(this@MainActivity)
                val layoutInflater = LayoutInflater.from(this@MainActivity)
                val view = layoutInflater.inflate(R.layout.alert_dialog_weather, null, false)
                view.txtCity.text = location.locationName
                val weatherLineChart = view.lineChartWeather
                chartWeatherHandler(chart = weatherLineChart, location = location)
                builder.setView(view)
                builder.show()
            }
        })
        recycleViewWeather.adapter = adapter
        recycleViewWeather.addItemDecoration(DividerItemDecoration(recycleViewWeather.context, DividerItemDecoration.VERTICAL))
    }

    private fun chartWeatherHandler(chart: LineChart, location: Location) {
        //設定圖表樣式
        chart.description.isEnabled = false //描述
        //拖動
        chart.isDragEnabled = true
        chart.setTouchEnabled(true)
        //縮放
        chart.isScaleYEnabled = false
        chart.isScaleXEnabled = false
        //與邊界距離
        chart.extraTopOffset = 30f
        chart.extraBottomOffset = 20f

        chart.setDrawGridBackground(true)
        chart.setDrawBorders(true)




        AxisHelper.create(chart)
                .leftYAxisGridLineColor(ColorSet.GRAY) //Y軸線的顏色
                .position(XAxis.XAxisPosition.BOTTOM) //X軸位置(時間)
                .textSize(14f)  //外圍數字字型大小
                .xAxisDrawGridEnable(false) //x軸線顯示
//                .yAxisLabelCount(10, false)
        //圖表下方最高溫最低溫圖示
        LegendHelper.create(chart)
                .form(Legend.LegendForm.CIRCLE)//樣式
                .formLineWidth(10f)//高度
                .isDrawInside(false)
                .orientation(Legend.LegendOrientation.HORIZONTAL) //顯示方式
                .textSize(18f)
                //位置
                .horizontalAlignment(Legend.LegendHorizontalAlignment.CENTER)
                .verticalAlignment(Legend.LegendVerticalAlignment.BOTTOM)


        // 整理資料
        val entriesMap = HashMap<String, List<Entry>>()

        val weatherElements = location.weatherElements
        val xIndexList = ArrayList<String>()
        var preWeatherName = ""
        for (weatherElement in weatherElements) {
            val entries = ArrayList<Entry>()
            val times = weatherElement.times
            val singleIndexList = ArrayList<String>()
            for (j in times.indices) { // 遍歷times有效索引
                val time = times[j]
                time.parameter.elementName = weatherElement.name
                val localDateTime = time.middleLocalDateTime
                val xTitle = localDateTime.format(DateTimeFormatter.ofPattern("MM/dd"))
                var xIndex = xIndexList.indexOf(xTitle)


                val parameter = time.parameter
                val value = parameter.value

                var singleIndex = singleIndexList.indexOf(xTitle)


                if (xIndex == -1) {
                    xIndexList.add(xTitle)
                    xIndex = xIndexList.size - 1

                }
                Log.d("xTitle",xIndexList.indexOf(xTitle).toString())
                if (singleIndex == -1) {
                    singleIndexList.add(xTitle)
                    entries.add(Entry(xIndex.toFloat(), java.lang.Float.valueOf(value), time))
                }else{
                    Log.d("singleIndex",singleIndex.toString())
                        if (weatherElement.name.equals("MaxT")) {
                            if (entries[singleIndex].y < java.lang.Float.valueOf(value)) {
                                entries[singleIndex].y = java.lang.Float.valueOf(value)
                            }
                        } else if (weatherElement.name.equals("MinT")) {
                            if (entries[singleIndex].y > java.lang.Float.valueOf(value)) {
                                entries[singleIndex].y = java.lang.Float.valueOf(value)
                            }
                        }
                }


            }

            entriesMap[weatherElement.name] = entries
        }
        // 設定圖表的資料
        val maxTemperatureDataSet = LineDataSet(entriesMap["MaxT"], "最高溫")
        //設置顏色
        maxTemperatureDataSet.color = Color.rgb(245, 177, 176)//線
        maxTemperatureDataSet.setCircleColor(Color.rgb(245, 177, 176))//圓圈
        maxTemperatureDataSet.valueTextColor = Color.rgb(245, 177, 176)

        maxTemperatureDataSet.setDrawCircleHole(false) //設置圓圈空心
        maxTemperatureDataSet.lineWidth = 4f //線條寬度
        maxTemperatureDataSet.valueTextSize = 16f

        maxTemperatureDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val minTemperatureDataSet = LineDataSet(entriesMap["MinT"], "最低溫")
        minTemperatureDataSet.color = Color.rgb(132, 170, 208)
        minTemperatureDataSet.setCircleColor(Color.rgb(132, 170, 208))
        minTemperatureDataSet.setDrawCircleHole(false)
        minTemperatureDataSet.lineWidth = 2f
        minTemperatureDataSet.valueTextSize = 16f
        minTemperatureDataSet.valueTextColor = Color.rgb(132, 170, 208)
        minTemperatureDataSet.valueFormatter = object : ValueFormatter() { //設定y軸格式(去小數點)
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        //建立圖表
        val lineData = LineData(maxTemperatureDataSet, minTemperatureDataSet)
        chart.data = lineData
        chart.setVisibleXRangeMaximum(7f) //圖表顯示筆數


        val xAxis = chart.xAxis
        xAxis.setLabelCount(7, false) //x軸顯示筆數
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return xIndexList[index]
            }
        }
    }
}