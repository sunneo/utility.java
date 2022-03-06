package com.example.events;

/**
 * a variable class which provides onValueChange event.
 *
 * @param <T> variable type.
 */
public class Var<T extends Object> extends BaseVar<T> {
	public static <T> Var<T> create(T val){
		return new Var<T>(val);
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.dispose();
		super.finalize();
	}

	public static class Builder<T>{
		boolean m_unsetOnNull;
		T m_defaultValue;
		boolean m_notifyOnOnlyChange=false;
		INotification<ValueChangedEventArgs<T>> m_valueChangedHandler;
		INotification<INotificationEventArgs.INotificationEventArg1<T>> m_valueChangeRejectHandler;
		public Builder<T> unsetOnNull(boolean enabled) {
			this.m_unsetOnNull=enabled;
			return this;
		}
		public Builder<T> value(T val) {
			this.m_defaultValue=val;
			return this;
		}
		public Builder<T> notifyOnlyOnChange(boolean bEnable){
			this.m_notifyOnOnlyChange=bEnable;
			return this;
		}
		public Builder<T> changed(INotification<ValueChangedEventArgs<T>> valueChangedHandler) {
			this.m_valueChangedHandler=valueChangedHandler;
			return this;
		}
		public Builder<T> changeRejected(INotification<INotificationEventArgs.INotificationEventArg1<T>> valueChangeRejectHandler) {
			this.m_valueChangeRejectHandler=valueChangeRejectHandler;
			
			return this;
		}
		public Var<T> build(){
			
			Var<T> ret = new Var<T>(m_defaultValue){
				public void set(T value) {
					super.set(value);
					if(m_unsetOnNull) {
						if(value == null) {
							this.hasValue=false;
						}
					}
				}
			};
			ret.notifyOnlyOnChanged=m_notifyOnOnlyChange;
			if(m_unsetOnNull && m_defaultValue == null) {
				ret.hasValue=false;
			}
			if(this.m_valueChangedHandler != null) {
				ret.onValueChanged.addDelegate(m_valueChangedHandler);
			}
			if(this.m_valueChangeRejectHandler != null) {
				ret.onValueChangeRejected.addDelegate(m_valueChangeRejectHandler);
			}
			return ret;
		}
	}
	/**
	 * return a restorable-which will restore to default value on a GET() is invoked
	 * 
	 * @param <T>
	 * @param defaultVal
	 * @return
	 */
	public static <T> Var<T> newRestorable(T defaultVal){
		return new RestorableVar<T>(defaultVal); 
	}
	
	boolean isdisposed=false;
	public boolean isDisposed() {
		return isdisposed;
	}
	public void dispose() {
		if(isDisposed()) return;
		if(this.onValueChanged != null)
			this.onValueChanged.dispose();
		this.onValueChanged = null;
		if(this.onValueChangeRejected != null)
			this.onValueChangeRejected.dispose();
		this.onValueChangeRejected = null;
		
		super.dispose();
		isdisposed=true;
	}
	

	public EventDelegate<INotification<ValueChangedEventArgs<T>>> onValueChanged = new EventDelegate<>();
	public EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<T>>> onValueChangeRejected = new EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<T>>>();

	/**
	 * notify value has changed.
	 */
	protected void notifyValueChanged(T newValue,T origValue) {
		if(this.isdisposed) return;
		if (this.onValueChanged != null && !onValueChanged.isEmpty()) {
			ValueChangedEventArgs<T> args = new ValueChangedEventArgs<>(newValue,origValue);
			this.onValueChanged.invoke(this, args);
			if(args.cancel) {
				this.value = origValue;
				onValueChangeRejected.invoke(this, origValue);
				return;
			}
		}
		notifyChanged();
	}

	/**
	 * set value, trigger ValueChanged event.
	 * @param value given value
	 * @return
	 */
	public void set(T value) {
		T origValue=this.value;
		boolean hadValue=hasValue;
		changed=false;
		if (notifyOnlyOnChanged) {
			if(!hadValue || !(this.value!=null && this.value.equals(value))) {
				changed=true;
			}
		} else {
			changed=true;
		}
		this.value = value;
		hasValue=true;
		if(skipInitialValueNotification) {
			if(!hadValue) {
				changed=false;
			}
		}
		if (changed) {
			notifyValueChanged(this.value,origValue);
		}
	}

	public T get() {
		changed=false;
		return this.value;
	}

	

	public Var(T value){
		this.value = value;
		hasValue = true;
	}

	public Var() {
	}

	@Override
	public String toString() {
	    if(value != null) {
	    	return value.toString();
	    }
	    return super.toString();
	}
}


/**
 * Restorable is a class which will restore its default value
 * when the GET() is invoked.
 *
 * @param <T>
 */
class RestorableVar<T> extends Var<T>{
	T defaultVal;
	public RestorableVar(T defaultVal) {
		super(defaultVal);
		this.defaultVal = defaultVal;
	}
	public T get() {
		T ret = this.value;
		this.value = defaultVal;
		return ret;
	}
}
