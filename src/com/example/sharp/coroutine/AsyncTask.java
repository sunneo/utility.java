package com.example.sharp.coroutine;

import com.example.android.IUiThreadRunner;
import com.example.events.WritableValue;
import com.example.sharp.BaseLinkedList;
import com.example.sharp.CString;
import com.example.sharp.Delegates;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncTask {
	public static interface ThisAction extends Delegates.Action1<AsyncTask>, Delegates.Action {

	}
	public static <T>  ThreadingFuture<T> runAsync(Delegates.Func<T> runnable) {
		return runAsync("",runnable);
	}
	public static class ThreadingFuture<T> implements Future<T> {
		protected String name;
		WritableValue<Object> value = new WritableValue<>();
		/**
		 * locker
		 */
		Object locker = new Object();
		/**
		 * queue to hold planed tasks (added by thenRun)
		 */
		BaseLinkedList<Runnable> runnableQueues = new BaseLinkedList<Runnable>();
		/**
		 * job thread
		 */
		Thread thread = null;
		volatile boolean started=false;
		private void initThread() {
			thread = new Thread(() -> {
				try {
					while(true) {
						Runnable job = getJob();
						if(job == null) break;
						job.run();
					}
				} catch (Throwable throwable) {

				}
			});
		}

		/**
		 * cancel job, this will clear all queued task
		 * @param mayInterruptIfRunning interrupt when thread is running
		 */
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			clearJobs();
			if (mayInterruptIfRunning) {
				if (thread != null && thread.isAlive() && !thread.isInterrupted()) {
					thread.interrupt();
				}
			}
			value = new WritableValue<>();

			return !thread.isAlive() || thread.isInterrupted();
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return false;
		}

		/**
		 * get a job from queue
		 * @return queued job or null
		 */
		private Runnable getJob() {
			Runnable ret=null;
			synchronized(locker) {
				if(runnableQueues.IsEmpty.get()) {
					return null;
				}
				ret = runnableQueues.RemoveFirst().Value;
			}
			return ret;
		}
		/**
		 * clear all job
		 */
		private void clearJobs() {
			synchronized(locker) {
				runnableQueues.Clear();
				runnableQueues = new BaseLinkedList<Runnable>();
			}
		}

		public <T2> ThreadingFuture<T2> thenRun(Delegates.Func1<T,T2> action) {

			synchronized (locker) {
				runnableQueues.AddLast(()->value.set(action.Invoke((T)value.get())));
			}
			if(!thread.isAlive() || !started) {
				initThread();
				start();
			}
			return (ThreadingFuture<T2>) this;
		}
		public ThreadingFuture<Void> thenRun(Runnable action) {
			synchronized (locker) {
				runnableQueues.AddLast(action);
			}
			if(!thread.isAlive() || !started) {
				initThread();
				start();
			}
			return (ThreadingFuture<Void>) this;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			if(!thread.isAlive() && !started) {
				initThread();
				start();
				thread.join();
			} else if(started){
				thread.join();
			}
			return (T)value.get();
		}

		@Override
		public T get(long l, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
			if(!thread.isAlive() && !started) {
				initThread();
				start();
				thread.join(timeUnit.toMillis(l));
			} else if(started){
				thread.join(timeUnit.toMillis(l));
			}
			return (T)value.get();
		}

		public ThreadingFuture(Delegates.Func<T> runnable) {
			this(runnable,"");
		}
		public ThreadingFuture(Delegates.Func<T> runnable, String name) {
			this.name = name;
			runnableQueues.AddLast(()->value.set(runnable.Invoke()));
			initThread();
		}

		public void start() {
			if(CString.IsNullOrEmpty(name)) {
				thread.setName("runAsync<T>"); //$NON-NLS-1$
			} else {
				thread.setName(name);
			}
			thread.start();
			started=true;
		}
	}
	public static <T> ThreadingFuture<T> runAsync(String name, Delegates.Func<T> runnable) {
		ThreadingFuture<T> future = new ThreadingFuture<T>(runnable,name);
		future.start();
		return future;
	}
	public class AfterFinishJobArgs {
		public IUiThreadRunner Ctrl;
		public Delegates.Action Action;

		public AfterFinishJobArgs(Delegates.Action r) {
			this.Action = r;
		}

		public AfterFinishJobArgs(IUiThreadRunner c, Delegates.Action r) {
			this.Ctrl = c;
			this.Action = r;
		}
	}

	public Hashtable<String, Object> DynamicFields = new Hashtable<String, Object>();

	Object AfterFinishJobLocker = new Object();
	private LinkedList<AfterFinishJobArgs> AfterFinishJob = new LinkedList<AfterFinishJobArgs>();
	private Object ownedJob = null;
	private boolean fromJobConstructor = false;
	public volatile boolean IsFault = false;

	public boolean IsAlive() {
		if (bgthread == null) {
			return false;
		}
		try {
			return bgthread.isAlive();
		} catch (Exception ee) {
			ee.printStackTrace();
			return false;
		}
	}

	private boolean IUiThreadRunnerInvoker(IUiThreadRunner ctrl, Object job) {
		if (job != null) {
			try {
				if (job instanceof ThisAction) {
					final ThisAction actionObj = (ThisAction) job;
					if (ctrl != null) {
						ctrl.runOnUiThread(() -> actionObj.Invoke(AsyncTask.this));
					} else {
						actionObj.Invoke(this);
					}
				} else if (job instanceof Delegates.Action) {
					final Delegates.Action actionObj = (Delegates.Action) job;
					if (ctrl != null) {
						ctrl.runOnUiThread(actionObj);
					} else {
						actionObj.Invoke();
					}

				}
			} catch (Exception ee) {
				ee.printStackTrace();
			}
			return true;
		}
		return false;
	}

	private boolean PollAndRunJobAfterFinishJob() {
		Object job = null;
		IUiThreadRunner ctrl = null;
		synchronized (AfterFinishJobLocker) {
			if (AfterFinishJob.size() > 0) {
				job = AfterFinishJob.getFirst().Action;
				ctrl = AfterFinishJob.getFirst().Ctrl;
				AfterFinishJob.removeFirst();
			}
		}

		return IUiThreadRunnerInvoker(ctrl, job);
	}

	private void runner() {
		try {
			IsFault = false;
			IsAvailable = false;
			if (fromJobConstructor) {
				if (ownedJob != null) {
					if (ownedJob instanceof ThisAction) {
						final ThisAction actionObj = (ThisAction) ownedJob;
						actionObj.Invoke(this);
					} else if (ownedJob instanceof Delegates.Action) {
						Delegates.Action actionObj = (Delegates.Action) ownedJob;
						actionObj.Invoke();
					}
				}
			} else {
				Run();
			}
			IsAvailable = true;
			while (PollAndRunJobAfterFinishJob())
				;
		} catch (Exception ee) {
			IsFault = true;
			FaultReason = ee;
			ee.printStackTrace();
		}
	}

	private void jobFlusher() {
		while (PollAndRunJobAfterFinishJob())
			;
	}

	private boolean AsyncFlushJob() {
		synchronized (AfterFinishJobLocker) {
			if (bgthread != null && bgthread.isAlive())
				return false; // do not rerun
			bgthread = new Thread(new Runnable() {
				@Override
				public void run() {
					jobFlusher();
					bgthread = null;
				}
			});
			if (!CString.IsNullOrEmpty(mName)) {
				bgthread.setName(mName);
			} else {
				bgthread.setName("AsyncThread");
			}

			bgthread.setPriority(Thread.MIN_PRIORITY);
			bgthread.start();
			return true;
		}
	}

	public boolean TimedWait(int millis) {
		if (bgthread != null && bgthread.isAlive()) {
			try {
				bgthread.join(millis);
				return !bgthread.isAlive();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public void Wait() {
		if (bgthread != null && bgthread.isAlive()) {
			try {
				bgthread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean Join(int timeMills) {
		if (bgthread == null || !bgthread.isAlive())
			return true;
		if (timeMills > 0) {
			try {
				bgthread.join(timeMills);
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				bgthread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		return !bgthread.isAlive();
	}

	private volatile Thread bgthread;

	private boolean StartAsync() {
		synchronized (AfterFinishJobLocker) {
			if (bgthread != null && bgthread.isAlive())
				return false; // do not rerun
			bgthread = new Thread(new Runnable() {
				@Override
				public void run() {
					runner();
				}
			});
			if (!CString.IsNullOrEmpty(mName)) {
				bgthread.setName(mName);
			} else {
				bgthread.setName("AsyncThread");
			}
			bgthread.setPriority(Thread.MIN_PRIORITY);
			bgthread.start();
			return true;
		}
	}

	// =============================================================
	protected void Run() {

	}

	private String mName = "";

	public AsyncTask() {

	}

	public void SetName(String name) {
		mName = name;
	}

	public AsyncTask(Delegates.Action j) {
		ownedJob = j;
		fromJobConstructor = true;
	}

	public Exception FaultReason = null;
	public volatile boolean IsAvailable = false;

	/// <summary>
	/// start running
	/// </summary>
	/// <param name="bsynchronizeding">when bsynchronizeding instanceof true, caller
	/// would bsynchronized until job finished</param>
	public void Start(boolean bsynchronizeding) {
		if (bsynchronizeding) {
			runner();
		} else {
			StartAsync();
		}
	}

	public void Start() {
		this.Start(true);
	}

	/// <summary>
	/// wait until job and its additional job finish
	/// </summary>
	/// <param name="bsynchronizeding">when bsynchronizeding instanceof true, caller
	/// would bsynchronized until job finished</param>
	/// <returns></returns>
	public boolean FlushJob(boolean bsynchronizeding) {
		if (bsynchronizeding) {
			jobFlusher();
			return true;
		} else {
			return AsyncFlushJob();
		}
	}

	public boolean FlushJob() {
		return FlushJob(true);
	}

	/// <summary>
	/// it async thread was running, terminate it.
	/// </summary>
	public void StopAsync() {
		if (bgthread != null && bgthread.isAlive()) {
			bgthread.stop();
			bgthread = null;
		}
	}

	/// <summary>
	/// add additional job into running thread
	/// </summary>
	/// <param name="l">an action job which contains statements for running</param>
	public void AddAfterFinishJob(Delegates.Action l) {
		synchronized (AfterFinishJobLocker) {
			AfterFinishJob.addLast(new AfterFinishJobArgs(l));
		}
	}

	/// <summary>
	/// add additional job into running thread
	/// </summary>
	/// <param name="l">an action job which contains statements for running</param>
	public void AddAfterFinishJob(ThisAction l) {
		synchronized (AfterFinishJobLocker) {
			AfterFinishJob.addLast(new AfterFinishJobArgs(l));
		}
	}

	/// <summary>
	/// add additional job into running thread
	/// </summary>
	/// <param name="l">an action job which contains statements for running</param>
	public void AddAfterFinishJob(IUiThreadRunner c, ThisAction l) {
		synchronized (AfterFinishJobLocker) {
			AfterFinishJob.addLast(new AfterFinishJobArgs(c, l));
		}
	}

	public void Dispose() {
		StopAsync();
		ClearJob();
		StopAsync();
		IsFault = false;
	}

	public void ClearJob() {
		synchronized (AfterFinishJobLocker) {
			AfterFinishJob.clear();
		}
	}

}
