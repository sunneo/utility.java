package com.example.android;

import com.example.sharp.Delegates;

public interface IUiThreadRunner {
	void runOnUiThread(Delegates.Action r);
}
