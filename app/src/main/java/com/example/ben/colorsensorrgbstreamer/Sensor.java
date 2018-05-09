package com.example.ben.colorsensorrgbstreamer;

import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.HashMap;

public class Sensor {

    private String name;

    private TextView redTextView;
    private TextView greenTextView;
    private TextView blueTextView;

    private SurfaceView surface;

    private HashMap<String, Double> normalizationValues = new HashMap<>();

    private Double redVal = 0.0;
    private Double greenVal = 0.0;
    private Double blueVal = 0.0;

    public Sensor(){
        normalizationValues.put("red", Double.POSITIVE_INFINITY);
        normalizationValues.put("green", Double.POSITIVE_INFINITY);
        normalizationValues.put("blue", Double.POSITIVE_INFINITY);
    }

    public Sensor(TextView redTextView, TextView greenTextView, TextView blueTextView, SurfaceView surface){
        this.redTextView = redTextView;
        this.greenTextView = greenTextView;
        this.blueTextView = blueTextView;

        this.surface = surface;

        normalizationValues.put("red", Double.POSITIVE_INFINITY);
        normalizationValues.put("green", Double.POSITIVE_INFINITY);
        normalizationValues.put("blue", Double.POSITIVE_INFINITY);
    }

    public void setName(String name){
        this.name = name;
    }

    private void setColorVal(String colorStr, Double colorVal){
        switch(colorStr){
            case "red":
                this.redVal = colorVal;
                break;
            case "green":
                this.greenVal = colorVal;
                break;
            case "blue":
                this.blueVal = colorVal;
                break;
            default:
                break;
        }
    }

    public void setRedTextView(TextView redTextView) {
        this.redTextView = redTextView;
    }

    public void setGreenTextView(TextView greenTextView) {
        this.greenTextView = greenTextView;
    }

    public void setBlueTextView(TextView blueTextView) {
        this.blueTextView = blueTextView;
    }

    public void setSurface(SurfaceView surface) {
        this.surface = surface;
    }

    public void computeAndSetSurfaceColor(String colorStr, Double colorNum){
        //modify appropriate color variable to either be the maximum value, or the passed in value
        Log.i("Sensor Name: ", name);
        Log.i("Color Being Processed: ", colorStr);
        if(colorNum > normalizationValues.get(colorStr)){
            setColorVal(colorStr, normalizationValues.get(colorStr));
            Log.i("Color Number: ", normalizationValues.get(colorStr).toString());
        }
        else{
            Log.i("Color Number: ", colorNum.toString());
            setColorVal(colorStr, colorNum);
        }

        int color = calculateRGB();

        String redStr = "" + Color.red(color);
        String greenStr = "" + Color.green(color);
        String blueStr = "" + Color.blue(color);

        Log.i("Normalization Value: ", normalizationValues.get(colorStr).toString());
        Log.i("Red: ", redStr);
        Log.i("Green: ", greenStr);
        Log.i("Blue: ", blueStr);

        setSurfaceColor(color);
    }

    public void setSurfaceColor(int color){
        String redStr = "R: " + Color.red(color);
        String greenStr = "G: " + Color.green(color);
        String blueStr = "B: " + Color.blue(color);
        surface.setBackgroundColor(color);
        redTextView.setText(redStr);
        greenTextView.setText(greenStr);
        blueTextView.setText(blueStr);
    }

    private int calculateRGB(){
        int red = 0;//(int) (redVal / normalizationValues.get("red") * 255);
        int green = (int) (greenVal / normalizationValues.get("green") * 255);
        int blue = (int) (blueVal / normalizationValues.get("blue") * 255);
        return Color.rgb(red, green, blue);
    }

    public void calibrate(){
        //find the normalization values
        normalizationValues.put("red", redVal);
        normalizationValues.put("green", greenVal);
        normalizationValues.put("blue", blueVal);
    }
}
