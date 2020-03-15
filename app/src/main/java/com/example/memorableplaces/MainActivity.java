package com.example.memorableplaces;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    static ArrayList<LatLng> locations=new ArrayList<LatLng>();
    static ArrayList<String> places=new ArrayList<String>();
    static ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        ArrayList<String> latitudes=new ArrayList<String>();
        ArrayList<String> longitudes=new ArrayList<String>();
        locations.clear();
        latitudes.clear();
        longitudes.clear();
        places.clear();
        try {
            int i;
            places= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longs",ObjectSerializer.serialize(new ArrayList<String>())));
            if(latitudes.size()>0 && longitudes.size()>0 && places.size()>0){
                if(places.size()==longitudes.size() && places.size()==latitudes.size()){
                    for(i=0;i<latitudes.size();i++){
                        locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
            }
            else {
                places.add("Add a Place...");
                locations.add(new LatLng(0,0));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("error", "onCreate: ");
        }
        listView=findViewById(R.id.listView);
        adapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,places);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this,places.get(position), Toast.LENGTH_SHORT).show();
                view.setSelected(true);
                final int i=position;
                if(i!=0) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_delete)
                            .setTitle("Are you sure")
                            .setMessage("Do you want to delete this Item")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    locations.remove(i);
                                    places.remove(i);
                                    adapter.notifyDataSetChanged();
                                    try {
                                        ArrayList<String> latitudes=new ArrayList<String>();
                                        ArrayList<String> longitudes=new ArrayList<String>();
                                        for(LatLng i:MainActivity.locations){
                                            latitudes.add(Double.toString(i.latitude));
                                            longitudes.add(Double.toString(i.longitude));
                                        }
                                        sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
                                        sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();
                                        sharedPreferences.edit().putString("longs",ObjectSerializer.serialize(longitudes)).apply();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                return true;
            }

        });
     }
}
