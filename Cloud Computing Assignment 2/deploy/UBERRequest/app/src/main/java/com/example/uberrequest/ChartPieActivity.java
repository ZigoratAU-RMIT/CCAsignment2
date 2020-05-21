package com.example.uberrequest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class ChartPieActivity extends AppCompatActivity {
    PieChartView pieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_pie);

        pieChartView = findViewById(R.id.chart);

        List pieData = new ArrayList<>();
        pieData.add(new SliceValue(24, Color.BLUE).setLabel("P1: 119203 - 24%"));
        pieData.add(new SliceValue(18, Color.GRAY).setLabel("P2: 89503 - 18%"));
        pieData.add(new SliceValue(6, Color.RED).setLabel("P3: 31276 - 6%"));
        pieData.add(new SliceValue(19, Color.GREEN).setLabel("P4: 96884 - 19%"));
        pieData.add(new SliceValue(6, Color.MAGENTA).setLabel("P5: 25780 - 6%"));
        pieData.add(new SliceValue(27, Color.DKGRAY).setLabel("P6: 137354 - 27%"));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterText1("Points in map").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartView.setPieChartData(pieChartData);
    }
}