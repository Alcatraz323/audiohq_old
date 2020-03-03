package io.alcatraz.audiohq.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.adapters.FloatAdapter;
import io.alcatraz.audiohq.beans.nativebuffers.PackageBuffers;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;
import io.alcatraz.audiohq.utils.ShellDataBridge;
import io.alcatraz.audiohq.utils.Utils;

public class FloatPanelService extends Service {
    UpdatePreferenceReceiver updatePreferenceReceiver;

    private NotificationManager notificationManager;
    private String notificationId = "audiohq_floater";
    private String notificationName = "AudioHQ Foreground";

    VolumeChangeObserver observer;
    WindowManager windowManager;
    LayoutInflater layoutInflater;
    WindowManager.LayoutParams layoutParams;

    Handler handler = new Handler();

    //Widgets
    View root;
    CardView toggle;
    CardView list_back;
    ListView listView;
    ImageView toggle_icon;

    FloatAdapter adapter;
    PackageBuffers packageBuffers = new PackageBuffers();

    boolean hasShownPanel = false;

    //Preference
    String gravity;
    String background;
    boolean foreground_service;
    String dismiss_delay;
    String margin_top;
    String margin_top_landscape;
    String icon_tint;
    String toggle_size;
    String font_color;

    Runnable cleaner = new Runnable() {
        @Override
        public void run() {
            windowManager.removeView(root);
            list_back.setVisibility(View.GONE);
            hasShownPanel = false;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadPreference();
        initialize();
        initializeWindow();
        registReceivers();
    }

    private void initialize() {
        observer = new VolumeChangeObserver(this);
        observer.registerVolumeReceiver();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        observer.setOnVolumeChangeListener(new VolumeChangeObserver.OnVolumeChangeListener() {
            @Override
            public void onVolumeChange() {
                synchronized (this) {
                    if (!hasShownPanel) {
                        showFloatingWindow();
                        hasShownPanel = true;
                    }
                }
            }
        });

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void initializeWindow() {
        initViews();

        layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (gravity.equals("start_top"))
            layoutParams.gravity = Gravity.START | Gravity.TOP;
        else if (gravity.equals("end_top"))
            layoutParams.gravity = Gravity.END | Gravity.TOP;
        layoutParams.x = 0;
        layoutParams.y = Utils.Dp2Px(this,Integer.parseInt(margin_top));
        if (foreground_service)
            startForeground(1, getNotification());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        if (gravity.equals("start_top"))
            root = layoutInflater.inflate(R.layout.panel_float, null);
        else if (gravity.equals("end_top"))
            root = layoutInflater.inflate(R.layout.panel_float_right, null);

        listView = root.findViewById(R.id.float_list);
        toggle = root.findViewById(R.id.float_trigger);
        list_back = root.findViewById(R.id.float_card);
        toggle_icon = root.findViewById(R.id.float_toggle_icon);
        adapter = new FloatAdapter(this, packageBuffers, handler, cleaner);

        int tg_size_integer = Utils.Dp2Px(this,Integer.parseInt(toggle_size));
        toggle.setCardBackgroundColor(Color.parseColor(background));
        Utils.setViewSize(toggle,tg_size_integer,tg_size_integer);
        Utils.setImageWithTint(toggle_icon,R.drawable.ic_pencil,Color.parseColor(icon_tint));
        adapter.setFontColor(font_color);
        list_back.setCardBackgroundColor(Color.parseColor(background));
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.getCount() != 0) {
                    if (list_back.getVisibility() == View.GONE) {
                        list_back.setVisibility(View.VISIBLE);
                        try {
                            AnimateUtils.playstart(list_back, new AnimateUtils.SimpleAnimateInterface() {
                                @Override
                                public void onEnd() {

                                }
                            });
                        } catch (Exception e) {

                        }

                    } else {
                        try {
                            AnimateUtils.playEnd(list_back);
                        } catch (Exception e) {

                        }
                    }
                }
            }
        });
        toggle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        handler.removeCallbacks(cleaner);
                        break;
                    case MotionEvent.ACTION_UP:
                        handler.postDelayed(cleaner,Integer.parseInt(dismiss_delay));
                        break;
                }
                return false;
            }
        });
    }

    private void checkAndAdjustHeight() {
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        if (totalHeight >= Utils.Dp2Px(this, 320))
            layoutParams.height = Utils.Dp2Px(this, 320);
        else
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Configuration mConfiguration = this.getResources().getConfiguration();
        int ori = mConfiguration.orientation;

        if(ori == Configuration.ORIENTATION_LANDSCAPE){
            layoutParams.y = Utils.Dp2Px(this,Integer.parseInt(margin_top_landscape));
        }else {
            layoutParams.y = Utils.Dp2Px(this,Integer.parseInt(margin_top));
        }
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            updateList();
            checkAndAdjustHeight();
            windowManager.addView(root, layoutParams);

            int dismiss_mills = Integer.parseInt(dismiss_delay);

            root.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                            handler.removeCallbacks(cleaner);
                            break;
                        case MotionEvent.ACTION_UP:
                            handler.postDelayed(cleaner,dismiss_mills);
                            break;
                    }
                    return false;
                }
            });

            handler.postDelayed(cleaner, dismiss_mills);
        }
    }

    public void updateList() {
        ShellDataBridge.getProcessBuffersService(new AsyncInterface<PackageBuffers>() {
            @Override
            public boolean onAyncDone(@Nullable PackageBuffers val) {
                packageBuffers = (val == null ? new PackageBuffers() : val);
                adapter.setNewData(packageBuffers);
                listView.setAdapter(adapter);

                return false;
            }

            @Override
            public void onFailure(String reason) {
                Toast.makeText(FloatPanelService.this, reason, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        observer.unregisterVolumeReceiver();
        unregisterReceiver(updatePreferenceReceiver);
        super.onDestroy();
    }

    private void loadPreference() {
        SharedPreferenceUtil spf = SharedPreferenceUtil.getInstance();
        gravity = (String) spf.get(this, Constants.PREF_FLOAT_WINDOW_GRAVITY,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_GRAVITY);
        background = (String) spf.get(this, Constants.PREF_FLOAT_WINDOW_BACKGROUND,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_BACKGROUND);
        foreground_service = (boolean) spf.get(this, Constants.PREF_FLOAT_FOREGROUND_SERVICE,
                Constants.DEFAULT_VALUE_PREF_FLOAT_FOREGROUND_SERVICE);
        dismiss_delay = (String) spf.get(this, Constants.PREF_FLOAT_WINDOW_DISMISS_DELAY,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_DISMISS_DELAY);
        margin_top = (String) spf.get(this, Constants.PREF_FLOAT_WINDOW_MARGIN_TOP,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_MARGIN_TOP);
        margin_top_landscape = (String) spf.get(this, Constants.PREF_FLOAT_WINDOW_MARGIN_TOP_LANDSCAPE,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_MARGIN_TOP_LANDSCAPE);
        icon_tint = (String) spf.get(this,Constants.PREF_FLOAT_WINDOW_ICON_TINT,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_ICON_TINT);
        toggle_size = (String) spf.get(this,Constants.PREF_FLOAT_WINDOW_TOGGLE_SIZE,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_TOGGLE_SIZE);
        font_color = (String) spf.get(this,Constants.PREF_FLOAT_WINDOW_FONT_COLOR,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_FONT_COLOR);
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_chevron_right)
                .setContentTitle(getString(R.string.notification_overlay));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        return notification;
    }

    public void registReceivers() {
        IntentFilter ifil = new IntentFilter();
        ifil.addAction(Constants.BROADCAST_ACTION_UPDATE_PREFERENCES);
        updatePreferenceReceiver = new UpdatePreferenceReceiver();
        registerReceiver(updatePreferenceReceiver, ifil);
    }

    class UpdatePreferenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            loadPreference();
            initializeWindow();
        }
    }
}

