package com.example.smarthelmet;

import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.smarthelmet.databinding.ActivityMapsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    Marker liveMarker;
    private Timer myTimer;
    Button btn;
    ProgressDialog p;
    boolean isengineOn=true;
    private LatLng latLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        btn=(Button)findViewById(R.id.btn);
        p=new ProgressDialog(this);
        p.setTitle("Please Wait...");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isengineOn=!isengineOn;
                if(isengineOn){
                  btn.setText("Engine On");
                }
                else{
                  btn.setText("Engine Off");
                }
                //This method runs in the same thread as the UI.
                RequestQueue queue1 = Volley.newRequestQueue(MapsActivity.this);
                JSONObject data=new JSONObject();
                try {
                    data.put("engineOnOfStatus",isengineOn);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                p.show();
                JsonObjectRequest request =new JsonObjectRequest(Request.Method.POST, ConfigSetting.host+"/Home/PostLocationStatus/", data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status=response.getString("status");
                                    Toast.makeText(MapsActivity.this,status , Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                p.hide();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        p.hide();
                        //Toast.makeText(getApplicationContext(),error.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                });
                queue1.add(request);
                //Do something to the UI thread here
            }
        });

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Timer",Toast.LENGTH_SHORT).show();
                RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
                JsonObjectRequest request =new JsonObjectRequest(Request.Method.GET, ConfigSetting.host+"/Home/GetLocationStatus/", null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    double lat=response.getDouble("lat");
                                    double lang=response.getDouble("lng");
                                    latLng = new LatLng(lat,lang);
                                    if(liveMarker == null){
                                        liveMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("My Bike is Here"));
                                    }
                                    else {
                                        liveMarker.setPosition(latLng);
                                    }
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        //Toast.makeText(getApplicationContext(),error.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                });
                queue.add(request);
                // Do the task...
                handler.postDelayed(this, 2000); // Optional, to repeat the task.
            }
        };
        handler.postDelayed(runnable, 1500);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Param is optional, to run task on UI thread.

    }



}
