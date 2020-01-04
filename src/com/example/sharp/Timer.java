package com.example.sharp;

import java.util.TimerTask;

import com.example.android.IUiThreadRunner;
import com.example.events.EventDelegate;

/**
 * TimerWrapper as System.Windows.Forms.Timer in C# usage:
 * 
 * <pre>
 * TimerWrapper timer = new TimerWrapper();
 * timer.Tick.addDelegate(() -> {
 * 	// do something.
 * });
 * timer.start();
 * </pre>
 */
public class Timer {
	private java.util.Timer mTimer;
	private IUiThreadRunner mParent;
	@SuppressWarnings("rawtypes")
	EventDelegate ValueChanged = new EventDelegate();
	@SuppressWarnings("rawtypes")
	EventDelegate EnabledChanged = new EventDelegate();
	volatile long Interval = 1000L;
	volatile boolean bEnabled = false;

	@SuppressWarnings("unchecked")
	protected void onEnabledChanged(boolean bEnabled) {

		EnabledChanged.invoke(this, bEnabled);
	}

	protected void onValueChanged() {
		boolean origEnabled = bEnabled;
		mTimer.cancel();
		if (origEnabled) {
			start();
		}
		ValueChanged.invoke(this, new com.example.events.INotificationEventArgs());
	}

	public synchronized void start() {
		mTimer = new java.util.Timer();
		mTimer.schedule(new TimerTask() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if (mParent != null) {
					mParent.runOnUiThread(() -> Tick.invoke(this, this));
				} else {
					Tick.invoke(this);
				}
			}
		}, 0L, Interval);
		this.bEnabled = true;
	}

	public synchronized void stop() {
		mTimer.cancel();
		bEnabled = false;
	}

	public synchronized void setInterval(long interval) {
		long origVal = this.Interval;
		Interval = interval;
		if (origVal != Interval) {
			onValueChanged();
		}
	}

	public synchronized boolean isEnabled() {
		return bEnabled;
	}

	public synchronized void setEnabled(boolean bEnabled) {
		boolean orig = bEnabled;
		this.bEnabled = bEnabled;
		if (orig != this.bEnabled) {
			onEnabledChanged(this.bEnabled);
		}
	}

	public Timer(IUiThreadRunner activity) {
		this.mParent = activity;
		this.mTimer = new java.util.Timer();
	}

	@SuppressWarnings("rawtypes")
	public EventDelegate Tick = new EventDelegate();
}
