package marco.com.blescantest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView textview_msg;
    private Button button_scan;
    private Button button_stopscan;
    private ListView listview_result;
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothManager bluetoothManager;
    private Handler mHandler;
    private static final int REQUEST_BT_ENABLE = 1;
    private static final int REQUEST_ACCESS_COARSE_LOCATION_PERMISSION = 2;
    private static final long SCAN_PERIOD_mSECOND = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //keep screen on
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mHandler = new Handler();
        //find view
        findview();

        //check device available
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showMSG("Your device doesn't support BLE");
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //check bluetooth support
        if (mBluetoothAdapter == null) {
            //doesn't support bluetooth
            showMSG("Your device doesn't support Bluetooth");
        }
        //check bluetooth enabled
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
            }
        }
        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION_PERMISSION);
        }


        mLeDeviceListAdapter = new LeDeviceListAdapter(MainActivity.this);
        listview_result.setAdapter(mLeDeviceListAdapter);
        listview_result.setOnItemClickListener(listview_listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        scan_device(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BT_ENABLE && resultCode == Activity.RESULT_CANCELED) {
            showMSG("Need to enable bluetooth, please restart and try again.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage("Please allow the app to have the permission")
                            .setPositiveButton("OK", null)
                            .show();
                    textview_msg.setText("Please allow the app to have the permission");
                }
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showMSG(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        textview_msg.setText(msg);
    }

    private void findview() {
        textview_msg = findViewById(R.id.textView_msg);
        button_scan = findViewById(R.id.button_scan);
        button_stopscan = findViewById(R.id.button_stopscan);
        listview_result = findViewById(R.id.listview_result);

        button_scan.setOnClickListener(bt_listener);
        button_stopscan.setOnClickListener(bt_listener);
    }

    private Button.OnClickListener bt_listener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_scan:
                    scan_device(true);
                    break;
                case R.id.button_stopscan:
                    scan_device(false);
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener listview_listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            if(device == null){
                return;
            }
            scan_device(false);

            Intent intent = new Intent(MainActivity.this,DeviceActivity.class);
            intent.putExtra(DeviceActivity.EXTRA_BLUETOOTH_DEVICE, device);
            intent.putExtra(DeviceActivity.EXTRA_DEVICE_NAME, device.getName());
            intent.putExtra(DeviceActivity.EXTRA_DEVICE_ADDRESS, device.getAddress());

            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();

            startActivity(intent);
        }
    };

    private void scan_device(boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    button_scan.setEnabled(true);
                    textview_msg.setText("Stop Scan");
                }
            }, SCAN_PERIOD_mSECOND);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
            button_scan.setEnabled(false);
            textview_msg.setText("Start Scan");
        } else {
            button_scan.setEnabled(true);
            textview_msg.setText("Stop Scan");
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device,rssi);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });

        }
    };
}