class VolumeChangeObserver {
    private static final String ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";

    private Context mContext;
    private OnVolumeChangeListener mOnVolumeChangeListener;
    private VolumeReceiver mVolumeReceiver;

    public VolumeChangeObserver(Context context) {
        mContext = context;
    }

    public void registerVolumeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_VOLUME_CHANGED);
        mVolumeReceiver = new VolumeReceiver(this);
        mContext.registerReceiver(mVolumeReceiver, intentFilter);
    }

    public void unregisterVolumeReceiver() {
        if (mVolumeReceiver != null) mContext.unregisterReceiver(mVolumeReceiver);
        mOnVolumeChangeListener = null;
    }

    public void setOnVolumeChangeListener(OnVolumeChangeListener listener) {
        this.mOnVolumeChangeListener = listener;
    }

    public interface OnVolumeChangeListener {
        void onVolumeChange();
    }

    private static class VolumeReceiver extends BroadcastReceiver {
        private WeakReference<VolumeChangeObserver> mObserver;

        VolumeReceiver(VolumeChangeObserver observer) {
            mObserver = new WeakReference<>(observer);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mObserver == null) return;
            if (mObserver.get().mOnVolumeChangeListener == null) return;
            if (isReceiveVolumeChange(intent)) {
                OnVolumeChangeListener listener = mObserver.get().mOnVolumeChangeListener;
                listener.onVolumeChange();
            }
        }

        private boolean isReceiveVolumeChange(Intent intent) {
            return intent.getAction() != null
                    && intent.getAction().equals(ACTION_VOLUME_CHANGED);
        }
    }
}
