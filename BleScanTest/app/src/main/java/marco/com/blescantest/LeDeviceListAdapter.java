package marco.com.blescantest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<Integer> mLeRssi;
    private LayoutInflater mInflator;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        mLeRssi = new ArrayList<Integer>();
        mInflator = LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device, int rssi) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
            mLeRssi.add(rssi);
        } else {
            mLeRssi.set(mLeDevices.indexOf(device), rssi);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mLeDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.sample_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = convertView.findViewById(R.id.device_address);
            viewHolder.deviceName = convertView.findViewById(R.id.device_name);
            viewHolder.deviceRssi = convertView.findViewById(R.id.device_rssi);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String deviceName = mLeDevices.get(position).getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText("NAME : "+deviceName);
        else
            viewHolder.deviceName.setText("NAME : "+"null");

        viewHolder.deviceAddress.setText("Mac : "+mLeDevices.get(position).getAddress());
        viewHolder.deviceRssi.setText("Rssi : "+String.valueOf(mLeRssi.get(position)));

        return convertView;
    }

    private class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
    }
}
