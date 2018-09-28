package com.example.notification;

/**
 * Created by sunneo on 2018/7/30.
 */

public interface INotification<T extends INotificationEventArgs> {
    void perform(Object from, T args);
}
