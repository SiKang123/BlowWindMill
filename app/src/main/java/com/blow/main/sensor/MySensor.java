package com.blow.main.sensor;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;

/**
 * Created by SiKang on 2016/1/5.
 */
public abstract class MySensor {
    Object mLock;
    SensorManager sensorManager;
    Context hostContext;
    Handler mHandler;

    public MySensor(Context context, Handler mHandler) {
        mLock = new Object();
        this.mHandler = mHandler;
        hostContext = context;
        sensorManager = (SensorManager) hostContext.getSystemService(hostContext.SENSOR_SERVICE);
    }

    public MySensor() {
        mLock = new Object();
    }


    public abstract void start();

    public abstract void shutDown();

    public abstract void destory();
}
