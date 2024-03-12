package com.example.quickshare.shareReceiveFile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_recive_file);

        TabLayout tabLayout = findViewById(R.id.tabLayout);


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
}