package com.example.uberrequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnalysePoints extends AppCompatActivity {

    private final List<LatLng> analyseList = new ArrayList<>();
    private List<String> list = new ArrayList<>();
    private  int locCounter;
    private ListView listview;
    private Button btnShowMapsAnalyze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse_points);
        listview = (ListView) findViewById(R.id.listview);
        showPoints();
    }
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    private void showPoints() {
        locCounter = 1;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //List<String> list = new ArrayList<>();
                    list.add("Analyse Points and number of samples");
                    int itemCounter = 1;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        LatLng item = new LatLng(Double.parseDouble(document.get("lang").toString()),
                                Double.parseDouble(document.get("lat").toString()));
                        analyseList.add(item);
                        int count = Integer.parseInt(document.get("count").toString());
                        list.add(itemCounter + " - " + item.latitude + " , " + item.longitude + "  ->  " + count);
                        itemCounter++;
                    }
                    fillList();
                } else {
                    Toast.makeText(AnalysePoints.this, "Error getting documents", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fillList(){
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        btnShowMapsAnalyze = (Button) findViewById(R.id.btnShowMapsAnalyze);
        btnShowMapsAnalyze.setEnabled(true);
        btnShowMapsAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MapsActivityAnalyze.class));
            }
        });
    }
}