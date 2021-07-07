package com.example.events;

/**
 * base var type which provide raw event handler for chaining every changes.
 * this class is aimed at providing non-generic template to notify/invoke event
 * handler within generic templates at once.
 */

/**
 * base var type which provide raw event handler for chaining every changes.
 * this class is aimed at providing non-generic template to notify/invoke event
 * handler within generic templates at once.
 */
public class BaseVar {
	public boolean hasValue;
	public boolean changed;
	/**
	 * set this flag to make Var notify only when value change
	 * rather than every single set() invocation
	 */
	public boolean notifyOnlyOnChanged;
	/**
	 * set this flag when notification to initial value should be skipped 
	 */
	public boolean skipInitialValueNotification = false;

	@SuppressWarnings("rawtypes")
	public EventDelegate<INotification> onChanged = new EventDelegate<>();
	public void notifyChanged() {
		if(this.onChanged != null)
			onChanged.invoke(this, this);
	}
	public void dispose() {
		if(onChanged != null) {
			onChanged.dispose();
		}
		onChanged = null;
	}
	@Override
	protected void finalize() throws Throwable {
		this.dispose();
		super.finalize();
	}
}
