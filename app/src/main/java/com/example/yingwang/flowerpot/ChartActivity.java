package com.example.yingwang.flowerpot;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RelativeLayout;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;


public class ChartActivity extends ActionBarActivity {

    private GraphicalView mChart;
    private GraphicalView barChart;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;
    RelativeLayout layout_xian;
    RelativeLayout layout_zhu;

    List<double[]> x = new ArrayList<double[]>();
    List<double[]> y = new ArrayList<double[]>();


    private void initChart() {
        mCurrentSeries = new XYSeries("日常数据");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();

        mRenderer.addSeriesRenderer(mCurrentRenderer);
        mRenderer.setBarSpacing(0.01);//设置间距
        mRenderer.setXLabels(0);//设置 X 轴不显示数字（改用我们手动添加的文字标签））;//设置X轴显示的刻度标签的个数
        mRenderer.setYLabels(15);// 设置合适的刻度，在轴上显示的数量是 MAX / labels

        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);//设置y轴显示的分列，默认是 Align.CENTER
        mRenderer.setPanEnabled(true, false);//设置x方向可以滑动，y方向不可以滑动
        mRenderer.setZoomEnabled(false,false);//设置x，y方向都不可以放大或缩小
        SimpleSeriesRenderer r = mRenderer.getSeriesRendererAt(0);
        r.setDisplayChartValues(true);//设置是否在主题上方显示值

        r.setChartValuesSpacing(3);//柱体上方字的与柱体顶部的距离
        r.setGradientEnabled(true);
        r.setGradientStart(20, Color.BLUE);
        r.setGradientStop(100, Color.GREEN);
    }

    private void addSampleData() {
        AVQuery<AVObject> query = new AVQuery<AVObject>("FlowerRecord");
        query.whereEqualTo("uid", Constants.UID);
        query.orderByAscending("iid");
        try {
            query.findInBackground(new FindCallback<AVObject>() {
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null) {
                        for(AVObject avObject: avObjects){
                            String temp = String.valueOf(avObject.getInt("temp"));
                            String water = String.valueOf(avObject.getInt("water"));
                            Integer isDry = avObject.getInt("is_dry");
                            String time = avObject.getString("time");
                            Integer flowerID = avObject.getInt("fid");
                            int hour = Integer.valueOf(time.split(":")[0]);
                            mCurrentSeries.add(hour, Integer.valueOf(water));
                        }
                        Log.e("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                        mChart = ChartFactory.getCubeLineChartView(ChartActivity.this, mDataset, mRenderer, 0.3f);
                        barChart = ChartFactory.getBarChartView(ChartActivity.this, mDataset, mRenderer, BarChart.Type.DEFAULT);
                        layout_xian.addView(mChart);
                        layout_zhu.addView(barChart);

                    } else {
                        Log.e("失败", "查询错误: " + e.getMessage());
                    }

                }
            });


        }catch (Exception ex){
            Log.e("avsearch","failed",ex);
            return;
        }
    }

    protected void onResume() {
        super.onResume();
        /*layout_zhu = (RelativeLayout) findViewById(R.id.layout_zhu);
        layout_xian = (RelativeLayout) findViewById(R.id.layout_xian);
        if (mChart == null) {
            initChart();
            addSampleData();
        } else {
            mChart.repaint();
        }*/


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chart);

        generate_data();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {


                Intent intent = new Intent();

        /* 指定intent要启动的类 */
                intent.setClass(ChartActivity.this, FullscreenActivity.class);
        /* 启动一个新的Activity */
                ChartActivity.this.startActivity(intent);
        /* 关闭当前的Activity */
                ChartActivity.this.finish();
                return true;
            }

        return super.onKeyDown(keyCode, event);
    }



    private void generate_data(){
        //get data from avos

        AVQuery<AVObject> query = new AVQuery<AVObject>("FlowerRecord");
        query.whereEqualTo("uid", Constants.UID);
        query.orderByAscending("iid");
        try {
            query.findInBackground(new FindCallback<AVObject>() {
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null) {
                        double[] times = new double[avObjects.size()];
                        double[] waters = new double[avObjects.size()];
                        double[] temps = new double[avObjects.size()];
                        for(int i=0; i != avObjects.size(); ++i){
                            AVObject avObject = avObjects.get(i);
                            String temp = String.valueOf(avObject.getInt("temp"));
                            String water = String.valueOf(avObject.getInt("water"));
                            Integer isDry = avObject.getInt("is_dry");
                            String time = avObject.getString("time");
                            Integer flowerID = avObject.getInt("fid");

                            //con time
                            String[] timec = time.split(":");
                            double real_time = Integer.valueOf(timec[0]) + Double.valueOf(timec[1]) * 0.01;
                            Log.e("real_time", Double.toString(real_time));
                            times[i] = real_time;
                            waters[i] = Double.valueOf(water);
                            temps[i] = Double.valueOf(temp);


                        }
                        x.add(times);
                        x.add(times);
                        y.add(waters);
                        y.add(temps);

                        GraphicalView view = new AverageCubicTemperatureChart().generateView(ChartActivity.this.getApplicationContext(),x,y);
                        RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout);
                        layout.addView(view);

                        Log.e("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                    } else {
                        Log.e("失败", "查询错误: " + e.getLocalizedMessage(),e);
                    }

                }
            });


        }catch (Exception ex){
            Log.e("avsearch","failed",ex);
            return;
        }
    }
}
