package ca.uwaterloo.lab1_205_06;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import ca.uwaterloo.sensortory.LineGraphView;

/**
 * Created by Kristopher_User on 2017-05-17.
 */

//Generic sensor manager. Handles all sensor events
public class Sensor_Manager implements SensorEventListener{

    //reference to textview display that it will update (with the curernt value)
    TextView display;
    //reference to textview display that it will update (with min/max values)
    TextView displaymax;
    //reference to the linegraphview to update with readings (only used if the sensor type is accelerometer)
    LineGraphView lgraph;

    //Max and min X, Y, Z respectivly (if light sensor, only uses the "x" axis to store values)
    float Mx, mx, My, my, Mz, mz;

    //Arrays to store the last 100 readings of the x, y, z axis (only used if sensor type is accelerometer)
    float[] lastx;
    float[] lasty;
    float[] lastz;

    //creates a new sensor manager instance
    public Sensor_Manager(TextView tv, TextView tvmax, LineGraphView graph)
    {
        display = tv;
        displaymax = tvmax;
        lgraph = graph;
        //initialize min and max readings so that they will be overwritten quickly by real values
        Mx = My = Mz = 0.0f;
        mx = my = mz = 1000000000f;
        //set the size of the last readings array to store 100 readings
        lastx = new float[100];
        lasty = new float[100];
        lastz = new float[100];
    }

    //clears the min and max readings from each axis
    public void ClearMinMax()
    {
        Mx = My = Mz = mx = my = mz = 0.0f;
    }


    //no implementation
    public void onAccuracyChanged(Sensor s, int i)
    {
        //not implemented
    }


    public void onSensorChanged(SensorEvent se)
    {
        //switch on type of sensor to determine how to handle event
        //pass the event infromation to the appropriate handling method
        switch(se.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                updateAccelerometer(se);
                break;
            case Sensor.TYPE_LIGHT:
                updateLight(se);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                updateMagnetometor(se);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                updateLinAcc(se);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                updateGyro(se);
                break;
            default:
                break;
        }
    }

    private float _min(float value, float min)
    {
        //return the smaller of the 2 values
        if (value < min)
        {
            return value;
        }
        else
        {
            return min;
        }
    }

    private float _max(float value, float max)
    {
        //return the larger of the 2 values
        if (value > max)
        {
            return value;
        }
        else
        {
            return max;
        }
    }


    private void updateMinMax(SensorEvent se)
    {
        //update max values for all 3 axis
        Mx = _max( se.values[0], Mx);
        My = _max( se.values[1], My);
        Mz = _max( se.values[2], Mz);
        //update min values
        mx = _min( se.values[0], mx);
        my = _min( se.values[1], my);
        mz = _min( se.values[2], mz);
    }


    private void update_Last_Values(SensorEvent se)
    {
        //update last values
        for (int i = 0; i < 99; i++)
        {
            lastx[i] = lastx[i+1];
            lasty[i] = lasty[i+1];
            lastz[i] = lastz[i+1];
        }
        lastx[99] = se.values[0];
        lasty[99] = se.values[1];
        lastz[99] = se.values[2];
    }

    //return a 3 float arrays containing the last 100 accelerometer readings
    public float[][] collectHistory()
    {

        float[][] result;

        result = new float[3][100];
        result[0] = lastx;
        result[1] = lasty;
        result[2] = lastz;
        return result;
    }

    private void updateAccelerometer(SensorEvent se)
    {
        //update the display
        display.setText(String.format("Accelerometer:\nX: %.2f Y: %.2f Z: %.2f", se.values[0], se.values[1], se.values[2]));

        //update min/max
        updateMinMax(se);

        //display min/max readings
        displaymax.setText(String.format("Accelerometer Max:\nX: %.2f, Y: %.2f, Z: %.2f \nAccelerometer Min:\nX: %.2f, Y: %.2f, Z: %.2f", Mx, My, Mz, mx, my, mz));

        //update graph
        lgraph.addPoint(se.values);

        //update last value set
        update_Last_Values(se);
    }


    private void updateLight(SensorEvent se)
    {
        //update display
        display.setText(String.format("Light Sensor:\n" +  se.values[0]));
        //update max values
        //light sensor uses x axis
        Mx = _max( se.values[0], Mx);
        mx = _min( se.values[0], mx);
        //update min/max display
        displaymax.setText(String.format("Light Sensor Max:\n%f\nLight Sensor Min:\n%f", Mx, mx));
    }

    private void updateMagnetometor(SensorEvent se)
    {
        //update display
        display.setText(String.format("Magnetometor: X:%f Y:%f Z:%f", se.values[0], se.values[1], se.values[2]));
        //update max values
        updateMinMax(se);
        //update min/max display
        displaymax.setText(String.format("Magnetometor Max: X%f, Y%f, Z%f \nMagnetometor Min: X%f, Y%f, Z%f", Mx, My, Mz, mx, my, mz));
    }

    private void updateLinAcc(SensorEvent se)
    {
        display.setText(String.format("Linear Accelerometer: X:%f Y:%f Z:%f", se.values[0], se.values[1], se.values[2]));
        //update max values
        updateMinMax(se);
        //update min/max display
        displaymax.setText(String.format("Linear Accelerometer Max: X%f, Y%f, Z%f \nLinear Accelerometer Min: X%f, Y%f, Z%f", Mx, My, Mz, mx, my, mz));
    }

    private void updateGyro(SensorEvent se)
    {
        display.setText(String.format("Gyroscope: X:%f Y:%f Z:%f", se.values[0], se.values[1], se.values[2]));
        //update max values
        updateMinMax(se);
        //update min/max display
        displaymax.setText(String.format("Gyroscope Max: X%f, Y%f, Z%f\nGyroscope Min: X%f, Y%f, Z%f", Mx, My, Mz, mx, my, mz));
    }

}
