package com.example.events;

import com.example.sharp.BaseDictionary;
import com.example.sharp.Delegates;
import com.example.sharp.GenericDataSet;
import com.example.sharp.Tracer;

/**
 * named event handler holds one-shot event handler and a named event for a  
 * listener design-pattern.
 */
public class NamedEventHandler<NamedType> {
	public final EventDelegate<INamedEventHandler<NamedType>> OnNamedEventTriggered = new EventDelegate<INamedEventHandler<NamedType>>();
	private final BaseDictionary<NamedType, Object> OneShotEventHandlers = new BaseDictionary<>();
	private final BaseDictionary<NamedType, Object> OneShotDataStorage  = new BaseDictionary<>();
	public synchronized void RegisterOneShotEventHandler(NamedType command, Delegates.Func2<Object, GenericDataSet, Boolean> handler) {
		OneShotEventHandlers.set(command, handler);
	}
	public synchronized void UnregisterOneShotEventHandler(NamedType command) {
		OneShotEventHandlers.Remove(command);
	}
	public synchronized <T> void SetOneShotDataStorage(NamedType name,T value) {
		OneShotDataStorage.set(name, value);
	}
	public synchronized void UnsetOneShotDataStorage(NamedType name) {
		OneShotDataStorage.Remove(name);
	}
	@SuppressWarnings("unchecked")
	public synchronized <T> T GetOneShotDataStorage(NamedType name) {
		if(!OneShotDataStorage.ContainsKey(name)) return null;
		T ret = (T)OneShotDataStorage.get(name);
		OneShotDataStorage.Remove(name);
		return ret;
	}
	/**
	 * Handle One Shot Event Handler
	 * @see RegisterOneShotEventHandler
	 * @param sender sender
	 * @param command command
	 * @param dataSet dataSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean HandleOneShotEventHandler(Object sender,NamedType command, GenericDataSet dataSet) {
		if(OneShotEventHandlers.ContainsKey(command)) {
			Delegates.Func2<Object, GenericDataSet, Boolean> handler = (Delegates.Func2<Object, GenericDataSet, Boolean>)OneShotEventHandlers.get(command);
			try {
				OneShotEventHandlers.Remove(command);
			    return handler.Invoke(sender,dataSet);
			}catch(Exception ee) {
				Tracer.D(ee);
				return false;
			}
		}
		return false;
	}
	public void invoke(Object sender,NamedType name,GenericDataSet args) {
		this.OnNamedEventTriggered.invoke(sender, name,args);
	}

}
