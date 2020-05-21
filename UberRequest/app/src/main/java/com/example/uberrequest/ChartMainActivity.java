package com.example.uberrequest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChartMainActivity extends AppCompatActivity {


    Button btnShowBarChart;
    Button btnShowPieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_main);

        btnShowBarChart = (Button) findViewById(R.id.btnBarChart);
        btnShowBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ChartBarActivity.class));
            }
        });

        btnShowPieChart = (Button) findViewById(R.id.btnPieChart);
        btnShowPieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ChartPieActivity.class));
            }
        });

    }
}