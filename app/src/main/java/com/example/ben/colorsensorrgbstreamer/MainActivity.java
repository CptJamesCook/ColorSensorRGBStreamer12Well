package com.example.ben.colorsensorrgbstreamer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static final UUID MELODYSMART_SERVICE_UUID = UUID.fromString("bc2f4cc6-aaef-4351-9034-d66268e328f0");
    private static final UUID MELODYSMART_DATACHARACTERISTIC_UUID = UUID.fromString("06d1e5e7-79ad-4a71-8faa-373789f7d93c");
    private static final UUID MELODYSMART_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    // Note that for ASCII one through twelve, they are actually ASCII symbols a, b, c, ...etc.
    private final byte ASCII_R = 0b1010010;
    private final byte ASCII_G = 0b1000111;
    private final byte ASCII_B = 0b1000010;
    private final byte ASCII_ONE = 0x61;
    private final byte ASCII_TWO = 0x62;
    private final byte ASCII_THREE = 0x63;
    private final byte ASCII_FOUR = 0x64;
    private final byte ASCII_FIVE = 0x65;
    private final byte ASCII_SIX = 0x66;
    private final byte ASCII_SEVEN = 0x67;
    private final byte ASCII_EIGHT = 0x68;
    private final byte ASCII_NINE = 0x69;
    private final byte ASCII_TEN = 0x6A;
    private final byte ASCII_ELEVEN = 0x6B;
    private final byte ASCII_TWELVE = 0x6C;

    private Double redValue, greenValue, blueValue;
    private Double redValue1, redValue2, redValue3, redValue4, redValue5, redValue6, redValue7, redValue8, redValue9, redValue10, redValue11, redValue12;
    private Double greenValue1, greenValue2, greenValue3, greenValue4, greenValue5, greenValue6, greenValue7, greenValue8, greenValue9, greenValue10, greenValue11, greenValue12;
    private Double blueValue1, blueValue2, blueValue3, blueValue4, blueValue5, blueValue6, blueValue7, blueValue8, blueValue9, blueValue10, blueValue11, blueValue12;
    private int beginstop = 0;
    private double accumulatedtime;
    private int stopwatchgoing;


    private static final int ACCESS_COARSE_LOCATION_REQUEST = 1;
    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private static final int MSG_UPDATE = 2;
    private static final int REFRESH_RATE = 1;

    private Button BluetoothButton, ConnectButton, BeginButton, StopButton, ClearButton, SaveButton, DrawFluidButton, ReturnFluidButton;
    private TextView RedText, GreenText, BlueText, DataText, ColorText;
    private SurfaceView ColorSurface, ColorSurface2, ColorSurface4, ColorSurface5;

    private TextView Text1, Text2, Text3, Text4, Text5, Text6, Text7, Text8, Text9, Text10, Text11, Text12;
    private TextView Text17, Text18, Text19, Text20, Text21, Text22, Text24, Text25, Text26, Text27, Text29, Text30, Text31, Text32, Text33, Text34, Text35, Text36, Text37, Text38, Text39, Text40, Text41, Text42, Text43, Text44, Text45, Text47, Text49, Text51, Text52, Text54, Text55, Text56, Text57, Text58;
    private SurfaceView Surface1, Surface2, Surface3, Surface4, Surface5, Surface6, Surface7, Surface8, Surface9, Surface10, Surface11, Surface12;

    private String RedTextString, GreenTextString, BlueTextString;
    private BluetoothAdapter deviceBluetoothAdapter;
    private BluetoothLeScanner deviceLeScanner;
    private ScanCallback deviceLeScanCallback;
    private Handler bluetoothButtonHandler, scanHandler, beginHandler, stopHandler;
    private BluetoothDevice myDevice;
    private BluetoothGattCallback myDeviceGattCallback;
    private BluetoothGatt myDeviceGatt;
    private BluetoothGattService myDeviceGattService;
    private BluetoothGattCharacteristic myDeviceGattCharacteristic, myDeviceGattReceivedCharacteristic;
    private BluetoothGattDescriptor myDeviceGattDescriptor;

    private HSSFWorkbook RawDataWorkbook;
    private Sheet RawDataSheet;
    private Row NewRow;
    private int ModulusCheck, ModulusParam;

    private EditText SaveDialogFileName;
    private String SaveFileName;
    private AlertDialog myDialog;


