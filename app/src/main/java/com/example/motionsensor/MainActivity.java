package com.example.motionsensor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {
//public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    Sensor accelerometer;
    public StringBuilder data;
    int row_count;

    boolean start_record;
    int row_limit_count;
    private TextView row_count_text,acceleration_text,delay_state_text;
    private EditText rowlimit_text;
    private Spinner mode_select_spinner;

    public String filename = "data.csv";
    public String delay_state;
    Timestamp start_time_stamp;
    Timestamp finish_time_stamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        delay_state="Fastest";

        row_count_text = (TextView)findViewById(R.id.row_count);
        acceleration_text = (TextView)findViewById(R.id.acceleration);
        delay_state_text = (TextView)findViewById(R.id.delay_state);
        rowlimit_text = (EditText)findViewById(R.id.rowlimit);

        //Spinner
        mode_select_spinner = (Spinner)findViewById(R.id.selectmode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.modes,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mode_select_spinner.setAdapter(adapter);
        mode_select_spinner.setOnItemSelectedListener(this);

        row_count =0;
        start_record=false;
//        row_limit_count =  Integer.parseInt(String.valueOf(rowlimit_text.getText()));
        row_limit_count=1000;
        data = new StringBuilder();
        data.append(",X,Y,Z\n");
    }

    public void setStart_record(View view){
        // clear the StringBuilder after send the data
        data=new StringBuilder();
//        data.append("# this is meta data \n");
        data.append("Timestamp,X,Y,Z\n");
        row_count_text.setText("Data Clear");

        start_time_stamp = new Timestamp(currentTimeMillis());
        filename = start_time_stamp+"_data.csv";
        row_limit_count =  Integer.parseInt(String.valueOf(rowlimit_text.getText()));

        start_record=true;
        Log.d(TAG,"Start Record");
        row_count_text.setText("start record \nCurrent Row Count: "+row_count);
    }

    
    public void send_record(View view){
        finish_time_stamp = new Timestamp(currentTimeMillis());
//        filename = delay_state+"__"+start_time_stamp+"__"+finish_time_stamp+"row_"+row_limit_count+"data.csv";
        filename = start_time_stamp+"data.csv";
        try{
            FileOutputStream out = openFileOutput(filename,Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(),filename);
            Uri path = FileProvider.getUriForFile(context,
                    "com.example.motionsensor.fileprovider",
                    filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT,"Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM,path);
            startActivity((Intent.createChooser(fileIntent,"Send Mail")));


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setDelay_FASTED(View view){
        delay_state="Fastest";
        sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        delay_state_text.setText(delay_state);
    }
    public void setDelay_GAME(View view){
        delay_state="Game";
        sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_GAME);
        delay_state_text.setText(delay_state);
    }
    public void setDelay_UI(View view){
        delay_state="UI";
        sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        delay_state_text.setText(delay_state);
    }
    public void setDelay_NORMAL(View view){
        delay_state="Normal";
        sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        delay_state_text.setText(delay_state);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
//        Log.d(TAG,"onSensorChanged X: "+event.values[0]+" Y: "+event.values[1]+" Z: "+event.values[2]);
        Timestamp time_stamp = new Timestamp(currentTimeMillis());
        if(start_record==true) {
            if (row_count <= row_limit_count) {
                row_count++;
                data.append(String.format("%s,%s,%s,%s\n", time_stamp, event.values[0], event.values[1], event.values[2]));
//                Log.d(TAG,"recording...");
                row_count_text.setText("start record \nCurrent Row Count: "+row_count);
                acceleration_text.setText(time_stamp+"\nX: "+event.values[0]+"\nY: "+event.values[1]+"\nZ: "+event.values[2]);
            } else {
//                Log.d(TAG, String.valueOf(data));
                start_record = false;
                row_count_text.setText("Record finish");
                row_count=0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);

        if(text == "Accelerometer_gravity"){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else if(text=="Accelerometer_uncalibrated"){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
        }
        else if(text=="Gyroscope"){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        else if(text=="linear_acceleration"){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
