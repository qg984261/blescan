package marco.com.blescantest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class DeviceActivity extends AppCompatActivity {
    //180A Device Information
    public static final UUID SERVICE_DEVICE_INFORMATION = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_MODEL_NUMBER_STRING = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_SERIAL_NUMBER_STRING = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_HARDWARE_REVISION_STRING = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_SOFTWARE_REVISION_STRING = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_MANUFACTURER_NAME_STRING = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");

    //180F Battery Service
    public static final UUID SERVICE_BATTERY = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    public static final UUID CHAR_BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static final UUID DESC_CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34b");

    //
    public static final String EXTRA_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRA_BLUETOOTH_DEVICE = "BT_DEVICE";

    public static final String TAG = "DeviceActivity";


    private BluetoothDevice mBTDevice;
    private BluetoothGatt mBTGatt;
    private static int mStatus = 0;
    private BluetoothGattCharacteristic ch;
    private Button bt_battery, bt_model_number, bt_serial_number, bt_hardware_ver, bt_software_ver, bt_manufacturer_name;
    private ArrayList<Button> buttons = new ArrayList<>();
    private TextView tv_msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        //keep screen on
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mBTDevice = getIntent().getExtras().getParcelable(EXTRA_BLUETOOTH_DEVICE);
        String name = getIntent().getStringExtra(EXTRA_DEVICE_NAME);
        String address = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);

        findviews();

        initial();

    }

    private void findviews() {
        bt_battery = findViewById(R.id.bt_battery);
        bt_model_number = findViewById(R.id.bt_model_number);
        bt_serial_number = findViewById(R.id.bt_serial_number);
        bt_hardware_ver = findViewById(R.id.bt_hardware_ver);
        bt_software_ver = findViewById(R.id.bt_software_ver);
        bt_manufacturer_name = findViewById(R.id.bt_manufacturer_name);

        bt_battery.setOnClickListener(bts_listener);
        bt_model_number.setOnClickListener(bts_listener);
        bt_serial_number.setOnClickListener(bts_listener);
        bt_hardware_ver.setOnClickListener(bts_listener);
        bt_software_ver.setOnClickListener(bts_listener);
        bt_manufacturer_name.setOnClickListener(bts_listener);
        buttons.add(bt_battery);
        buttons.add(bt_model_number);
        buttons.add(bt_serial_number);
        buttons.add(bt_hardware_ver);
        buttons.add(bt_software_ver);
        buttons.add(bt_manufacturer_name);

        tv_msg = findViewById(R.id.textView);
    }

    private Button.OnClickListener bts_listener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_battery:
                    ch = mBTGatt.getService(SERVICE_BATTERY).getCharacteristic(CHAR_BATTERY_LEVEL);
                    break;
                case R.id.bt_model_number:
                    ch = mBTGatt.getService(SERVICE_DEVICE_INFORMATION).getCharacteristic(CHAR_MODEL_NUMBER_STRING);
                    break;
                case R.id.bt_serial_number:
                    ch = mBTGatt.getService(SERVICE_DEVICE_INFORMATION).getCharacteristic(CHAR_SERIAL_NUMBER_STRING);
                    break;
                case R.id.bt_hardware_ver:
                    ch = mBTGatt.getService(SERVICE_DEVICE_INFORMATION).getCharacteristic(CHAR_HARDWARE_REVISION_STRING);
                    break;
                case R.id.bt_software_ver:
                    ch = mBTGatt.getService(SERVICE_DEVICE_INFORMATION).getCharacteristic(CHAR_SOFTWARE_REVISION_STRING);
                    break;
                case R.id.bt_manufacturer_name:
                    ch = mBTGatt.getService(SERVICE_DEVICE_INFORMATION).getCharacteristic(CHAR_MANUFACTURER_NAME_STRING);
                    break;
            }

            if (ch == null) {
                Log.i(TAG, "characteristic is null");
                return;
            }
            mBTGatt.readCharacteristic(ch);
            switch_all_button(false);
        }
    };

    private void initial() {
        //connect device
        if (mBTDevice != null) {
            if (mBTGatt == null) {
                mBTGatt = mBTDevice.connectGatt(this, false, mGattCallback);
            } else {
                if (mStatus == BluetoothProfile.STATE_DISCONNECTED) {
                    mBTGatt.connect();
                    mBTGatt.discoverServices();
                }
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBTGatt == null) {
            return;
        }
        mBTGatt.close();
        mBTGatt = null;
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mStatus = newState;
                mBTGatt.discoverServices();
                Log.i(TAG, "Gatt Server Connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mStatus = newState;
                Log.i(TAG, "Gatt Server Disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "myservice uuid = : " + SERVICE_BATTERY.toString());
            for (BluetoothGattService service : gatt.getServices()) {
                if ((service == null) || (service.getUuid() == null)) {
                    continue;
                }
                Log.i(TAG, "  service uuid = : " + service.getUuid().toString());

                for (BluetoothGattCharacteristic characerteristic : service.getCharacteristics()) {
                    Log.i(TAG, "chara uuid = " + characerteristic.getUuid().toString());
                }
            }

            Log.i(TAG, "onDiscovered over");
            runOnUiThread(new Runnable() {
                public void run() {
                    switch_all_button(true);
                }
            });


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] data = characteristic.getValue();
            String text = "";

            if (characteristic.getUuid().equals(CHAR_BATTERY_LEVEL)) {
                for (int i = 0; i < data.length; i++) {
                    text = text + String.valueOf((int) data[i]) + "";

                }
            } else {
                try {
                    text = new String(data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            final String result = text;
            Log.i(TAG + " read: ", text);
            runOnUiThread(new Runnable() {
                public void run() {
                    switch_all_button(true);
                    changeMsg(result);
                }
            });


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] data = characteristic.getValue();
            String text = "";
            for (int i = 0; i < data.length; i++) {
                text = text + String.valueOf((int) data[i]) + ",";
            }
            Log.i(TAG + " write: ", text);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            byte[] data = characteristic.getValue();
            String text = "";
            for (int i = 0; i < data.length; i++) {
                text = text + String.valueOf((int) data[i]) + ",";
            }
            Log.i(TAG + " changed: ", text);
        }
    };

    private void switch_all_button(boolean flag) {
        for (Button bt : buttons) {
            bt.setEnabled(flag);
        }
    }

    private void changeMsg(String str) {
        tv_msg.setText(str);
    }
}
