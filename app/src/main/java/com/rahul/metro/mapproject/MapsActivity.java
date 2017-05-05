package com.rahul.metro.mapproject;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rahul.metro.mapproject.model.DirectionsModel;
import com.rahul.metro.mapproject.model.GeocodedWaypoint;
import com.rahul.metro.mapproject.model.Route;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    LatLng noida = new LatLng(28.575055, 77.356026);
    LatLng gurgaon = new LatLng(28.460260, 77.072290);
    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private String url = "https://maps.googleapis.com/maps/api/directions/json?origin=noida&destination=gurgaon&key=AIzaSyAQkN5Q8ADqmY1CLprYzHkCy49lcBMx_jo";
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestQueue = Volley.newRequestQueue(this);


        gson = new GsonBuilder().create();
    }

    private void reqpuestDirection(String url) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                DirectionsModel directionsModel = gson.fromJson(response, DirectionsModel.class);

                if (directionsModel.getStatus().toLowerCase().contains("ok")) {
                    List<Route> mRouteList = directionsModel.getRoutes();
                    List<GeocodedWaypoint> mGeocodedWaypoints = directionsModel.getGeocodedWaypoints();
                    String hashDecode = mRouteList.get(0).getOverviewPolyline().getPoints();
                    List<LatLng> mLatLngs = decodePoly(hashDecode);
                    for (int z = 0; z < mLatLngs.size() - 1; z++) {
                        if (z == 0)
                        mMap.addMarker(new MarkerOptions().position(mLatLngs.get(0)).title("Hudda City Center")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        if (mLatLngs.size() -2 == z )
                        mMap.addMarker(new MarkerOptions().position(mLatLngs.get(mLatLngs.size()-1)).title("Noida City Center")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        LatLng src = mLatLngs.get(z);
                        LatLng dest = mLatLngs.get(z + 1);
                        mMap.addPolyline(new PolylineOptions()
                                .add(new LatLng(src.latitude, src.longitude),
                                        new LatLng(dest.latitude, dest.longitude))
                                .width(5).color(Color.BLUE).geodesic(true));
                    }
                    System.out.print("direction " + response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(stringRequest);

    }

//    private void reqpuestDirection(String url) {
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String jsonData) {
//
//                System.out.println("Direction response "+jsonData);
//                JSONObject jObject;
//                List<List<HashMap<String, String>>> routes = null;
//
//                try {
//                    jObject = new JSONObject(jsonData);
//                    Log.d("ParserTask",jsonData.toString());
//                    DataParser parser = new DataParser();
//                    Log.d("ParserTask", parser.toString());
//
//                    // Starts parsing data
//                    routes = parser.parse(jObject);
//                    Log.d("ParserTask","Executing routes");
//                    Log.d("ParserTask",routes.toString());
//
//                } catch (Exception e) {
//                    Log.d("ParserTask",e.toString());
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        });
//        requestQueue.add(stringRequest);
//    }


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

        // Add a marker in Sydney and move the camera


     /*   mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });

        mMap.addPolyline(new PolylineOptions().add(noida, gurgaon).width(5).color(Color.RED));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);*/

        reqpuestDirection(url);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gurgaon, 10f));
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
