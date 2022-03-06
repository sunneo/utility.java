package com.example.events;

/**
 * base var type which provide raw event handler for chaining every changes.
 * this class is aimed at providing non-generic template to notify/invoke event
 * handler within generic templates at once.
 */
public class BaseVar <T> extends WritableValue<T>{
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
	/**
	 * arguments for handling var chaining.
	 */
	@SuppressWarnings("rawtypes")
	public static class ChainActionArgs extends INotificationEventArgs {

		public BaseVar sender;
		
		public BaseVar[] others;
	}
	/**
	 * chain availability of Vars
	 *
	 * @param action action triggered when chained variables have value.
	 *               (dependencies satisfied)
	 * @param vars   dependencies.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void chain(Var<? extends Object> pthis, ChainAction action, BaseVar... vars) {
		
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
	/**
	 * action for handling chaining.
	 */
	public static interface ChainAction {
		void run(ChainActionArgs args);
	}
	@Override
	protected void finalize() throws Throwable {
		this.dispose();
	}
}