/*
    // Obtained from https://stackoverflow.com/questions/3733867/stop-watch-logic
    Handler stopwatchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START:
                    stopwatch.start(); //start timer
                    stopwatchHandler.sendEmptyMessage(MSG_UPDATE);
                    break;

                case MSG_UPDATE:
                    accumulatedtime = (double)stopwatch.getElapsedTime()/1000;
                    StopwatchText.setText("Time: " + accumulatedtime);
                    stopwatchHandler.sendEmptyMessageDelayed(MSG_UPDATE,REFRESH_RATE); //text view is updated every second,
                    break;                                  //though the timer is still running
                case MSG_STOP:
                    stopwatchHandler.removeMessages(MSG_UPDATE); // no more updates.
                    stopwatch.stop();//stop timer;
                    break;

                default:
                    break;
            }
        }
    };*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothButton = (Button)findViewById(R.id.bluetoothbutton);
        ConnectButton = (Button)findViewById(R.id.connectbutton);
        BeginButton = (Button)findViewById(R.id.beginbutton);
        StopButton = (Button)findViewById(R.id.stopbutton);
        SaveButton = (Button)findViewById(R.id.savebutton);
        RedText = (TextView) findViewById(R.id.redtext);
        GreenText = (TextView)findViewById(R.id.greentext);
        BlueText = (TextView)findViewById(R.id.bluetext);
        DataText = (TextView)findViewById(R.id.datatext);
        ColorText = (TextView)findViewById(R.id.representativetext);
        /*ColorSurface = (SurfaceView)findViewById(R.id.surfaceView);
        ColorSurface2 = (SurfaceView)findViewById(R.id.surfaceView2);
        ColorSurface4 = (SurfaceView)findViewById(R.id.surfaceView4);
        ColorSurface5 = (SurfaceView)findViewById(R.id.surfaceView5);*/


        Text1 = (TextView)findViewById(R.id.textView1);
        Text2 = (TextView)findViewById(R.id.textView2);
        Text3 = (TextView)findViewById(R.id.textView3);
        Text4 = (TextView)findViewById(R.id.textView7);
        Text5 = (TextView)findViewById(R.id.textView8);
        Text6 = (TextView)findViewById(R.id.textView9);
        Text7 = (TextView)findViewById(R.id.textView10);
        Text8 = (TextView)findViewById(R.id.textView11);
        Text9 = (TextView)findViewById(R.id.textView12);
        Text10 = (TextView)findViewById(R.id.textView13);
        Text11 = (TextView)findViewById(R.id.textView14);
        Text12 = (TextView)findViewById(R.id.textView16);

        Surface1 = (SurfaceView)findViewById(R.id.surfaceView1);
        Surface2 = (SurfaceView)findViewById(R.id.surfaceView2);
        Surface3 = (SurfaceView)findViewById(R.id.surfaceView3);
        Surface4 = (SurfaceView)findViewById(R.id.surfaceView4);
        Surface5 = (SurfaceView)findViewById(R.id.surfaceView5);
        Surface6 = (SurfaceView)findViewById(R.id.surfaceView6);
        Surface7 = (SurfaceView)findViewById(R.id.surfaceView7);
        Surface8 = (SurfaceView)findViewById(R.id.surfaceView8);
        Surface9 = (SurfaceView)findViewById(R.id.surfaceView9);
        Surface10 = (SurfaceView)findViewById(R.id.surfaceView10);
        Surface11 = (SurfaceView)findViewById(R.id.surfaceView11);
        Surface12 = (SurfaceView)findViewById(R.id.surfaceView12);

        Text17 = (TextView)findViewById(R.id.textView17);
        Text18 = (TextView)findViewById(R.id.textView18);
        Text19 = (TextView)findViewById(R.id.textView19);
        Text20 = (TextView)findViewById(R.id.textView20);
        Text21 = (TextView)findViewById(R.id.textView21);
        Text22 = (TextView)findViewById(R.id.textView22);
        Text24 = (TextView)findViewById(R.id.textView24);
        Text25 = (TextView)findViewById(R.id.textView25);
        Text26 = (TextView)findViewById(R.id.textView26);
        Text27 = (TextView)findViewById(R.id.textView27);
        Text29 = (TextView)findViewById(R.id.textView29);
        Text30 = (TextView)findViewById(R.id.textView30);
        Text31 = (TextView)findViewById(R.id.textView31);
        Text32 = (TextView)findViewById(R.id.textView32);
        Text33 = (TextView)findViewById(R.id.textView33);
        Text34 = (TextView)findViewById(R.id.textView34);
        Text35 = (TextView)findViewById(R.id.textView35);
        Text36 = (TextView)findViewById(R.id.textView36);
        Text37 = (TextView)findViewById(R.id.textView37);
        Text38 = (TextView)findViewById(R.id.textView38);
        Text39 = (TextView)findViewById(R.id.textView39);
        Text40 = (TextView)findViewById(R.id.textView40);
        Text41 = (TextView)findViewById(R.id.textView41);
        Text42 = (TextView)findViewById(R.id.textView42);
        Text43 = (TextView)findViewById(R.id.textView43);
        Text44 = (TextView)findViewById(R.id.textView44);
        Text45 = (TextView)findViewById(R.id.textView45);
        Text47 = (TextView)findViewById(R.id.textView47);
        Text49 = (TextView)findViewById(R.id.textView49);
        Text51 = (TextView)findViewById(R.id.textView51);
        Text52 = (TextView)findViewById(R.id.textView52);
        Text54 = (TextView)findViewById(R.id.textView54);
        Text55 = (TextView)findViewById(R.id.textView55);
        Text56 = (TextView)findViewById(R.id.textView56);
        Text57 = (TextView)findViewById(R.id.textView57);
        Text58 = (TextView)findViewById(R.id.textView58);



        DrawFluidButton = (Button)findViewById(R.id.pumpinbutton);
        ReturnFluidButton = (Button)findViewById(R.id.pumpoutbutton);


        SaveButton.setEnabled(false);
        BeginButton.setEnabled(false);
        DrawFluidButton.setEnabled(false);
        ReturnFluidButton.setEnabled(false);
        StopButton.setEnabled(false);
        SaveDialogFileName = new EditText(getApplicationContext());
        SaveDialogFileName.setTextColor(Color.BLACK);

        myDeviceGatt = null;

        accumulatedtime = 0;
        stopwatchgoing = 0;

        redValue1 = redValue2 = redValue3 = redValue4 = redValue5 = redValue6 = redValue7 = redValue8 = redValue9 = redValue10 = redValue11 = redValue12 = 0.0;
        greenValue1 = greenValue2 = greenValue3 = greenValue4 = greenValue5 = greenValue6 = greenValue7 = greenValue8 = greenValue9 = greenValue10 = greenValue11 = greenValue12 = 0.0;
        blueValue1 = blueValue2 = blueValue3 = blueValue4 = blueValue5 = blueValue6 = blueValue7 = blueValue8 = blueValue9 = blueValue10 = blueValue11 = blueValue12 = 0.0;

        AlertDialog.Builder SaveDialogBuilder = new AlertDialog.Builder(this);
        SaveDialogBuilder.setMessage("Enter a name for your file: ");
        SaveDialogBuilder.setTitle("Save As");
        SaveDialogBuilder.setView(SaveDialogFileName);

        SaveDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SaveFileName = "/" + SaveDialogFileName.getText().toString() + ".xls";                                                              //  This will define the name for the file that we want to save
                String FilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + SaveFileName;          //  This saves the file to the android device. This file is currently saved to the downloads directory
                FileOutputStream myOutputStream = null;
                try {
                    myOutputStream = new FileOutputStream(FilePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    RawDataWorkbook.write(myOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    myOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        SaveDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //  This method for the neutral button is not used; by default it will close the dialog automatically which is what we want to happen
            }
        });

        myDialog = SaveDialogBuilder.create();


        BluetoothManager deviceBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        deviceBluetoothAdapter = deviceBluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_REQUEST);
        }

        deviceLeScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                myDevice = result.getDevice();
                String DeviceAddressString = myDevice.getAddress();
                String[] DeviceAddressStringComponents = DeviceAddressString.split(":", 6);
                String DeviceAddressStringPart1 = DeviceAddressStringComponents[0];
                String DeviceAddressStringPart2 = DeviceAddressStringComponents[1];

                if ((DeviceAddressStringPart1.equals("20")) && (DeviceAddressStringPart2.equals("FA"))) {

                    connectToDevice();

                }

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }
        };

        myDeviceGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    gatt.discoverServices();



                }
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    myDeviceGatt.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myDeviceGatt = null;
                            if (deviceBluetoothAdapter.isEnabled()) {
                                ConnectButton.setEnabled(true);
                                BeginButton.setEnabled(false);
                                StopButton.setEnabled(false);
                                DrawFluidButton.setEnabled(false);
                                ReturnFluidButton.setEnabled(false);
                            }
                            else {
                                ConnectButton.setEnabled(false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);



                myDeviceGattService = myDeviceGatt.getService(MELODYSMART_SERVICE_UUID);
                myDeviceGattCharacteristic = myDeviceGattService.getCharacteristic(MELODYSMART_DATACHARACTERISTIC_UUID);
                myDeviceGattDescriptor = myDeviceGattCharacteristic.getDescriptor(MELODYSMART_DESCRIPTOR_UUID);

                myDeviceGatt.setCharacteristicNotification(myDeviceGattCharacteristic, true);
                myDeviceGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                myDeviceGatt.writeDescriptor(myDeviceGattDescriptor);

                scanHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BeginButton.setEnabled(true);
                        StopButton.setEnabled(true);
                        DrawFluidButton.setEnabled(true);
                        ReturnFluidButton.setEnabled(true);
                    }
                }, 10);


            }

            // when we receive the rgb data from the device
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                myDeviceGattReceivedCharacteristic = characteristic;
               receivedData();
            }

        };

        bluetoothButtonHandler = new Handler();
        beginHandler = new Handler();
        stopHandler = new Handler();
        scanHandler = new Handler();


        ModulusParam = 36;
        ModulusCheck = 0;
    }




    protected void onResume() {
        super.onResume();


        if (deviceBluetoothAdapter != null) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                if (deviceBluetoothAdapter.isEnabled()) {
                    ConnectButton.setEnabled(true);
                    BeginButton.setEnabled(false);
                    StopButton.setEnabled(false);
                    DrawFluidButton.setEnabled(false);
                    ReturnFluidButton.setEnabled(false);
                    if (myDeviceGatt == null) {
                        beginstop = 0;
                    }
                }
                else {
                    ConnectButton.setEnabled(false);
                    StopButton.setEnabled(false);
                    BeginButton.setEnabled(false);
                    DrawFluidButton.setEnabled(false);
                    ReturnFluidButton.setEnabled(false);
                }
            }
            else {
                ConnectButton.setEnabled(false);
                StopButton.setEnabled(false);
                BeginButton.setEnabled(false);
                DrawFluidButton.setEnabled(false);
                ReturnFluidButton.setEnabled(false);
            }
        }
        else {
            ConnectButton.setEnabled(false);
            StopButton.setEnabled(false);
            BeginButton.setEnabled(false);
            DrawFluidButton.setEnabled(false);
            ReturnFluidButton.setEnabled(false);
        }



        BluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!deviceBluetoothAdapter.isEnabled()) {

                    deviceBluetoothAdapter.enable();

                    bluetoothButtonHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                           ConnectButton.setEnabled(true);
                            BeginButton.setEnabled(false);
                            StopButton.setEnabled(false);
                            DrawFluidButton.setEnabled(false);
                            ReturnFluidButton.setEnabled(false);
                      }
                    }, 1000);
                }
                else {

                    ConnectButton.setEnabled(false);
                    BeginButton.setEnabled(false);
                    StopButton.setEnabled(false);
                    DrawFluidButton.setEnabled(false);
                    ReturnFluidButton.setEnabled(false);
                    deviceBluetoothAdapter.disable();
                }

            }
        });

        ConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevice();
                ConnectButton.setEnabled(false);
            }
        });

        BeginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginstop = 1;
                beginDataTransfer();
                BeginButton.setEnabled(false);
                StopButton.setEnabled(false);
                DrawFluidButton.setEnabled(false);
                ReturnFluidButton.setEnabled(false);
                beginHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BeginButton.setEnabled(true);
                        StopButton.setEnabled(true);
                        DrawFluidButton.setEnabled(true);
                        ReturnFluidButton.setEnabled(true);
                    }
                }, 2000);
            }
        });

        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginstop = 0;
                RedTextString = "Red: ";
                GreenTextString = "Green: ";
                BlueTextString = "Blue: ";
                RedText.setText(RedTextString);
                GreenText.setText(GreenTextString);
                BlueText.setText(BlueTextString);
             /*   ColorSurface.setBackgroundColor(-1);
                ColorSurface2.setBackgroundColor(-1);
                ColorSurface4.setBackgroundColor(-1);
                ColorSurface5.setBackgroundColor(-1);*/
                Surface1.setBackgroundColor(-1);
                Surface2.setBackgroundColor(-1);
                Surface3.setBackgroundColor(-1);
                Surface4.setBackgroundColor(-1);
                Surface5.setBackgroundColor(-1);
                Surface6.setBackgroundColor(-1);
                Surface7.setBackgroundColor(-1);
                Surface8.setBackgroundColor(-1);
                Surface9.setBackgroundColor(-1);
                Surface10.setBackgroundColor(-1);
                Surface11.setBackgroundColor(-1);
                Surface12.setBackgroundColor(-1);
                stopDataTransfer();
                BeginButton.setEnabled(false);
                StopButton.setEnabled(false);
                DrawFluidButton.setEnabled(false);
                ReturnFluidButton.setEnabled(false);
                stopHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BeginButton.setEnabled(true);
                        StopButton.setEnabled(true);
                        DrawFluidButton.setEnabled(true);
                        ReturnFluidButton.setEnabled(true);
                    }
                }, 2000);
            }
        });

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.show();
            }
        });

        DrawFluidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawFluid();
            }
        });

        ReturnFluidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnFluid();
            }
        });




    }

    public void scanForDevice() {

        deviceLeScanner = deviceBluetoothAdapter.getBluetoothLeScanner();
        deviceLeScanner.startScan(deviceLeScanCallback);

        scanHandler.postDelayed(new Runnable() {                              //  Here the handler is called to manage the time for the scan
            @Override
            public void run() {
                deviceLeScanner.stopScan(deviceLeScanCallback);                 //  At the end of the 2 second scan, stop the scan

            }
        }, 2000);

    }

    public void connectToDevice() {

        myDeviceGatt = myDevice.connectGatt(this, false, myDeviceGattCallback);

    }

    public void beginDataTransfer() {



        //Excel spreadsheet related
        RawDataWorkbook = new HSSFWorkbook();
        RawDataSheet = RawDataWorkbook.createSheet("Sheet1");
        Row TitleRow = RawDataSheet.createRow(0);
        NewRow = RawDataSheet.createRow(1);

        SaveButton.setEnabled(true);

        ModulusCheck = 0;


        TitleRow.createCell(1).setCellValue("Measurement #");

        TitleRow.createCell(2).setCellValue("Red 1");
        TitleRow.createCell(3).setCellValue("Green 1");
        TitleRow.createCell(4).setCellValue("Blue 1");

        TitleRow.createCell(6).setCellValue("Red 2");
        TitleRow.createCell(7).setCellValue("Green 2");
        TitleRow.createCell(8).setCellValue("Blue 2");

        TitleRow.createCell(10).setCellValue("Red 3");
        TitleRow.createCell(11).setCellValue("Green 3");
        TitleRow.createCell(12).setCellValue("Blue 3");

        TitleRow.createCell(14).setCellValue("Red 4");
        TitleRow.createCell(15).setCellValue("Green 4");
        TitleRow.createCell(16).setCellValue("Blue 4");

        TitleRow.createCell(18).setCellValue("Red 5");
        TitleRow.createCell(19).setCellValue("Green 5");
        TitleRow.createCell(20).setCellValue("Blue 5");

        TitleRow.createCell(22).setCellValue("Red 6");
        TitleRow.createCell(23).setCellValue("Green 6");
        TitleRow.createCell(24).setCellValue("Blue 6");

        TitleRow.createCell(26).setCellValue("Red 7");
        TitleRow.createCell(27).setCellValue("Green 7");
        TitleRow.createCell(28).setCellValue("Blue 7");

        TitleRow.createCell(30).setCellValue("Red 8");
        TitleRow.createCell(31).setCellValue("Green 8");
        TitleRow.createCell(32).setCellValue("Blue 8");

        TitleRow.createCell(34).setCellValue("Red 9");
        TitleRow.createCell(35).setCellValue("Green 9");
        TitleRow.createCell(36).setCellValue("Blue 9");

        TitleRow.createCell(38).setCellValue("Red 10");
        TitleRow.createCell(39).setCellValue("Green 10");
        TitleRow.createCell(40).setCellValue("Blue 10");

        TitleRow.createCell(42).setCellValue("Red 11");
        TitleRow.createCell(43).setCellValue("Green 11");
        TitleRow.createCell(44).setCellValue("Blue 11");

        TitleRow.createCell(46).setCellValue("Red 12");
        TitleRow.createCell(47).setCellValue("Green 12");
        TitleRow.createCell(48).setCellValue("Blue 12");


        myDeviceGattCharacteristic.setValue("B0000000000000");

        myDeviceGatt.writeCharacteristic(myDeviceGattCharacteristic);


    }

    public void stopDataTransfer() {
        myDeviceGattCharacteristic.setValue("S0000000000000");
        myDeviceGatt.writeCharacteristic(myDeviceGattCharacteristic);
    }

    //################################################################################################################
    public void receivedData() {

        if (beginstop == 1) {

            byte[] dataCharacteristicByte = myDeviceGattCharacteristic.getValue();
            String newstring = new String(dataCharacteristicByte);
            Log.i("Raw Data: ", newstring);

            if (ModulusCheck != 0) {
                if ((ModulusCheck % ModulusParam) == 0) {
                    Log.d("am i null?", NewRow.toString());
                    NewRow = RawDataSheet.createRow((ModulusCheck / ModulusParam) + 1);
                }
            }

            if (dataCharacteristicByte[0] == ASCII_R) {

                byte[] dataByteValue = new byte[dataCharacteristicByte.length - 3];
                System.arraycopy(dataCharacteristicByte, 3, dataByteValue, 0, dataByteValue.length);
                String dataString = new String(dataByteValue);

                switch (dataCharacteristicByte[1]) {
                    case ASCII_ONE:
                        redValue1 = Double.parseDouble(dataString) * 5.1813;
                        if (redValue1 > 8388608) {
                            redValue1 = 8388608.0;
                        }
                        break;
                    case ASCII_TWO:
                        redValue2 = Double.parseDouble(dataString) * 5.182;
                        if (redValue2 > 8388608) {
                            redValue2 = 8388608.0;
                        }
                        break;
                    case ASCII_THREE:
                        redValue3 = Double.parseDouble(dataString) * 5.178;
                        if (redValue3 > 8388608) {
                            redValue3 = 8388608.0;
                        }
                        break;
                    case ASCII_FOUR:
                        redValue4 = Double.parseDouble(dataString) * 5.181;
                        if (redValue4 > 8388608) {
                            redValue4 = 8388608.0;
                        }
                        break;
                    case ASCII_FIVE:
                        redValue5 = Double.parseDouble(dataString) * 4.49;
                        if (redValue5 > 8388608) {
                            redValue5 = 8388608.0;
                        }
                        break;
                    case ASCII_SIX:
                        redValue6 = Double.parseDouble(dataString) * 4.49;
                        if (redValue6 > 8388608) {
                            redValue6 = 8388608.0;
                        }
                        break;
                    case ASCII_SEVEN:
                        redValue7 = Double.parseDouble(dataString) * 4.484;
                        if (redValue7 > 8388608) {
                            redValue7 = 8388608.0;
                        }
                        break;
                    case ASCII_EIGHT:
                        redValue8 = Double.parseDouble(dataString) * 4.487;
                        if (redValue8 > 8388608) {
                            redValue8 = 8388608.0;
                        }
                        break;
                    case ASCII_NINE:
                        redValue9 = Double.parseDouble(dataString) * 4.66;
                        if (redValue9 > 8388608) {
                            redValue9 = 8388608.0;
                        }
                        break;
                    case ASCII_TEN:
                        redValue10 = Double.parseDouble(dataString) * 4.66;
                        if (redValue10 > 8388608) {
                            redValue10 = 8388608.0;
                        }
                        break;
                    case ASCII_ELEVEN:
                        redValue11 = Double.parseDouble(dataString) * 4.167;
                        if (redValue11 > 8388608) {
                            redValue11 = 8388608.0;
                        }
                        break;
                    case ASCII_TWELVE:
                        redValue12 = Double.parseDouble(dataString) * 4.657;
                        if (redValue12 > 8388608) {
                            redValue12 = 8388608.0;
                        }
                        break;
                    default:
                        break;

                }

            }


            else if (dataCharacteristicByte[0] == ASCII_G) {

                byte[] dataByteValue = new byte[dataCharacteristicByte.length - 3];
                System.arraycopy(dataCharacteristicByte, 3, dataByteValue, 0, dataByteValue.length);
                String dataString = new String(dataByteValue);

                switch (dataCharacteristicByte[1]) {
                    case ASCII_ONE:
                        greenValue1 = Double.parseDouble(dataString) * 1.722;
                        if (greenValue1 > 8388608) {
                            greenValue1 = 8388608.0;
                        }
                        break;
                    case ASCII_TWO:
                        greenValue2 = Double.parseDouble(dataString) * 1.75;
                        if (greenValue2 > 8388608) {
                            greenValue2 = 8388608.0;
                        }
                        break;
                    case ASCII_THREE:
                        greenValue3 = Double.parseDouble(dataString) * 1.544;
                        if (greenValue3 > 8388608) {
                            greenValue3 = 8388608.0;
                        }
                        break;
                    case ASCII_FOUR:
                        greenValue4 = Double.parseDouble(dataString) * 1.484;
                        if (greenValue4 > 8388608) {
                            greenValue4 = 8388608.0;
                        }
                        break;
                    case ASCII_FIVE:
                        greenValue5 = Double.parseDouble(dataString) * 1.413;
                        if (greenValue5 > 8388608) {
                            greenValue5 = 8388608.0;
                        }
                        break;
                    case ASCII_SIX:
                        greenValue6 = Double.parseDouble(dataString) * 1.464;
                        if (greenValue6 > 8388608) {
                            greenValue6 = 8388608.0;
                        }
                        break;
                    case ASCII_SEVEN:
                        greenValue7 = Double.parseDouble(dataString) * 1.471;
                        if (greenValue7 > 8388608) {
                            greenValue7 = 8388608.0;
                        }
                        break;
                    case ASCII_EIGHT:
                        greenValue8 = Double.parseDouble(dataString) * 1.476;
                        if (greenValue8 > 8388608) {
                            greenValue8 = 8388608.0;
                        }
                        break;
                    case ASCII_NINE:
                        greenValue9 = Double.parseDouble(dataString) * 1.596;
                        if (greenValue9 > 8388608) {
                            greenValue9 = 8388608.0;
                        }
                        break;
                    case ASCII_TEN:
                        greenValue10 = Double.parseDouble(dataString) * 1.453;
                        if (greenValue10 > 8388608) {
                            greenValue10 = 8388608.0;
                        }
                        break;
                    case ASCII_ELEVEN:
                        greenValue11 = Double.parseDouble(dataString) * 1.534;
                        if (greenValue11 > 8388608) {
                            greenValue11 = 8388608.0;
                        }
                        break;
                    case ASCII_TWELVE:
                        greenValue12 = Double.parseDouble(dataString) * 1.346;
                        if (greenValue12 > 8388608) {
                            greenValue12 = 8388608.0;
                        }
                        break;
                    default:
                        break;

                }

            }

            else if (dataCharacteristicByte[0] == ASCII_B) {

                byte[] dataByteValue = new byte[dataCharacteristicByte.length - 3];
                System.arraycopy(dataCharacteristicByte, 3, dataByteValue, 0, dataByteValue.length);
                String dataString = new String(dataByteValue);

                switch (dataCharacteristicByte[1]) {
                    case ASCII_ONE:
                        blueValue1 = Double.parseDouble(dataString) * 1.703;
                        if (blueValue1 > 8388608) {
                            blueValue1 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color1 = (0xff) << 24 | ((int)(redValue1 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue1 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue1 / 8388608 * 255) & 0xff);
                                Surface1.setBackgroundColor(Color1);
                                Text17.setText("R: " + redValue1.intValue());
                                Text18.setText("G: " + greenValue1.intValue());
                                Text19.setText("B: " + blueValue1.intValue());
                            }
                        });
                        break;
                    case ASCII_TWO:
                        blueValue2 = Double.parseDouble(dataString) * 5.181;
                        if (blueValue2 > 8388608) {
                            blueValue2 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {                                                                       // bluevalue1 since sensor 2 has hardware issues but we didn't have available blue photodiodes at the time
                                int Color2 = (0xff) << 24 | ((int)(redValue2 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue2 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue1 / 8388608 * 255) & 0xff);
                                Surface2.setBackgroundColor(Color2);
                                Text20.setText("R: " + redValue2.intValue());
                                Text21.setText("G: " + greenValue2.intValue());
                                Text22.setText("B: " + blueValue2.intValue());
                            }
                        });
                        break;
                    case ASCII_THREE:
                        blueValue3 = Double.parseDouble(dataString) * 2.115;
                        if (blueValue3 > 8388608) {
                            blueValue3 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color3 = (0xff) << 24 | ((int)(redValue3 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue3 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue3 / 8388608 * 255) & 0xff);
                                Surface3.setBackgroundColor(Color3);
                                Text24.setText("R: " + redValue3.intValue());
                                Text25.setText("G: " + greenValue3.intValue());
                                Text26.setText("B: " + blueValue3.intValue());
                            }
                        });
                        break;
                    case ASCII_FOUR:
                        blueValue4 = Double.parseDouble(dataString) * 2.236;
                        if (blueValue4 > 8388608) {
                            blueValue4 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color4 = (0xff) << 24 | ((int)(redValue4 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue4 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue4 / 8388608 * 255) & 0xff);
                                Surface4.setBackgroundColor(Color4);
                                Text27.setText("R: " + redValue4.intValue());
                                Text29.setText("G: " + greenValue4.intValue());
                                Text30.setText("B: " + blueValue4.intValue());
                            }
                        });
                        break;
                    case ASCII_FIVE:
                        blueValue5 = Double.parseDouble(dataString) * 2.072;
                        if (blueValue5 > 8388608) {
                            blueValue5 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color5 = (0xff) << 24 | ((int)(redValue5 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue5 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue5 / 8388608 * 255) & 0xff);
                                Surface5.setBackgroundColor(Color5);
                                Text31.setText("R: " + redValue5.intValue());
                                Text32.setText("G: " + greenValue5.intValue());
                                Text33.setText("B: " + blueValue5.intValue());
                            }
                        });
                        break;
                    case ASCII_SIX:
                        blueValue6 = Double.parseDouble(dataString) * 1.143;
                        if (blueValue6 > 8388608) {
                            blueValue6 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color6 = (0xff) << 24 | ((int)(redValue6 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue6 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue6 / 8388608 * 255) & 0xff);
                                Surface6.setBackgroundColor(Color6);
                                Text34.setText("R: " + redValue6.intValue());
                                Text35.setText("G: " + greenValue6.intValue());
                                Text36.setText("B: " + blueValue6.intValue());
                            }
                        });
                        break;
                    case ASCII_SEVEN:
                        blueValue7 = Double.parseDouble(dataString) * 1.97;
                        if (blueValue7 > 8388608) {
                            blueValue7 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color7 = (0xff) << 24 | ((int)(redValue7 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue7 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue7 / 8388608 * 255) & 0xff);
                                Surface7.setBackgroundColor(Color7);
                                Text37.setText("R: " + redValue7.intValue());
                                Text38.setText("G: " + greenValue7.intValue());
                                Text39.setText("B: " + blueValue7.intValue());
                            }
                        });
                        break;
                    case ASCII_EIGHT:
                        blueValue8 = Double.parseDouble(dataString) * 1.962;
                        if (blueValue8 > 8388608) {
                            blueValue8 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color8 = (0xff) << 24 | ((int)(redValue8 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue8 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue8 / 8388608 * 255) & 0xff);
                                Surface8.setBackgroundColor(Color8);
                                Text40.setText("R: " + redValue8.intValue());
                                Text41.setText("G: " + greenValue8.intValue());
                                Text42.setText("B: " + blueValue8.intValue());
                            }
                        });
                        break;
                    case ASCII_NINE:
                        blueValue9 = Double.parseDouble(dataString) * 2.183;
                        if (blueValue9 > 8388608) {
                            blueValue9 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color9 = (0xff) << 24 | ((int)(redValue9 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue9 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue9 / 8388608 * 255) & 0xff);
                                Surface9.setBackgroundColor(Color9);
                                Text43.setText("R: " + redValue9.intValue());
                                Text44.setText("G: " + greenValue9.intValue());
                                Text45.setText("B: " + blueValue9.intValue());
                            }
                        });
                        break;
                    case ASCII_TEN:
                        blueValue10 = Double.parseDouble(dataString) * 1.991;
                        if (blueValue10 > 8388608) {
                            blueValue10 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color10 = (0xff) << 24 | ((int)(redValue10 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue10 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue10 / 8388608 * 255) & 0xff);
                                Surface10.setBackgroundColor(Color10);
                                Text47.setText("R: " + redValue10.intValue());
                                Text49.setText("G: " + greenValue10.intValue());
                                Text51.setText("B: " + blueValue10.intValue());
                            }
                        });
                        break;
                    case ASCII_ELEVEN:
                        blueValue11 = Double.parseDouble(dataString) * 2.352;
                        if (blueValue11 > 8388608) {
                            blueValue11 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color11 = (0xff) << 24 | ((int)(redValue11 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue11 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue11 / 8388608 * 255) & 0xff);
                                Surface11.setBackgroundColor(Color11);
                                Text52.setText("R: " + redValue11.intValue());
                                Text54.setText("G: " + greenValue11.intValue());
                                Text55.setText("B: " + blueValue11.intValue());
                            }
                        });
                        break;
                    case ASCII_TWELVE:
                        blueValue12 = Double.parseDouble(dataString) * 1.731;
                        if (blueValue12 > 8388608) {
                            blueValue12 = 8388608.0;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int Color12 = (0xff) << 24 | ((int)(redValue12 / 8388608 * 255) & 0xff) << 16 | ((int)(greenValue12 / 8388608 * 255) & 0xff) << 8 | ((int)(blueValue12 / 8388608 * 255) & 0xff);
                                Surface12.setBackgroundColor(Color12);
                                Text56.setText("R: " + redValue12.intValue());
                                Text57.setText("G: " + greenValue12.intValue());
                                Text58.setText("B: " + blueValue12.intValue());
                            }
                        });
                        break;
                    default:
                        break;

                }

                if (beginstop == 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            /*
                            int mycolor = (0xff) << 24 | (redValue.intValue() & 0xff) << 16 | (greenValue.intValue() & 0xff) << 8 | (blueValue.intValue() & 0xff);
                            int mycolorred = ((0xff) << 24 | (redValue.intValue() & 0xff) << 16 | (0x00) << 8 | (0x00));
                            int mycolorgreen = ((0xff) << 24 | (0x00) << 16 | (greenValue.intValue() & 0xff) << 8 | (0x00));
                            int mycolorblue = ((0xff) << 24 | (0x00) << 16 | (0x00) << 8 | (blueValue.intValue() & 0xff));

                            ColorSurface.setBackgroundColor(mycolor);
                            ColorSurface2.setBackgroundColor(mycolorred);
                            ColorSurface4.setBackgroundColor(mycolorgreen);
                            ColorSurface5.setBackgroundColor(mycolorblue);*/

                            NewRow.createCell(1).setCellValue(0);

                            NewRow.createCell(2).setCellValue(redValue1 / 8388608 * 255);
                            ModulusCheck = ModulusCheck + 1;

                            NewRow.createCell(3).setCellValue(greenValue1 / 8388608 * 255);
                            ModulusCheck = ModulusCheck + 1;

                            NewRow.createCell(4).setCellValue(blueValue1 / 8388608 * 255);
                            ModulusCheck = ModulusCheck + 1;

                            NewRow.createCell(6).setCellValue(redValue2 / 8388608 * 255);


                            NewRow.createCell(7).setCellValue(greenValue2 / 8388608 * 255);


                            NewRow.createCell(8).setCellValue(blueValue2 / 8388608 * 255);


                            NewRow.createCell(10).setCellValue(redValue3 / 8388608 * 255);


                            NewRow.createCell(11).setCellValue(greenValue3 / 8388608 * 255);


                            NewRow.createCell(12).setCellValue(blueValue3 / 8388608 * 255);


                            NewRow.createCell(14).setCellValue(redValue4 / 8388608 * 255);


                            NewRow.createCell(15).setCellValue(greenValue4 / 8388608 * 255);


                            NewRow.createCell(16).setCellValue(blueValue4 / 8388608 * 255);


                            NewRow.createCell(18).setCellValue(redValue5 / 8388608 * 255);


                            NewRow.createCell(19).setCellValue(greenValue5 / 8388608 * 255);


                            NewRow.createCell(20).setCellValue(blueValue5 / 8388608 * 255);


                            NewRow.createCell(22).setCellValue(redValue6 / 8388608 * 255);


                            NewRow.createCell(23).setCellValue(greenValue6 / 8388608 * 255);


                            NewRow.createCell(24).setCellValue(blueValue6 / 8388608 * 255);


                            NewRow.createCell(26).setCellValue(redValue7 / 8388608 * 255);


                            NewRow.createCell(27).setCellValue(greenValue7 / 8388608 * 255);


                            NewRow.createCell(28).setCellValue(blueValue7 / 8388608 * 255);


                            NewRow.createCell(30).setCellValue(redValue8 / 8388608 * 255);


                            NewRow.createCell(31).setCellValue(greenValue8 / 8388608 * 255);


                            NewRow.createCell(32).setCellValue(blueValue8 / 8388608 * 255);


                            NewRow.createCell(34).setCellValue(redValue9 / 8388608 * 255);


                            NewRow.createCell(35).setCellValue(greenValue9 / 8388608 * 255);


                            NewRow.createCell(36).setCellValue(blueValue9 / 8388608 * 255);


                            NewRow.createCell(38).setCellValue(redValue10 / 8388608 * 255);


                            NewRow.createCell(39).setCellValue(greenValue10 / 8388608 * 255);


                            NewRow.createCell(40).setCellValue(blueValue10 / 8388608 * 255);


                            NewRow.createCell(42).setCellValue(redValue11 / 8388608 * 255);


                            NewRow.createCell(43).setCellValue(greenValue11 / 8388608 * 255);


                            NewRow.createCell(44).setCellValue(blueValue11 / 8388608 * 255);


                            NewRow.createCell(46).setCellValue(redValue12 / 8388608 * 255);


                            NewRow.createCell(47).setCellValue(greenValue12 / 8388608 * 255);


                            NewRow.createCell(48).setCellValue(blueValue12 / 8388608 * 255);


                        }
                    });
                } else if (beginstop == 0) {
                    stopDataTransfer();
                }
            }
        }
        else if (beginstop == 0) {
            stopDataTransfer();
        }

    }

    public void DrawFluid() {
        myDeviceGattCharacteristic.setValue("M0000000000000");
        myDeviceGatt.writeCharacteristic(myDeviceGattCharacteristic);
    }

    public void ReturnFluid() {
        myDeviceGattCharacteristic.setValue("O0000000000000");
        myDeviceGatt.writeCharacteristic(myDeviceGattCharacteristic);
    }
}


