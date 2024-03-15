package com.example.quickshare.shareReceiveFile;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quickshare.CONSTANTS;
import com.example.quickshare.MyViewPagerAdapter;
import com.example.quickshare.R;
import com.example.quickshare.RecipientFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SendReceiveFileActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    MyViewPagerAdapter myAdapter;
    TabLayout tabLayout;

    BluetoothAdapter bluetoothAdapter;
    public static BluetoothSocket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_recive_file);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        askPermission();
        enableBluetooth();
        ensureDiscoverable();

        viewPager = findViewById(R.id.viewPager2);
        myAdapter = new MyViewPagerAdapter(
                getSupportFragmentManager(),
                getLifecycle());
        myAdapter.addFragment(new FileSharingFragment());
        myAdapter.addFragment(new RecipientFragment());
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(myAdapter);

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if(position == CONSTANTS.SEND_FILE_POSE) {
                            tab.setText("Share file");
                        }
                        if (position == CONSTANTS.RECEIVE_FILE_POSE) {
                            tab.setText("Receive file");
                        }
                    }
                }
        ).attach();


        int defaultFragment = getIntent().getIntExtra("default_fragment", CONSTANTS.SEND_FILE_POSE); // Default to Share file fragment
        viewPager.setCurrentItem(defaultFragment);
    }


    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            askPermission();
        }
//        if (bluetoothAdapter.getScanMode() !=
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
        if(true){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                askPermission();
            }
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, CONSTANTS.REQUEST_ENABLE_BT);
            return;
        }
    }

    public void askPermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 1);
            try {
                wait(750);
            } catch (InterruptedException e) {
            }
        }
    }
}