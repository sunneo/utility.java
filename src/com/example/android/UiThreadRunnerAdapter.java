package com.example.android;

import com.example.android.IUiThreadRunner;

public class UiThreadRunnerAdapter implements IUiThreadRunner {
    @Override
    public void runOnUiThread(Runnable r) {
        r.run();
    }
}
