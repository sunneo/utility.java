package com.example.events;


import java.util.HashMap;
import java.util.Vector;

import com.example.sharp.Delegates;


/**
 * EventDelegate for implementing observer pattern. event delegate provides add,
 * remove invoke, clear operations for registry customized event handlers.
 *
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class EventDelegate<T extends INotification> {
	public static interface Handler1<T1> extends INotification<INotificationEventArgs.INotificationEventArg1<T1>>{}
	public static interface Handler2<T1,T2> extends INotification<INotificationEventArgs.INotificationEventArg2<T1,T2>>{}
	public static interface Handler3<T1,T2,T3> extends INotification<INotificationEventArgs.INotificationEventArg3<T1,T2,T3>>{}
	public static interface Handler4<T1,T2,T3,T4> extends INotification<INotificationEventArgs.INotificationEventArg4<T1,T2,T3,T4>>{}
	public static interface Handler5<T1,T2,T3,T4,T5> extends INotification<INotificationEventArgs.INotificationEventArg5<T1,T2,T3,T4,T5>>{}
	public static interface Handler6<T1,T2,T3,T4,T5,T6> extends INotification<INotificationEventArgs.INotificationEventArg6<T1,T2,T3,T4,T5,T6>>{}
	public static interface Handler7<T1,T2,T3,T4,T5,T6,T7> extends INotification<INotificationEventArgs.INotificationEventArg7<T1,T2,T3,T4,T5,T6,T7>>{}
	public static interface Handler8<T1,T2,T3,T4,T5,T6,T7,T8> extends INotification<INotificationEventArgs.INotificationEventArg8<T1,T2,T3,T4,T5,T6,T7,T8>>{}
	
	public static class Event1<T1> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<T1>>>{}
	public static class Event2<T1,T2> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg2<T1,T2>>>{}
	public static class Event3<T1,T2,T3> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg3<T1,T2,T3>>>{}
	public static class Event4<T1,T2,T3,T4> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg4<T1,T2,T3,T4>>>{}
	public static class Event5<T1,T2,T3,T4,T5> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg5<T1,T2,T3,T4,T5>>>{}
	public static class Event6<T1,T2,T3,T4,T5,T6> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg6<T1,T2,T3,T4,T5,T6>>>{}
	public static class Event7<T1,T2,T3,T4,T5,T6,T7> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg7<T1,T2,T3,T4,T5,T6,T7>>>{}
	public static class Event8<T1,T2,T3,T4,T5,T6,T7,T8> extends EventDelegate<INotification<INotificationEventArgs.INotificationEventArg8<T1,T2,T3,T4,T5,T6,T7,T8>>>{}
	
	
	
	
	public static <T1> Event1<T1> create(Class<T1> clz){
		return new Event1<T1>();
	}
	public static <T1,T2> Event2<T1,T2> create(Class<T1> clz1,Class<T2> clz2){
		return new Event2<T1,T2>();
	}
	public static <T1,T2,T3> Event3<T1,T2,T3> create(Class<T1> clz1,Class<T2> clz2,Class<T3> clz3){
		return new Event3<T1,T2,T3>();
	}
	public static <T1,T2,T3,T4> Event4<T1,T2,T3,T4> create(Class<T1> clz1,Class<T2> clz2,Class<T3> clz3,Class<T4> clz4){
		return new Event4<T1,T2,T3,T4>();
	}
	public static <T1,T2,T3,T4,T5> Event5<T1,T2,T3,T4,T5> create(Class<T1> clz1,Class<T2> clz2,Class<T3> clz3,Class<T4> clz4,Class<T5> clz5){
		return new Event5<T1,T2,T3,T4,T5>();
	}
	public static <T1,T2,T3,T4,T5,T6> Event6<T1,T2,T3,T4,T5,T6> create(Class<T1> clz1,Class<T2> clz2,Class<T3> clz3,Class<T4> clz4,Class<T5> clz5,Class<T6> clz6){
		return new Event6<T1,T2,T3,T4,T5,T6>();
	}
	public static <T1,T2,T3,T4,T5,T6,T7> Event7<T1,T2,T3,T4,T5,T6,T7> create(Class<T1> clz1,Class<T2> clz2,Class<T3> clz3,Class<T4> clz4,Class<T5> clz5,Class<T6> clz6,Class<T7> clz7){
		return new Event7<T1,T2,T3,T4,T5,T6,T7>();
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Event8<T1,T2,T3,T4,T5,T6,T7,T8> create(Class<T1> clz1,Class<T2> clz2,Class<T3> clz3,Class<T4> clz4,Class<T5> clz5,Class<T6> clz6,Class<T7> clz7,Class<T8> clz8){
		return new Event8<T1,T2,T3,T4,T5,T6,T7,T8>();
	}
	
	public static <T1> INotificationEventArgs.INotificationEventArg1<T1> args(T1 v1){
		return new INotificationEventArgs.INotificationEventArg1<T1>(v1);
	}
	public static <T1,T2> INotificationEventArgs.INotificationEventArg2<T1,T2> args(T1 v1, T2 v2){
		return new INotificationEventArgs.INotificationEventArg2<T1,T2>(v1,v2);
	}
	public static <T1,T2,T3> INotificationEventArgs.INotificationEventArg3<T1,T2,T3> args(T1 v1, T2 v2,T3 v3){
		return new INotificationEventArgs.INotificationEventArg3<T1,T2,T3>(v1,v2,v3);
	}
	public static <T1,T2,T3,T4> INotificationEventArgs.INotificationEventArg4<T1,T2,T3,T4> args(T1 v1, T2 v2,T3 v3,T4 v4){
		return new INotificationEventArgs.INotificationEventArg4<T1,T2,T3,T4>(v1,v2,v3,v4);
	}
	public static <T1,T2,T3,T4,T5> INotificationEventArgs.INotificationEventArg5<T1,T2,T3,T4,T5> args(T1 v1, T2 v2,T3 v3,T4 v4,T5 v5){
		return new INotificationEventArgs.INotificationEventArg5<T1,T2,T3,T4,T5>(v1,v2,v3,v4,v5);
	}
	public static <T1,T2,T3,T4,T5,T6> INotificationEventArgs.INotificationEventArg6<T1,T2,T3,T4,T5,T6> args(T1 v1, T2 v2,T3 v3,T4 v4,T5 v5,T6 v6){
		return new INotificationEventArgs.INotificationEventArg6<T1,T2,T3,T4,T5,T6>(v1,v2,v3,v4,v5,v6);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> INotificationEventArgs.INotificationEventArg7<T1,T2,T3,T4,T5,T6,T7> args(T1 v1, T2 v2,T3 v3,T4 v4,T5 v5,T6 v6,T7 v7){
		return new INotificationEventArgs.INotificationEventArg7<T1,T2,T3,T4,T5,T6,T7>(v1,v2,v3,v4,v5,v6,v7);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> INotificationEventArgs.INotificationEventArg8<T1,T2,T3,T4,T5,T6,T7,T8> args(T1 v1, T2 v2,T3 v3,T4 v4,T5 v5,T6 v6,T7 v7,T8 v8){
		return new INotificationEventArgs.INotificationEventArg8<T1,T2,T3,T4,T5,T6,T7,T8>(v1,v2,v3,v4,v5,v6,v7,v8);
	}
    Vector<T> invocationList = new Vector<>();
    HashMap<T, T> removedAfterInvoke = new HashMap<>();
    EventDelegate me;
    public EventDelegate(){
    	me = this;
    }
    boolean invoking=false;
    public void resetInvoking() {
    	invoking=false;
    }
	/**
	 * test whether no listener
	 * 
	 * @return true if listener is empty
	 */
    public synchronized boolean isEmpty() {
    	return invocationList.isEmpty();
    }
    
	/**
	 * get all invocation list. DO NOT DIRECTLY USE IT, USE addDelegate and invoke
	 * instead.
	 * 
	 * @return invocation list
	 */
    public synchronized  Vector<T> getInvocationList(){
        return invocationList;
    }

	/**
	 * register EventHandler to a event.
	 * 
	 * @param t EventHandler
	 * @return assigned event handler
	 */
    public synchronized T addDelegate(T t){
        return addDelegate(t,false);
    }
	/**
	 * register EventHandler to a event.
	 * 
	 * @param t EventHandler
	 * @return assigned event handler
	 */
    public synchronized T addDelegate(T t, boolean removeAfterInvoke){
    	invocationList.add(t);
    	if(removeAfterInvoke) {
            removedAfterInvoke.put(t, t);
    	}
        return t;
    }
    
	/**
	 * remove EventHandler from a event.
	 * 
	 * @param t event handler
	 * @return true if remove successfully.
	 */
    public synchronized boolean removeDelegate(T t){
    	if(this.invocationList==null) return false;
        return invocationList.remove(t);
    }

	/**
	 * clear all EventHandler
	 */
    public synchronized void clear(){
    	invocationList.clear();
		invocationList = new Vector<>();
    }

	/**
	 * broadcast/notify/trigger all registered EventHandler.
	 * 
	 * @param sender Sender.
	 * @param args   Argument for event.
	 */
	@SuppressWarnings("unchecked")
	public void invoke(Object sender, INotificationEventArgs args) {
		if(invoking) {
			// prevent recursive
			return;
		}
		invoking=true;
        Vector<T> clone = getInvocationList();
    	try {
    		Vector<T> del = new Vector<>();
	        for(int i=0; i<clone.size(); ++i){	        
	            T t = clone.get(i);
	            t.perform(sender,args);
	            if(removedAfterInvoke.size()>0 && removedAfterInvoke.containsKey(t)) {
	            	del.add(t);
	            }
	        }
	        for(T t:del) {
	        	removeDelegate(t);
	        }
    	}catch(Exception ee) {
    		ee.printStackTrace();
    	}
    	invoking=false;
    }
	/**
	 * broadcast/notify/trigger all registered EventHandler.
	 * 
	 * @param sender Sender.
	 * @param args   Arguments for event.
	 */
    public void invoke(Object sender,Object... args){
        INotificationEventArgs arglist = new INotificationEventArgs();
        arglist.object = args;
        this.invoke(sender,arglist);
    }
    public <T1> void invoke(Object sender, T1 arg1 ) {
        this.invoke(sender, args(arg1));
    }
    public <T1,T2> void invoke(Object sender, T1 arg1, T2 arg2 ) {
        this.invoke(sender, args(arg1,arg2));
    }
    public <T1,T2,T3> void invoke(Object sender, T1 arg1, T2 arg2, T3 arg3 ) {
        this.invoke(sender, args(arg1,arg2,arg3));
    }
    public <T1,T2,T3,T4> void invoke(Object sender, T1 arg1, T2 arg2, T3 arg3, T4 arg4 ) {
        this.invoke(sender, args(arg1,arg2,arg3,arg4));
    }
    public <T1,T2,T3,T4,T5> void invoke(Object sender, T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5 ) {
        this.invoke(sender, args(arg1,arg2,arg3,arg4,arg5));
    }
    public <T1,T2,T3,T4,T5,T6> void invoke(Object sender, T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5 , T6 arg6) {
        this.invoke(sender, args(arg1,arg2,arg3,arg4,arg5,arg6));
    }
    public <T1,T2,T3,T4,T5,T6,T7> void invoke(Object sender, T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5 , T6 arg6, T7 arg7) {
        this.invoke(sender, args(arg1,arg2,arg3,arg4,arg5,arg6,arg7));
    }
    public <T1,T2,T3,T4,T5,T6,T7,T8> void invoke(Object sender, T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5 , T6 arg6, T7 arg7, T8 arg8) {
        this.invoke(sender, args(arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8));
    }
    
    Delegates.Action onDisposed=null;
    
    public void dispose() {
    	if(onDisposed!=null) {
    		try {
    		   this.clear();
    		   invocationList = null;
    		   onDisposed.Invoke();
    		   onDisposed=null;
    		}catch(Exception ee) {
    			ee.printStackTrace();
    		}
    	}
    }
   
    /**
     * Hook/route event handler
     * @return
     */
	@SuppressWarnings("unchecked")
	public EventDelegate<T> route(){
		
		final EventDelegate<T> ret = new EventDelegate<T>();
		
		final T notification = (T) new INotification() {

			@Override
			public void perform(Object from, INotificationEventArgs args) {
				ret.invoke(from, args);
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate(notification);
		return ret;
	}
	
	/////////////////////////////////////////////////
	//
	//  following code is for simplify parameterized route
	//  use codegen to maintain them, no need to directly modify
	//
	@SuppressWarnings("unchecked")
	public <T1> Event1<T1> route1(){

		final Event1<T1> ret = new Event1<T1>();
		
		final Handler1<T1> notification= new Handler1<T1>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg1<T1> args) {
				ret.invoke(from, args.get_1());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	@SuppressWarnings("unchecked")
	public <T1,T2> Event2<T1,T2> route2(){

		final Event2<T1,T2> ret = new Event2<T1,T2>();
		
		final Handler2<T1,T2> notification= new Handler2<T1,T2>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg2<T1,T2> args) {
				ret.invoke(from, args.get_1(),args.get_2());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	@SuppressWarnings("unchecked")
	public <T1,T2,T3> Event3<T1,T2,T3> route3(){

		final Event3<T1,T2,T3> ret = new Event3<T1,T2,T3>();
		
		final Handler3<T1,T2,T3> notification= new Handler3<T1,T2,T3>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg3<T1,T2,T3> args) {
				ret.invoke(from, args.get_1(),args.get_2(),args.get_3());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public <T1,T2,T3,T4> Event4<T1,T2,T3,T4> route4(){

		final Event4<T1,T2,T3,T4> ret = new Event4<T1,T2,T3,T4>();
		
		final Handler4<T1,T2,T3,T4> notification= new Handler4<T1,T2,T3,T4>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg4<T1,T2,T3,T4> args) {
				ret.invoke(from, args.get_1(),args.get_2(),args.get_3(),args.get_4());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	@SuppressWarnings("unchecked")
	public <T1,T2,T3,T4,T5> Event5<T1,T2,T3,T4,T5> route5(){

		final Event5<T1,T2,T3,T4,T5> ret = new Event5<T1,T2,T3,T4,T5>();
		
		final Handler5<T1,T2,T3,T4,T5> notification= new Handler5<T1,T2,T3,T4,T5>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg5<T1,T2,T3,T4,T5> args) {
				ret.invoke(from, args.get_1(),args.get_2(),args.get_3(),args.get_4(),args.get_5());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	@SuppressWarnings("unchecked")
	public <T1,T2,T3,T4,T5,T6> Event6<T1,T2,T3,T4,T5,T6> route6(){

		final Event6<T1,T2,T3,T4,T5,T6> ret = new Event6<T1,T2,T3,T4,T5,T6>();
		
		final Handler6<T1,T2,T3,T4,T5,T6> notification= new Handler6<T1,T2,T3,T4,T5,T6>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg6<T1,T2,T3,T4,T5,T6> args) {
				ret.invoke(from, args.get_1(),args.get_2(),args.get_3(),args.get_4(),args.get_5(),args.get_6());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	@SuppressWarnings("unchecked")
	public <T1,T2,T3,T4,T5,T6,T7> Event7<T1,T2,T3,T4,T5,T6,T7> route7(){

		final Event7<T1,T2,T3,T4,T5,T6,T7> ret = new Event7<T1,T2,T3,T4,T5,T6,T7>();
		
		final Handler7<T1,T2,T3,T4,T5,T6,T7> notification= new Handler7<T1,T2,T3,T4,T5,T6,T7>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg7<T1,T2,T3,T4,T5,T6,T7> args) {
				ret.invoke(from, args.get_1(),args.get_2(),args.get_3(),args.get_4(),args.get_5(),args.get_6(),args.get_7());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	@SuppressWarnings("unchecked")
	public <T1,T2,T3,T4,T5,T6,T7,T8> Event8<T1,T2,T3,T4,T5,T6,T7,T8> route8(){

		final Event8<T1,T2,T3,T4,T5,T6,T7,T8> ret = new Event8<T1,T2,T3,T4,T5,T6,T7,T8>();
		
		final Handler8<T1,T2,T3,T4,T5,T6,T7,T8> notification= new Handler8<T1,T2,T3,T4,T5,T6,T7,T8>() {

			@Override
			public void perform(Object from, INotificationEventArgs.INotificationEventArg8<T1,T2,T3,T4,T5,T6,T7,T8> args) {
				ret.invoke(from, args.get_1(),args.get_2(),args.get_3(),args.get_4(),args.get_5(),args.get_6(),args.get_7(),args.get_8());
			}
			
		};
		ret.onDisposed=new Delegates.Action() {
			
			@Override
			public void Invoke() {
				me.removeDelegate(notification);
			}
		};
		this.addDelegate((T) notification);
		return ret;
	}
	@Override
	protected void finalize() throws Throwable {
		this.dispose();
		super.finalize();
	}
}