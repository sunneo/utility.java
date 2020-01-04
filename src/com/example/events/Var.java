package com.example.events;

/**
 * a variable class which provides onValueChange event.
 * 
 *
 * @param <T> variable type.
 */
public class Var<T extends Object> extends BaseVar {
	protected T value;

	/**
	 * arguments for handling var chaining.
	 *
	 */
	public static class ChainActionArgs extends INotificationEventArgs {
		public BaseVar sender;
		public BaseVar[] others;
	}

	/**
	 * action for handling chaining.
	 */
	public static interface ChainAction {
		public void run(ChainActionArgs args);
	}
	

	public EventDelegate<INotification<ValueChangedEventArgs<T>>> onValueChanged = new EventDelegate<>();
	
	
	/**
	 * notify value has changed.
	 */
	protected void notifyValueChanged() {
		if (!onValueChanged.isEmpty()) {
			ValueChangedEventArgs<T> args = new ValueChangedEventArgs<>(value);
			this.onValueChanged.invoke(this, args);
		}
		notifyChanged();
	}

	/**
	 * set value, trigger ValueChanged event.
	 * 
	 * @param value given value
	 * @return
	 */
	public void set(T value) {
		
		if(notifyOnlyOnChanged) {
			if(!this.value.equals(value)) {
				changed=true;
			}
		}
		else {
			changed=true;	
		}
		this.value = value;
		this.hasValue=true;
		if (changed) {
			notifyValueChanged();
		}
	}

	public T get() {
		changed=false;
		return this.value;
	}

	/**
	 * chain availability of Vars
	 * 
	 * @param action action triggered when chained variables have value.
	 *               (dependencies satisfied)
	 * @param vars   dependencies.
	 */
	public static void chain(Var<? extends Object> pthis, ChainAction action, BaseVar... vars) {
		@SuppressWarnings("rawtypes")
		INotification handler = new INotification() {
			@Override
			public void perform(Object from, INotificationEventArgs e) {
				if (vars.length > 0) {
					boolean hasValue = true;
					for (BaseVar other : vars) {
						if (!other.hasValue) {
							hasValue = false;
							break;
						}
					}
					if (hasValue && action != null) {
						ChainActionArgs args = new ChainActionArgs();
						args.sender = pthis;
						args.others = vars;
						action.run(args);
					}
				}
			}
		};
		for (BaseVar var : vars) {
			var.onChanged.addDelegate(handler);
		}

	}
	
	
	public Var(T value){
		this.value = value;
		this.hasValue = true;
	}
	public Var() {
		
	}
	public String toString() {
	    if(value != null) {
	    	return value.toString();
	    }
	    return super.toString();
	}
	
}
