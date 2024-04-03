package com.example.sharp.coroutine;

import com.example.android.IUiThreadRunner;
import com.example.sharp.CString;
import com.example.sharp.Delegates;

import java.util.Hashtable;
import java.util.LinkedList;

public class AsyncTask {
	public static interface ThisAction extends Delegates.Action1<AsyncTask>, Delegates.Action {

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
