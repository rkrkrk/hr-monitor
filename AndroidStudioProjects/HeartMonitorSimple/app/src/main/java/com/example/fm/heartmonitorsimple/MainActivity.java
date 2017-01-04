package com.example.fm.heartmonitorsimple;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private BluetoothAdapter btAdapter;
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 1;
    private ListView lv;
    ArrayList deviceList = new ArrayList();
    ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    ArrayAdapter adapter;
    Handler mHandler;
    boolean mScanning;
    BluetoothGatt bluetoothGatt;
    TextView tHRvalueHR, tHRvalueDate, tHRdata;
    LinearLayout hrView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        hrView = (LinearLayout) findViewById(R.id.lLayoutHR);
//        hrView.setVisibility(View.GONE);

        tHRvalueHR = (TextView) findViewById(R.id.tCurrentHR);
        tHRvalueDate = (TextView) findViewById(R.id.tCurrentDate);
        tHRdata = (TextView) findViewById(R.id.tData);

        lv = (ListView)findViewById(R.id.lvDevices);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, deviceList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                if (bluetoothGatt == null) {
                    bluetoothGatt = devices.get(position).connectGatt(getApplicationContext(), false, btleGattCallback);
                } else {
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                    bluetoothGatt = devices.get(position).connectGatt(getApplicationContext(), false, btleGattCallback);
                }
                Log.i("REWWRE item", " "+position);
                Log.i("REWWRE item name", " "+devices.get(position).getName());
            }
        });
        mHandler = new Handler();

//        // set up adapter and check bluetooth is on
//        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
//        btAdapter = btManager.getAdapter();
//        if (btAdapter == null || !btAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, 1);
//        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                //discovery starts, we can show progress dialog or perform other tasks
//                Toast.makeText(getApplicationContext(), "Discovery Started",Toast.LENGTH_SHORT).show();
//
//            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                Toast.makeText(getApplicationContext(), "Discovery Ended",Toast.LENGTH_SHORT).show();
//            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                //bluetooth device found
//                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//                Toast.makeText(getApplicationContext(), "giot device "+ device.getName(),Toast.LENGTH_SHORT).show();
//                deviceList.add(device.getName());
//                adapter.notifyDataSetChanged();
//            }
//        }
//    };

    public void scan(View v){
        lv.setVisibility(View.VISIBLE);

        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }

        // set up adapter and check bluetooth is on
        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

//      scanLeDevice(true);
        if (pairedDevices.size() > 0) {
            deviceList.clear();
            devices.clear();
            for (BluetoothDevice device : pairedDevices) {
                Log.i("fffff", " "+device.getName()+ " - "+device.getAddress()+ " - "+device.getType());
                deviceList.add(device.getName());
                devices.add(device);
            }
            Log.i("hhhhhhhh", "ping ");
            adapter.notifyDataSetChanged();
        }
        scanLeDevice(true);
    }


    @Override
    public void onDestroy() {
        Log.i("onDestroyonDestr  ", "onCharacteristicChanged hey");

        if (bluetoothGatt == null) {
            super.onDestroy();
        } else {

            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            super.onDestroy();}
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    btAdapter.stopLeScan(leScanCallback);
                    Toast.makeText(getApplicationContext(), "Scanning Ended",Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            btAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            btAdapter.stopLeScan(leScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "got device "+ device.getName(),Toast.LENGTH_SHORT).show();
                            deviceList.add(device.getName()+"-"+device.getAddress());
                            devices.add(device);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            Log.i("REWWRE  ", "onCharacteristicChanged hey");

            final byte[] data = characteristic.getValue();
            final SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss - dd/MM/yy");

            hrView.post(new Runnable() {
                public void run() {
                    tHRvalueHR.setText(" - " + data[1] + " - ");
                    tHRvalueDate.setText(sdf.format(new Date()));
                }
            });

        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            Log.i("REWWRE  ", "onConnectionStateChange hey " + status + " - " +newState);
            if(newState == BluetoothProfile.STATE_CONNECTED){
                bluetoothGatt.discoverServices();
            }
        }



        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call

            List<BluetoothGattService> services = bluetoothGatt.getServices();
            Log.i("REWWRE  ", "onServicesDiscovered hey :" + status);
            for (BluetoothGattService service : services) {

                if (service.getUuid().toString().equals("0000180d-0000-1000-8000-00805f9b34fb")) {
                    BluetoothGattCharacteristic c = service.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
                    bluetoothGatt.setCharacteristicNotification(c, true);
                    BluetoothGattDescriptor descriptor = c.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                }

//                if (service.getUuid().toString().equals("0000180f-0000-1000-8000-00805f9b34fb")) {
//                    BluetoothGattCharacteristic c = service.getCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));
//                    bluetoothGatt.setCharacteristicNotification(c, true);
//                    BluetoothGattDescriptor descriptor = c.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//                    bluetoothGatt.writeDescriptor(descriptor);
//                }

                lv.post(new Runnable() {
                    public void run() {
                        lv.setVisibility(View.GONE);
                    }
                });


            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("QQQQQQQ", "Callback: Wrote GATT Descriptor successfully.");
            }
            else{
                Log.d("QQQQQQQ", "Callback: Error writing GATT Descriptor: "+ status);
            }
        };

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)  {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("QQQQQQQ", "Callback: Wrote GATT Descriptor successfully.");
            }
            else{
                Log.d("QQQQQQQ", "Callback: Error writing GATT Descriptor: "+ status);
            }
        };
    };

    private void updateHR(String str){

    }



//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) return;
//        String uuid = null;
//        String unknownServiceString = "unknownService";
//        String unknownCharaString = "unknownChar";
//        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
//        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =  new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
//
//        // Loops through available GATT Services.
//        for (BluetoothGattService gattService : gattServices) {
//            HashMap<String, String> currentServiceData = new HashMap<String, String>();
//            uuid = gattService.getUuid().toString();
//            currentServiceData.put("NAME", SampleGattAttributes.lookup(uuid, unknownServiceString));
//            currentServiceData.put("UUID", uuid);
//            gattServiceData.add(currentServiceData);
//
//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                    new ArrayList<HashMap<String, String>>();
//            List<BluetoothGattCharacteristic> gattCharacteristics =
//                    gattService.getCharacteristics();
//            ArrayList<BluetoothGattCharacteristic> charas =
//                    new ArrayList<BluetoothGattCharacteristic>();
//
//            // Loops through available Characteristics.
//            for (BluetoothGattCharacteristic gattCharacteristic :
//                    gattCharacteristics) {
//                charas.add(gattCharacteristic);
//                HashMap<String, String> currentCharaData =
//                        new HashMap<String, String>();
//                uuid = gattCharacteristic.getUuid().toString();
//                currentCharaData.put(
//                        "NAME", SampleGattAttributes.lookup(uuid,
//                                unknownCharaString));
//                currentCharaData.put("UUID", uuid);
//                gattCharacteristicGroupData.add(currentCharaData);
//            }
//            mGattCharacteristics.add(charas);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
//        }
//
//        Log.i("REWWRE  ", "onServicesDiscovered hey :" );
//    }






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
}
