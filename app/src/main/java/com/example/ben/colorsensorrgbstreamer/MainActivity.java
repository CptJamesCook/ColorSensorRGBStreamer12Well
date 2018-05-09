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
import java.io.FileOutputStream;
import java.util.HashMap;
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

    private HashMap<Byte, Integer> AsciiToIndex = new HashMap<Byte, Integer>();
    private HashMap<Byte, String> AsciiToColor = new HashMap<Byte, String>();

    //TODO: Make function that finds and sets these values
    private Double[] NORMALIZATIONRED = {370595.0, 370727.0, 370496.0, 370259.0, 426815.0,
                                         426650.0, 427327.0, 426756.0, 410367.0, 410518.0,
                                         405463.0, 406228.0};
    private Double[] NORMALIZATIONGREEN = {739927.0, 766746.0, 905849.0, 1064222.0, 1169129.0,
                                           1030470.0, 1134874.0, 1166203.0, 1090231.0, 1087279.0,
                                           1099069.0, 1127967.0};
    private Double[] NORMALIZATIONBLUE = {938894.0, 370674.0, 757476.0, 694020.0, 746368.0,
                                          1394877.0, 906198.0, 928066.0, 844389.0, 864055.0,
                                          888384.0, 947814.0};

    private Sensor[] sensors = new Sensor[12];

    private int beginstop = 0;

    private static final int ACCESS_COARSE_LOCATION_REQUEST = 1;

    private Button BluetoothButton, ConnectButton, BeginButton, StopButton, SaveButton,
                   DrawFluidButton, ReturnFluidButton, CalibrateButton;
    private TextView RedText, GreenText, BlueText;
    private TextView DataText, ColorText;
    private TextView Text1, Text2, Text3, Text4, Text5, Text6, Text7, Text8, Text9, Text10,
                     Text11, Text12;
    private TextView Text17, Text18, Text19, Text20, Text21, Text22, Text24, Text25, Text26,
                     Text27, Text29, Text30, Text31, Text32, Text33, Text34, Text35, Text36,
                     Text37, Text38, Text39, Text40, Text41, Text42, Text43, Text44, Text45,
                     Text47, Text49, Text51, Text52, Text54, Text55, Text56, Text57, Text58;
    private SurfaceView Surface1, Surface2, Surface3, Surface4, Surface5, Surface6, Surface7,
                        Surface8, Surface9, Surface10, Surface11, Surface12;

    private String RedTextString, GreenTextString, BlueTextString;
    private BluetoothAdapter deviceBluetoothAdapter;
    private BluetoothLeScanner deviceLeScanner;
    private ScanCallback deviceLeScanCallback;
    private Handler bluetoothButtonHandler, scanHandler, beginHandler, stopHandler;
    private BluetoothDevice myDevice;
    private BluetoothGattCallback myDeviceGattCallback;
    private BluetoothGatt myDeviceGatt;
    private BluetoothGattService myDeviceGattService;
    private BluetoothGattCharacteristic myDeviceGattCharacteristic;

    private BluetoothGattDescriptor myDeviceGattDescriptor;

    private EditText SaveDialogFileName;
    private String SaveFileName;
    private AlertDialog myDialog;

    //Used in receive function. Made global so it can run on UI thread
    private Double colorNum;
    private int index;
    private String colorStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeSensors();

        SaveButton.setEnabled(false);
        StopButton.setEnabled(false);
        BeginButton.setEnabled(false);
        DrawFluidButton.setEnabled(false);
        ReturnFluidButton.setEnabled(false);
        CalibrateButton.setEnabled(false);
        SaveDialogFileName = new EditText(getApplicationContext());
        SaveDialogFileName.setTextColor(Color.BLACK);

        myDeviceGatt = null;

        AsciiToIndex.put(ASCII_ONE, 0);
        AsciiToIndex.put(ASCII_TWO, 1);
        AsciiToIndex.put(ASCII_THREE, 2);
        AsciiToIndex.put(ASCII_FOUR, 3);
        AsciiToIndex.put(ASCII_FIVE, 4);
        AsciiToIndex.put(ASCII_SIX, 5);
        AsciiToIndex.put(ASCII_SEVEN, 6);
        AsciiToIndex.put(ASCII_EIGHT, 7);
        AsciiToIndex.put(ASCII_NINE, 8);
        AsciiToIndex.put(ASCII_TEN, 9);
        AsciiToIndex.put(ASCII_ELEVEN, 10);
        AsciiToIndex.put(ASCII_TWELVE, 11);

        AsciiToColor.put(ASCII_R, "red");
        AsciiToColor.put(ASCII_G, "green");
        AsciiToColor.put(ASCII_B, "blue");

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
                //TODO: Implement saving results
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
                                CalibrateButton.setEnabled(false);
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
                        CalibrateButton.setEnabled(true);
                    }
                }, 10);
            }

            // when we receive the rgb data from the device
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
               receivedData();
            }
        };

        bluetoothButtonHandler = new Handler();
        beginHandler = new Handler();
        stopHandler = new Handler();
        scanHandler = new Handler();
    }

    protected void onResume() {
        super.onResume();
        if ((deviceBluetoothAdapter != null)
                && (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            ConnectButton.setEnabled(true);
            BeginButton.setEnabled(false);
            StopButton.setEnabled(false);
            DrawFluidButton.setEnabled(false);
            ReturnFluidButton.setEnabled(false);
            CalibrateButton.setEnabled(false);
            if (myDeviceGatt == null) {
                beginstop = 0;
            }
            else {
                ConnectButton.setEnabled(false);
                StopButton.setEnabled(false);
                BeginButton.setEnabled(false);
                DrawFluidButton.setEnabled(false);
                ReturnFluidButton.setEnabled(false);
                CalibrateButton.setEnabled(false);
            }
        }
        else {
            ConnectButton.setEnabled(false);
            StopButton.setEnabled(false);
            BeginButton.setEnabled(false);
            DrawFluidButton.setEnabled(false);
            ReturnFluidButton.setEnabled(false);
            CalibrateButton.setEnabled(false);
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
                            CalibrateButton.setEnabled(false);
                      }
                    }, 1000);
                }
                else {
                    ConnectButton.setEnabled(false);
                    BeginButton.setEnabled(false);
                    StopButton.setEnabled(false);
                    DrawFluidButton.setEnabled(false);
                    ReturnFluidButton.setEnabled(false);
                    CalibrateButton.setEnabled(false);
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
                CalibrateButton.setEnabled(false);
                beginHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BeginButton.setEnabled(true);
                        StopButton.setEnabled(true);
                        DrawFluidButton.setEnabled(true);
                        ReturnFluidButton.setEnabled(true);
                        CalibrateButton.setEnabled(true);
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

                runOnUiThread(new Runnable() {
                    public void run() {
                        for(Sensor sensor: sensors){
                            sensor.setSurfaceColor(Color.WHITE);
                        }
                    }
                });

                stopDataTransfer();

                BeginButton.setEnabled(false);
                StopButton.setEnabled(false);
                DrawFluidButton.setEnabled(false);
                ReturnFluidButton.setEnabled(false);
                CalibrateButton.setEnabled(false);
                stopHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BeginButton.setEnabled(true);
                        StopButton.setEnabled(true);
                        DrawFluidButton.setEnabled(true);
                        ReturnFluidButton.setEnabled(true);
                        CalibrateButton.setEnabled(true);
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

        CalibrateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CalibrateSensors();
            }
        });
    }

    private void initializeViews(){
        BluetoothButton = (Button)findViewById(R.id.bluetoothbutton);
        ConnectButton = (Button)findViewById(R.id.connectbutton);
        BeginButton = (Button)findViewById(R.id.beginbutton);
        StopButton = (Button)findViewById(R.id.stopbutton);
        SaveButton = (Button)findViewById(R.id.savebutton);
        RedText = (TextView) findViewById(R.id.redtext);
        GreenText = (TextView)findViewById(R.id.greentext);
        BlueText = (TextView)findViewById(R.id.bluetext);
        DataText = (TextView)findViewById(R.id.textView4);
        ColorText = (TextView)findViewById(R.id.textView5);

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
        CalibrateButton = (Button)findViewById(R.id.calibratebutton);
    }

    public void initializeSensors(){
        for(Integer i=0; i<12; i++){
            sensors[i] = new Sensor();
            sensors[i].setName("Sensor " + i.toString());
        }
        sensors[0].setRedTextView(Text17);
        sensors[0].setBlueTextView(Text18);
        sensors[0].setGreenTextView(Text19);
        sensors[0].setSurface(Surface1);
        sensors[0].setSurfaceColor(Color.WHITE);

        sensors[1].setRedTextView(Text20);
        sensors[1].setBlueTextView(Text21);
        sensors[1].setGreenTextView(Text22);
        sensors[1].setSurface(Surface2);
        sensors[1].setSurfaceColor(Color.WHITE);

        sensors[2].setRedTextView(Text24);
        sensors[2].setBlueTextView(Text25);
        sensors[2].setGreenTextView(Text26);
        sensors[2].setSurface(Surface3);
        sensors[2].setSurfaceColor(Color.WHITE);

        sensors[3].setRedTextView(Text27);
        sensors[3].setBlueTextView(Text29);
        sensors[3].setGreenTextView(Text30);
        sensors[3].setSurface(Surface4);
        sensors[3].setSurfaceColor(Color.WHITE);

        sensors[4].setRedTextView(Text31);
        sensors[4].setBlueTextView(Text32);
        sensors[4].setGreenTextView(Text33);
        sensors[4].setSurface(Surface5);
        sensors[4].setSurfaceColor(Color.WHITE);

        sensors[5].setRedTextView(Text34);
        sensors[5].setBlueTextView(Text35);
        sensors[5].setGreenTextView(Text36);
        sensors[5].setSurface(Surface6);
        sensors[5].setSurfaceColor(Color.WHITE);

        sensors[6].setRedTextView(Text37);
        sensors[6].setBlueTextView(Text38);
        sensors[6].setGreenTextView(Text39);
        sensors[6].setSurface(Surface7);
        sensors[6].setSurfaceColor(Color.WHITE);

        sensors[7].setRedTextView(Text40);
        sensors[7].setBlueTextView(Text41);
        sensors[7].setGreenTextView(Text42);
        sensors[7].setSurface(Surface8);
        sensors[7].setSurfaceColor(Color.WHITE);

        sensors[8].setRedTextView(Text43);
        sensors[8].setBlueTextView(Text44);
        sensors[8].setGreenTextView(Text45);
        sensors[8].setSurface(Surface9);
        sensors[8].setSurfaceColor(Color.WHITE);

        sensors[9].setRedTextView(Text47);
        sensors[9].setBlueTextView(Text49);
        sensors[9].setGreenTextView(Text51);
        sensors[9].setSurface(Surface10);
        sensors[9].setSurfaceColor(Color.WHITE);

        sensors[10].setRedTextView(Text52);
        sensors[10].setBlueTextView(Text54);
        sensors[10].setGreenTextView(Text55);
        sensors[10].setSurface(Surface11);
        sensors[10].setSurfaceColor(Color.WHITE);

        sensors[11].setRedTextView(Text56);
        sensors[11].setBlueTextView(Text57);
        sensors[11].setGreenTextView(Text58);
        sensors[11].setSurface(Surface12);
        sensors[11].setSurfaceColor(Color.WHITE);
    }

    public void scanForDevice() {
        deviceLeScanner = deviceBluetoothAdapter.getBluetoothLeScanner();
        deviceLeScanner.startScan(deviceLeScanCallback);
        scanHandler.postDelayed(new Runnable() {                              //  Here the handler is called to manage the time for the scan
            @Override
            public void run() {
                deviceLeScanner.stopScan(deviceLeScanCallback);//  At the end of the 2 second scan, stop the scan
            }
        }, 2000);

    }

    public void connectToDevice() {
        myDeviceGatt = myDevice.connectGatt(this, false, myDeviceGattCallback);
    }

    public void beginDataTransfer() {
        SaveButton.setEnabled(true);
        myDeviceGattCharacteristic.setValue("B0000000000000");
        myDeviceGatt.writeCharacteristic(myDeviceGattCharacteristic);
    }

    public void stopDataTransfer() {
        myDeviceGattCharacteristic.setValue("S0000000000000");
        myDeviceGatt.writeCharacteristic(myDeviceGattCharacteristic);
    }

    public void receivedData() {

        if (beginstop == 1) {

            byte[] dataCharacteristicByte = myDeviceGattCharacteristic.getValue();
            String newstring = new String(dataCharacteristicByte);
            Log.i("Raw Data: ", newstring);

            byte[] dataByteValue = new byte[dataCharacteristicByte.length - 3];
            System.arraycopy(dataCharacteristicByte, 3, dataByteValue, 0, dataByteValue.length);
            String dataString = new String(dataByteValue);

            colorNum = Double.parseDouble(dataString);

            index = AsciiToIndex.get(dataCharacteristicByte[1]);
            colorStr = AsciiToColor.get(dataCharacteristicByte[0]);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sensors[index].computeAndSetSurfaceColor(colorStr, colorNum);
                }
            });
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

    public void CalibrateSensors(){
        for(Sensor sensor: sensors){
            sensor.calibrate();
        }
    }
}
