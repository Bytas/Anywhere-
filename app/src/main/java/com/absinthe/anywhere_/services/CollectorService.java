package com.absinthe.anywhere_.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.service.quicksettings.Tile;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.absinthe.anywhere_.R;
import com.absinthe.anywhere_.model.CollectorWindowManager;
import com.absinthe.anywhere_.model.CommandResult;
import com.absinthe.anywhere_.model.Const;
import com.absinthe.anywhere_.model.GlobalValues;
import com.absinthe.anywhere_.utils.CommandUtils;
import com.absinthe.anywhere_.utils.NotificationUtils;
import com.absinthe.anywhere_.utils.manager.Logger;
import com.absinthe.anywhere_.utils.TextUtils;
import com.absinthe.anywhere_.utils.ToastUtil;

public class CollectorService extends Service {
    public static final String COMMAND = "COMMAND";
    public static final String COMMAND_OPEN = "COMMAND_OPEN";
    public static final String COMMAND_CLOSE = "COMMAND_CLOSE";

    CollectorWindowManager mCollectorWindowManager;

    private void initCollectorWindowManager() {
        if (mCollectorWindowManager == null)
            mCollectorWindowManager = new CollectorWindowManager(getApplicationContext());
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler();

    private Runnable getCurrentInfoTask = new Runnable() {
        @Override
        public void run() {
            if (mCollectorWindowManager != null && mCollectorWindowManager.getView() != null) {
                String result = CommandUtils.execAdbCmd(Const.CMD_GET_TOP_STACK_ACTIVITY);

                if (result == null
                        || result.equals(CommandResult.RESULT_NULL)
                        || result.equals(CommandResult.RESULT_ROOT_PERM_ERROR)
                        || result.equals(CommandResult.RESULT_SHIZUKU_PERM_ERROR)) {
                    Thread.currentThread().interrupt();
                } else {
                    String[] params = TextUtils.processResultString(result);
                    if (params != null) {
                        mCollectorWindowManager.setInfo(params[0], params[1]);
                    }
                }
            }

            mHandler.postDelayed(this, GlobalValues.sDumpInterval);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, getNotificationInstance());
        Logger.i("CollectorService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            ToastUtil.makeText(R.string.toast_collector_service_launch_failed);
            stopSelf();
        } else {
            initCollectorWindowManager();
            String command = intent.getStringExtra(COMMAND);
            if (command != null) {
                if (command.equals(COMMAND_OPEN)) {
                    mCollectorWindowManager.addView();

                    if (GlobalValues.sIsCollectorPlus) {
                        mHandler.post(getCurrentInfoTask);
                    }
                } else if (command.equals(COMMAND_CLOSE)) {
                    Logger.d("Intent:COMMAND_CLOSE");
                    mHandler.removeCallbacks(getCurrentInfoTask);
                    mCollectorWindowManager.removeView();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (CollectorTileService.getInstance() != null) {
                            Tile tile = CollectorTileService.getInstance().getQsTile();
                            tile.setState(Tile.STATE_INACTIVE);
                            tile.setLabel(getString(R.string.tile_collector_on));
                            tile.updateTile();
                        }
                    }
                    stopSelf();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Logger.d("CollectorService onDestroy.");
        super.onDestroy();
    }

    private Notification getNotificationInstance() {
        return new NotificationCompat.Builder(this, NotificationUtils.COLLECTOR_CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_collector_title))
                .setContentText(getText(R.string.notification_collector_content))
                .setSmallIcon(R.drawable.ic_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
