package ca.uwaterloo.lab1_205_06;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ca.uwaterloo.sensortory.LineGraphView;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setup the UI and initialize sensor event managers
        doInitialize();
    }

    //sensorManager instance used to create Sensor_Manager's
    SensorManager sensorManager;

    //linegraphview for displaying accelerometer data
    LineGraphView graph;

    //List of each sensor manager (each one responsible for handling a different sensor)
    LinkedList<Sensor_Manager> managers;

    //clear readings button
    int btn_clear = 1;
    //record last 100 readings button
    int btn_record = 2;

    private void doInitialize()
    {
        //initialize the sensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //add UI controls and event handlers for sensors
        doInitializeSensors();

    }


    private void doInitializeSensors()
    {
        //get a reference to the linearlayout
        LinearLayout linlayout = (LinearLayout) findViewById(R.id.LinearLayoutDisplay);
        //create a new graph instance with the specified withd and height
        graph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"), 1020, 600);
        graph.setVisibility(View.VISIBLE);
        //add teh graph to teh linearlayout
        linlayout.addView(graph);

        //create button for clearing min/max readings
        Button btn_clearMinMax = new Button(getApplicationContext());
        btn_clearMinMax.setText("Clear Historical Min/Max");
        btn_clearMinMax.setOnClickListener(this);
        //set the id of the button so we can refer to it later
        btn_clearMinMax.setId(btn_clear);
        linlayout.addView(btn_clearMinMax);

        //create button for recording last 100 readins of accelerometer
        Button btn_Recordvalues = new Button(getApplicationContext());
        btn_Recordvalues.setText("Record last 100 readings");
        btn_Recordvalues.setOnClickListener(this);
        //set the id of the button so we can refer to it later
        btn_Recordvalues.setId(btn_record);
        linlayout.addView(btn_Recordvalues);

        //List of sensor types that we want to create a Sensor_Manger for
        List<Sensor> SensorsToUse = new LinkedList<Sensor>();
        managers = new LinkedList<Sensor_Manager>();
        //add the 5 types of sensors
        SensorsToUse.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        SensorsToUse.add(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        SensorsToUse.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        SensorsToUse.add(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        SensorsToUse.add(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));

        //create and initialize displays and handlers for each sensor
        for (Sensor s : SensorsToUse)
        {
            //create the textview for displaying realtime values
            TextView tv = new TextView(getApplicationContext());
            //create the textview for displaying min/mas values
            TextView tvm = new TextView(getApplicationContext());
            //set the text color and initialize the textview to a default message
            tv.setTextColor(Color.WHITE);
            tv.setText("No Sensor Present");
            tvm.setTextColor(Color.WHITE);
            tvm.setText("No Sensor Present");
            //add the textviews to the linearlayout
            linlayout.addView(tv);
            linlayout.addView(tvm);
            //create a new Sensor_Manager instance for the sensor
            Sensor_Manager sm = new Sensor_Manager(tv, tvm, graph);
            //if accelerometer we are initializing set its update speed to GAME_DELAY
            if(s == sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)){
                sensorManager.registerListener(sm, s, SensorManager.SENSOR_DELAY_GAME);
            } else {
                sensorManager.registerListener(sm, s, SensorManager.SENSOR_DELAY_NORMAL);
            }
            //add this manager to our list of Sensor_Managers so we can refer to it later
            managers.add(sm);
        }

    }

    //write the last 100 readings to a .csv file
    private int recordData()
    {
        //initialize the printwriter
        PrintWriter writer = null;
        try
        {

            //create the filepath for our output file
            File path = new File(getExternalFilesDir("Recorded Data"), "AccelerometerReadings.csv");

            //open the file
            FileOutputStream sData = new FileOutputStream(path);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(sData);
            BufferedWriter myBufferedWriter = new BufferedWriter(myOutWriter);
            writer = new PrintWriter(myBufferedWriter);


            //get the values to record
            float[][] values = managers.get(0).collectHistory();

            //write headings for columns
            writer.println("X,Y,Z");
            //write comma delimited values
            for (int i = 0; i < 100; i++)
            {
                //write the values to the debug log for faster access to the values (during debugging)
                Log.d("Data", String.format("%f,%f,%f",values[0][i], values[1][i], values[2][i]));
                //write the values to the file
                writer.println(String.format("%f,%f,%f",values[0][i], values[1][i], values[2][i]));
            }

            //close the printwriter
            writer.close();


        }
        catch (IOException ex)
        {
            //log any errors
            Log.e("ERROR WRITING TO FILE", ex.toString());
            //failed to write values
            return -1;
        }
        finally
        {
            //ensure the file is closed when done
            if (writer != null)
            {
                writer.close();
            }
        }
        //values successfully written
        return 0;
    }



    @Override
    //handle button clicks
    public void onClick(View v) {
        //was the clear min/max values button
        if (v.getId() == btn_clear)
        {
            Context clearContext = getApplicationContext();
            //display message to user that the values were cleared
            CharSequence clearText = "Cleared all maximum/minimum values!";
            int clearDuration = Toast.LENGTH_SHORT;

            Toast.makeText(clearContext, clearText, clearDuration).show();

            //clear all the sensors min/max values
            for (Sensor_Manager sm : managers) {
                sm.ClearMinMax();
            }
        }
        //was the record last 100 values button
        else if (v.getId() == btn_record)
        {
            //record data - store the result of the write opperation in recorded (0 = written, -1 = failed to write)
            int recorded = recordData();

            Context historyContext = getApplicationContext();
            //display message to user that the values were recorded
            CharSequence historyText = "Recorded last 100 accelerometer values!";
            CharSequence historyTextFailed = "FAILED to Record last 100 accelerometer values!";
            int historyDuration = Toast.LENGTH_SHORT;

            //display message to user
            if (recorded == 0)
            {
                Toast.makeText(historyContext, historyText, historyDuration).show();
            }
            else
            {
                Toast.makeText(historyContext, historyTextFailed, historyDuration).show();
            }




        }
    }
}
