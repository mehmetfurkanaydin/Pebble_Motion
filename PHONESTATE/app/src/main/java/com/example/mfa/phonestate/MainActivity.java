package com.example.mfa.phonestate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.*;
import android.hardware.Sensor;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.*;

import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity implements SensorEventListener{


    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final String TAG = "MyActivity";

    private float deltaX=0;
    private float deltaY=0;
    private float deltaZ=0;

    private float lastX=0;
    private float lastY=0;
    private float lastZ=0;

    private MobileServiceClient mClient;



    private float ThresHold=0;

    double  activeCount=0.0;
    double  passiveCount=0.0;
    double lastUpdateTable=0.0;
    double counter=0;


    private long lastUpdate;
    private long lastAPIUpdate;

    public static TextView activeText,passiveText;









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        try {
            mClient = new MobileServiceClient(
                    "xxxxxxx",
                    "xxxxxxxx",
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }





        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
         /* }
      });*/
        activeText=(TextView) findViewById(R.id.active_time);
        passiveText=(TextView) findViewById(R.id.passive_time);


        sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null){
         accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         sensorManager.registerListener(this,accelerometer,sensorManager.SENSOR_DELAY_NORMAL);
        }else{

        }
    }


   public void onResume(){
    super.onResume();
       sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
   }


    public void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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




    @Override
    public void onSensorChanged(SensorEvent event) {



        float speed;

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 1000) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            deltaX = Math.abs(lastX - event.values[0]);
            deltaY = Math.abs(lastY - event.values[1]);
            deltaZ = Math.abs(lastZ - event.values[2]);

            if (deltaX < 1)
                deltaX = 0;
            if (deltaY < 1)
                deltaY = 0;
            if (deltaZ < 1)
                deltaZ = 0;


            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];


            if (deltaX > ThresHold || deltaY > ThresHold || deltaZ > ThresHold) {
                activeCount =activeCount + 1;


                activeText.setText(String.format("%.1f", activeCount));



            } else {
                passiveCount = passiveCount + 1;
                passiveText.setText(String.format("%.1f", passiveCount));

                if ((curTime - lastAPIUpdate) > 60000) {


                    if(lastUpdateTable!=activeCount) {

                        Value value = new Value();

                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                        // Get the date today using Calendar object.
                        Date today = Calendar.getInstance().getTime();
                        // Using DateFormat format method we can create a string
                        // representation of a date with the defined format.
                        String reportDate = df.format(today);
                        value.date = reportDate;
                        value.currentvalue = Double.toString(activeCount);
                        lastUpdateTable=activeCount;




                        mClient.getTable(Value.class).insert(value, new TableOperationCallback<Value>() {
                            public void onCompleted(Value entity, Exception exception, ServiceFilterResponse response) {
                                if (exception == null) {
                                    Log.d("Insert Table", "Success");
                                } else {
                                    // Insert failed
                                    Log.d("Insert Table", "Failed");
                                }
                            }
                        });


                    }
                }

            }

        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putDouble("activeCount", activeCount);
        savedInstanceState.putDouble("passiveCount",passiveCount);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

         activeCount = savedInstanceState.getDouble("activeCount");
         passiveCount=savedInstanceState.getDouble("passiveCount");

    }

}

