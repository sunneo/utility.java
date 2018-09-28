package com.example;


import com.example.android.IUiThreadRunner;
import com.example.delegate.EventDelegate;
import com.example.notification.INotificationEventArgs;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sunneo on 2018/8/5.
 */

public class TimerWrapper {
    private Timer mTimer;
    private IUiThreadRunner mParent;
    EventDelegate ValueChanged = new EventDelegate();
    EventDelegate EnabledChanged = new EventDelegate();
    volatile long Interval = 1000L;
    volatile boolean bEnabled=false;

    protected void onEnabledChanged(boolean bEnabled){
        EnabledChanged.invoke(this, bEnabled);
    }
    protected void onValueChanged(){
        boolean origEnabled=bEnabled;
        mTimer.cancel();
        if(origEnabled){
            start();
        }
        ValueChanged.invoke(this, new INotificationEventArgs());
    }

    public synchronized void start(){
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mParent!=null){
                    mParent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Tick.invoke(this);
                        }
                    });
                }
                else {
                    Tick.invoke(this);
                }
            }
        },0L,Interval);
        this.bEnabled=true;
    }
    public synchronized void stop(){
        mTimer.cancel();
        bEnabled=false;
    }

    public synchronized void setInterval(long interval){
        long origVal = this.Interval;
        Interval=interval;
        if(origVal!=Interval){
            onValueChanged();
        }
    }
    public synchronized boolean isEnabled(){
        return bEnabled;
    }
    public synchronized void setEnabled(boolean bEnabled){
        boolean orig = bEnabled;
        this.bEnabled=bEnabled;
        if(orig != this.bEnabled){
            onEnabledChanged(this.bEnabled);
        }
    }

    public TimerWrapper(IUiThreadRunner activity){
        this.mParent=activity;
        this.mTimer = new Timer();
    }



    public EventDelegate Tick = new EventDelegate();
}
