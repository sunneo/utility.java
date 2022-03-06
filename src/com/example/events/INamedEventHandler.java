package com.example.events;

import com.example.sharp.GenericDataSet;

/**
 * lazy event handler 
 * get_1(): String event name 
 * get_2(): <String.T> key-value set for named parameter
 */
public interface INamedEventHandler<T> extends INotification<INotificationEventArgs.INotificationEventArg2<T, GenericDataSet>>{
	
}