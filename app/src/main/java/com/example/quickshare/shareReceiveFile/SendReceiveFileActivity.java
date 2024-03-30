package com.example.quickshare.shareReceiveFile;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quickshare.R;
import com.example.quickshare.Utils.CONSTANTS;
import com.example.quickshare.Utils.MyViewPagerAdapter;
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

        enableBluetooth();
        ensureDiscoverable();

        FileSharingFragment fileSharingFragment;

        if(getIntent().getStringExtra("file_path")!=null
                && getIntent().getStringExtra("file_type")!=null
                && getIntent().getStringExtra("file_size")!=null
                && getIntent().getByteArrayExtra("file_data")!=null) {

            String tmpFileSize = getIntent().getStringExtra("file_size");
            int fileSize = 0;
            if(tmpFileSize != null) {
                try {
                    fileSize = Integer.parseInt(tmpFileSize);
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }


            fileSharingFragment = new FileSharingFragment(
                    getIntent().getStringExtra("file_path"),
                    getIntent().getStringExtra("file_type"),
                    fileSize,
                    getIntent().getByteArrayExtra("file_data"));
        }
        else {
            fileSharingFragment = new FileSharingFragment();
        }

        viewPager = findViewById(R.id.viewPager2);
        myAdapter = new MyViewPagerAdapter(
                getSupportFragmentManager(),
                getLifecycle());
        myAdapter.addFragment(fileSharingFragment);
        myAdapter.addFragment(new RecipientFragment());
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(myAdapter);

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if(position == CONSTANTS.misc.SEND_FILE_POSE) {
                            tab.setText("Share file");
                        }
                        if (position == CONSTANTS.misc.RECEIVE_FILE_POSE) {
                            tab.setText("Receive file");
                        }
                    }
                }
        ).attach();


        int defaultFragment = getIntent().getIntExtra("default_fragment", CONSTANTS.misc.SEND_FILE_POSE); // Default to Share file fragment
        viewPager.setCurrentItem(defaultFragment);

    }

    public void ensureDiscoverable() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 33);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 33);
        }
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            // Initialize Activity Result Launcher for enabling Bluetooth
            ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK) {
                            Toast.makeText(this, R.string.bt_not_enabled_leaving,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            // Create intent to enable Bluetooth
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBluetoothIntent);
        }
    }
}