package com.example.minermonitornicehash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.security.NetworkSecurityPolicy;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.gson.Gson;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.zxing.Result;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.List;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import static android.security.NetworkSecurityPolicy.*;
import static java.lang.Math.round;

public class MainActivity extends AppCompatActivity {
    public static TextView riginfoTV;
    public static Button riginfobutton;
    public static CheckBox savewalletcb;
    public static String walletaddress;
    private LineChart lineChart;
    public float initialtime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        riginfoTV = (TextView) findViewById(R.id.rigsTextView);
        Button scanbutton = (Button) findViewById(R.id.scanButton);
        riginfobutton = (Button) findViewById(R.id.riginfoButton);
        savewalletcb = (CheckBox) findViewById(R.id.savewalletCheckBox);

        lineChart = (LineChart)findViewById(R.id.lineChart);
        NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int verticalSize = size.y  - 200;
        lineChart.setMinimumHeight(round(verticalSize/2));

        final RequestQueue queue = Volley.newRequestQueue(this);
        final String urlbase = "https://api2.nicehash.com/main/api/v2/mining/external/";
        //final String urlrigs = "/rigs/";
        final String urlrigs = "/rigs2/";
        final String urlstats = "/rigs/stats/unpaid/";
        //final String urlstats = "/rigs2/stats/unpaid/";
        final String urlalgorithms = "https://api2.nicehash.com/main/api/v2/mining/algorithms/";
        final Gson gson = new Gson();
        final ArrayList<Map> miningAlgorithms = new ArrayList<Map>();
        final ArrayList<String> detectedAlgorithms = new ArrayList<>();

        String wallet = readFromFile(this);
        if (wallet.equals("")){
            riginfoTV.setText("Please scan your Nicehash wallet QR code");
            riginfobutton.setEnabled(false);
            walletaddress = "";
            //walletaddress = "TESTWALLET";
        } else {
            walletaddress = wallet;
            riginfoTV.setText("Please push Get Information button");
        }

        scanbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);

            }
        });

        riginfobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                miningAlgorithms.clear();
                detectedAlgorithms.clear();
                final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlalgorithms, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int n = 0;
                        Map map = gson.fromJson(response, Map.class);
                        final ArrayList<Map> miningalgorithms = (ArrayList<Map>) map.get("miningAlgorithms");
                        if (miningalgorithms != null){
                            n = miningalgorithms.size();
                        }
                        for (int i=0; i < n; i++){
                            miningAlgorithms.add(miningalgorithms.get(i));
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //textView.setText("Error");
                        Toast.makeText(getApplicationContext(), String.valueOf(error), Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest);

                final StringRequest stringRequest2 = new StringRequest(Request.Method.GET, urlbase+walletaddress+urlrigs, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int n = 0;
                        Map map = gson.fromJson(response, Map.class);
                        final String address = (String) "Address: " + map.get("btcAddress");
                        final String unpaid = (String) "unpaid: " + map.get("unpaidAmount");
                        final ArrayList<Map> rigs = (ArrayList) map.get("miningRigs");
                        if (rigs != null){
                            n = rigs.size();
                        }
                        String riginfo = (String) "";
                        for (int i=0; i < n; i++){
                            riginfo = riginfo + "RigId: " + rigs.get(i).get("rigId") + '\n';
                            riginfo = riginfo + "Name: " + rigs.get(i).get("name") + '\n';
                            riginfo = riginfo + "Type: " + rigs.get(i).get("type") + '\n';
                            riginfo = riginfo + "Status: " + rigs.get(i).get("minerStatus") + '\n';
                            float profitability = 0;
                            profitability = Float.parseFloat(rigs.get(i).get("profitability").toString())*100000000;
                            String profitabilityStr = String.valueOf(profitability);
                            riginfo = riginfo + "Profit 24h: " + profitabilityStr + " BTC Satoshis" + '\n';
                            if (rigs.get(i).containsKey("devices")){
                                final ArrayList<Map> devices = (ArrayList<Map>) rigs.get(i).get("devices");
                                for (int j=0; j < devices.size(); j++){
                                    riginfo = riginfo + "Device: " + devices.get(j).get("name") + '\n';
                                    Map status = (Map) devices.get(j).get("status");
                                    riginfo = riginfo + "Status: " + status.get("enumName") + '\n';
                                    final ArrayList<Map> speeds = (ArrayList<Map>) devices.get(j).get("speeds");
                                    for (int k=0; k < speeds.size(); k++){
                                        riginfo = riginfo + "Algorithm: " + speeds.get(k).get("title") + ' ' + speeds.get(k).get("speed") + ' ' + speeds.get(k).get("displaySuffix") + '\n';
                                    }
                                }
                            };
                            if (rigs.get(i).containsKey("stats")){
                                final ArrayList<Map> stats = (ArrayList<Map>) rigs.get(i).get("stats");
                                for (int j=0; j < stats.size(); j++){
                                    Map algo = (Map) stats.get(j).get("algorithm");
                                    riginfo = riginfo + "Total for rig " + algo.get("description") + '\n';
                                    riginfo = riginfo + "Speed(a): " + stats.get(j).get("speedAccepted") + '\n';
                                    riginfo = riginfo + "Speed(r): " + stats.get(j).get("speedRejectedTotal") + '\n';
                                    //detectedAlgorithms.add(algo.get("description").toString());
                                }
                            };
                            riginfo = riginfo + '\n';
                        };
                        riginfoTV.setText(address+'\n'+unpaid+'\n'+'\n'+riginfo);
                        //Toast.makeText(getApplicationContext(),detectedAlgorithms.toString(),Toast.LENGTH_LONG).show();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //textView.setText("Error");
                        Toast.makeText(getApplicationContext(), String.valueOf(error), Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest2);

                final StringRequest stringRequest3 = new StringRequest(Request.Method.GET, urlbase+walletaddress+urlstats, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int n = 0;
                        float TIMEPERIOD = 1000*60*60*6;
                        float finaltime = 0;
                        Map map = gson.fromJson(response, Map.class);
                        final ArrayList<ArrayList> data = (ArrayList<ArrayList>) map.get("data");
                        if (data != null){
                            n = data.size();
                        }
                        LineData dataforgraph = new LineData();
                        String datainfo = (String) "";
                        String title = (String) "";
                        if (n >=1){
                            //initialtime = Float.parseFloat(data.get(n - 1).get(0).toString())
                            finaltime = Float.parseFloat(data.get(0).get(0).toString());
                            initialtime = finaltime - TIMEPERIOD;
                        } else {
                            n = 0;
                        }
                        //ArrayList<String> detectedAlgorithms = new ArrayList<String>();
                        detectedAlgorithms.clear();
                        for (int i = 0; i < n; i++) {
                            String order = data.get(i).get(1).toString();
                            if (finaltime-Float.parseFloat(data.get(i).get(0).toString()) <= TIMEPERIOD) {
                                if (!detectedAlgorithms.contains(order)) {
                                    detectedAlgorithms.add(order);
                                }
                            }
                        }
                        ArrayList<Entry> entries = new ArrayList<>();
                        for (int i = 0; i < n; i++) {
                            if (finaltime-Float.parseFloat(data.get(i).get(0).toString()) <= TIMEPERIOD) {
                                // normalize time values for XAxis (unit milliseconds from API)
                                float time = ((Float.parseFloat(data.get(i).get(0).toString()) - initialtime) / 100000);
                                //float time = ((Float.parseFloat(data.get(i).get(0).toString())) / 100000);
                                float profitability = Float.parseFloat(data.get(i).get(4).toString());
                                entries.add(new Entry(round(time), profitability * 100000000));
                            }
                        }
                        Collections.sort(entries,new EntryXComparator());
                        if (entries.size() > 0) {
                            LineDataSet lineDataSet = new LineDataSet(entries, "Profitability BTC Satoshis");
                            lineDataSet.setColor(ContextCompat.getColor(lineChart.getContext(), R.color.colorPrimary));
                            lineDataSet.setValueTextColor(ContextCompat.getColor(lineChart.getContext(), R.color.colorPrimaryDark));
                            lineDataSet.setDrawValues(false);
                            dataforgraph.addDataSet(lineDataSet);
                        }
                        /*for (int a=0; a < detectedAlgorithms.size(); a++) {
                            //ArrayList<Entry> entries = new ArrayList<>();
                            entries.clear();
                            String order = detectedAlgorithms.get(a);
                            for (int m = 0; m < miningAlgorithms.size(); m++) {
                                if (detectedAlgorithms.get(a).equals(miningAlgorithms.get(m).get("order").toString())) {
                                    title = miningAlgorithms.get(m).get("title").toString();
                                }
                            }
                            for (int i = 0; i < n; i++) {
                                String algo = data.get(i).get(1).toString();
                                if (algo.equals(order)) {
                                    if (finaltime-Float.parseFloat(data.get(i).get(0).toString()) <= TIMEPERIOD) {
                                        // normalize time values for XAxis (unit milliseconds from API)
                                        float time = ((Float.parseFloat(data.get(i).get(0).toString()) - initialtime) / 100000);
                                        //float time = ((Float.parseFloat(data.get(i).get(0).toString())) / 100000);
                                        float profitability = Float.parseFloat(data.get(i).get(3).toString());
                                        entries.add(new Entry(round(time), profitability * 100000000));
                                    }

                                }
                            }

                            Collections.sort(entries,new EntryXComparator());
                            if (entries.size() > 0) {
                                LineDataSet lineDataSet = new LineDataSet(entries, title);
                                lineDataSet.setColor(ContextCompat.getColor(lineChart.getContext(), R.color.colorPrimary + a * 20));
                                lineDataSet.setValueTextColor(ContextCompat.getColor(lineChart.getContext(), R.color.colorPrimaryDark));
                                dataforgraph.addDataSet(lineDataSet);
                            }
                            //end of loop to add lineDataSets

                        }*/

                        ValueFormatter xAxisFormatter = new ValueFormatter() {
                            @Override
                            public String getAxisLabel(float value, AxisBase axis) {
                                //return super.getAxisLabel(value, axis);
                                float timestamp = value*100000 + initialtime;
                                SimpleDateFormat formatter = new SimpleDateFormat("EEE H:mm");
                                String dateString = formatter.format(timestamp);
                                return dateString;
                            }
                        };
                        XAxis xAxis = lineChart.getXAxis();
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);
                        xAxis.setValueFormatter(xAxisFormatter);
                        YAxis yAxisRight = lineChart.getAxisRight();
                        yAxisRight.setEnabled(false);
                        YAxis yAxisLeft = lineChart.getAxisLeft();
                        yAxisLeft.setGranularity(1f);

                        lineChart.getDescription().setText("My Nicehash mining");
                        lineChart.setData(dataforgraph);
                        lineChart.animateX(2500);
                        lineChart.invalidate();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //textView.setText("Error");
                        Toast.makeText(getApplicationContext(), String.valueOf(error), Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest3);

            }
        });

    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("nhwallet.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            //Log.e("login activity", "File not found: " + e.toString());
            //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            //Log.e("login activity", "Can not read file: " + e.toString());
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }

        return ret;
    }

    public static void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("nhwallet.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            //Log.e("Exception", "File write failed: " + e.toString());
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                // do your code
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("About...");
                alertDialog.setMessage("Version 1.1\nDeveloped by MadeInPeru\nLima, January 2020\n\nPedro L. Martinez La Rosa");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
