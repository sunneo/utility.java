package com.example.android;

import com.example.sharp.Delegates;

public class UiThreadRunnerAdapter implements IUiThreadRunner {
	@Override
	public void runOnUiThread(Delegates.Action r) {
		r.Invoke();
	}
}
