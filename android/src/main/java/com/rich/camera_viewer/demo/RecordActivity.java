package com.rich.camera_viewer.demo;

import android.app.NotificationManager;
import android.os.Bundle;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rich.camera_viewer.R;

public class RecordActivity extends FragmentActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(LocalDefines.notificationID);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_record);

        PlaybackFragment playbackFragment = new PlaybackFragment();

        fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fl_root, playbackFragment);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }
}
