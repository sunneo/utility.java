package com.example;

import java.util.List;
import java.util.Vector;

import com.example.sharp.Delegates.Action1;
import com.example.sharp.Delegates.Action2;
import com.example.sharp.Delegates.Action3;
import com.example.sharp.coroutine.CancellationTokenSource;

public class Parallelx {
	public static enum ParallelForScheduler {
		Blocked, Cyclic, RuntimeLoadBalance
	}

	private void WaitForAsyncTasks(Vector<AsyncTask> tasks, boolean wait) {
		if (wait) {
			for (Integer i = 0; i < tasks.size(); ++i) {
				tasks.get(i).Join(0);
			}
		}
	}

	public static Integer Concurrency = Runtime.getRuntime().availableProcessors();

	public Vector<AsyncTask> RuntimeLoadBalanceFor(Integer lowerBound, Integer upperBound,
			Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource,
			boolean wait) {
		Vector<AsyncTask> ret = new Vector<AsyncTask>();
		Object locker = new Object();
		final Locked<Integer> sharedIdx = new Locked<>(lowerBound);
		Integer threadCount = Concurrency;
		for (Integer i = 0; i < threadCount; ++i) {
			final Integer _i = i;
			AsyncTask task = new AsyncTask(() ->

			{
				Integer load = lowerBound;
				while (true) {
					if (cancellationSource != null && cancellationSource.IsCancellationRequested) {
						// cancelled
						break;
					}
					// fetch next load
					synchronized (locker) {
						load = sharedIdx.get();
						sharedIdx.set(load + 1);
					}
					if (load >= upperBound) {
						break;
					}
					// consume load
					action.Invoke(load, cancellationSource, _i);
				}
			});
			task.Start(false);
			ret.add(task);
		}
		WaitForAsyncTasks(ret, wait);
		return ret;
	}

	private void BlockedParitionRunner(Integer start, Integer end,
			Action3<Integer, CancellationTokenSource, Integer> action, Integer threadid,
			CancellationTokenSource cancellationSource) {
		for (Integer i = start; i < end; ++i) {
			if (cancellationSource != null && cancellationSource.IsCancellationRequested) {
				break;
			}
			action.Invoke(i, cancellationSource, threadid);
		}
	}

	private void CyclicParitionRunner(Integer start, Integer end, Integer step,
			Action3<Integer, CancellationTokenSource, Integer> action, Integer threadid,
			CancellationTokenSource cancellationSource) {
		for (Integer i = start; i < end; i += step) {
			if (cancellationSource != null && cancellationSource.IsCancellationRequested) {
				break;
			}
			action.Invoke(i, cancellationSource, threadid);
		}
	}

	public Vector<AsyncTask> CyclicPartitionFor(Integer lowerBound, Integer upperBound,
			Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource,
			boolean wait) {
		Integer len = upperBound - lowerBound + 1;
		Integer threadCount = Concurrency;
		Integer partLen = len / threadCount;
		if (len % threadCount > 0) {
			partLen += 1;
		}
		Vector<AsyncTask> ret = new Vector<AsyncTask>();
		for (Integer i = 0; i < threadCount; ++i) {
			Integer start = lowerBound + i;
			Integer end = upperBound;
			AsyncTask.ThisAction actionTask = new AsyncTask.ThisAction() {
				public void Invoke(AsyncTask task) {
					Integer id = (Integer) task.DynamicFields.get("ID");
					CyclicParitionRunner(start, end, threadCount, action, id, cancellationSource);
				}

				@Override
				public void Invoke() {

				}
			};
			AsyncTask thread = new AsyncTask(actionTask);
			thread.DynamicFields.put("ID", i);
			thread.Start(false);
			ret.add(thread);
		}
		WaitForAsyncTasks(ret, wait);
		return ret;
	}

	public Vector<AsyncTask> BlockedPartitionFor(Integer lowerBound, Integer upperBound,
			final Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource,
			boolean wait) {
		Integer len = upperBound - lowerBound + 1;
		Integer threadCount = Concurrency;
		Integer partLen = len / threadCount;
		if (len % threadCount > 0) {
			partLen += 1;
		}
		Vector<AsyncTask> ret = new Vector<AsyncTask>();
		for (Integer i = 0; i < threadCount; ++i) {
			Integer start = lowerBound + i * partLen;
			Integer end = start + partLen;
			if (end > upperBound) {
				end = upperBound;
			}
			final Integer _start = start;
			final Integer _end = start;
			AsyncTask.ThisAction actionTask = new AsyncTask.ThisAction() {
				public void Invoke(AsyncTask task) {
					Integer id = (Integer) task.DynamicFields.get("ID");
					BlockedParitionRunner(_start, _end, action, id, cancellationSource);
				}

				@Override
				public void Invoke() {

				}
			};
			AsyncTask thread = new AsyncTask(actionTask);
			thread.DynamicFields.put("ID", i);
			thread.Start(false);
			ret.add(thread);
		}
		WaitForAsyncTasks(ret, wait);
		return ret;
	}

	public Vector<AsyncTask> BlockedPartitionFor(Integer lowerBound, Integer upperBound,
			final Action2<Integer, CancellationTokenSource> action, boolean wait) {
		Action3<Integer, CancellationTokenSource, Integer> actionProxy = (Integer i, CancellationTokenSource dummy,
				Integer arg3) -> {
			action.Invoke(i, dummy);
		};

		return BlockedPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
	}

	public Vector<AsyncTask> CyclicPartitionFor(Integer lowerBound, Integer upperBound,
			Action2<Integer, CancellationTokenSource> action, boolean wait) {
		Action3<Integer, CancellationTokenSource, Integer> actionProxy = (Integer i, CancellationTokenSource dummy,
				Integer arg3) -> {
			action.Invoke(i, dummy);
		};
		return CyclicPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
	}

	public Vector<AsyncTask> BlockedPartitionFor(Integer lowerBound, Integer upperBound, final Action1<Integer> action,
			boolean wait) {
		Action3<Integer, CancellationTokenSource, Integer> actionProxy = (Integer i, CancellationTokenSource dummy,
				Integer arg3) -> {
			action.Invoke(i);

		};
		return BlockedPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
	}

	public Vector<AsyncTask> BlockedPartitionFor(Integer upperBound, Action1<Integer> action, boolean wait) {
		return BlockedPartitionFor(0, upperBound, action, wait);
	}

	public Vector<AsyncTask> CyclicPartitionFor(Integer upperBound,
			final Action2<Integer, CancellationTokenSource> action, boolean wait) {
		return CyclicPartitionFor(0, upperBound, action, wait);
	}

	public Vector<AsyncTask> CyclicPartitionFor(Integer upperBound, Action1<Integer> action, boolean wait) {
		return CyclicPartitionFor(0, upperBound, action, wait);
	}

	public Vector<AsyncTask> CyclicPartitionFor(Integer lowerBound, Integer upperBound, Action1<Integer> action,
			boolean wait) {
		Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer, CancellationTokenSource, Integer>() {
			public void Invoke(Integer i, CancellationTokenSource dummy, Integer arg3) {
				action.Invoke(i);
			}
		};
		return CyclicPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
	}

	public Vector<AsyncTask> RuntimeLoadBalanceFor(Integer lowerBound, Integer upperBound, Action1<Integer> action,
			boolean wait) {
		Action3<Integer, CancellationTokenSource, Integer> actionProxy = (Integer i, CancellationTokenSource dummy,
				Integer arg3) -> {
			action.Invoke(i);

		};
		return RuntimeLoadBalanceFor(lowerBound, upperBound, actionProxy, null, wait);
	}

	public Vector<AsyncTask> RuntimeLoadBalanceFor(Integer upperBound, Action1<Integer> action, boolean wait) {
		return RuntimeLoadBalanceFor(0, upperBound, action, wait);
	}

	public static Vector<AsyncTask> For(Integer lowerbound, Integer upperBound,
			Action3<Integer, CancellationTokenSource, Integer> action, boolean wait, ParallelForScheduler scheduler) {
		switch (scheduler) {
		default:
		case Blocked:
			return new Parallelx().BlockedPartitionFor(lowerbound, upperBound, action, new CancellationTokenSource(),
					wait);
		case Cyclic:
			return new Parallelx().CyclicPartitionFor(lowerbound, upperBound, action, new CancellationTokenSource(),
					wait);
		case RuntimeLoadBalance:
			return new Parallelx().RuntimeLoadBalanceFor(lowerbound, upperBound, action, new CancellationTokenSource(),
					wait);
		}

	}

	public static Vector<AsyncTask> For(Integer lowerbound, Integer upperBound,
			Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource,
			boolean wait, ParallelForScheduler scheduler) {
		switch (scheduler) {
		default:
		case Blocked:
			return new Parallelx().BlockedPartitionFor(lowerbound, upperBound, action, cancellationSource, wait);
		case Cyclic:
			return new Parallelx().CyclicPartitionFor(lowerbound, upperBound, action, cancellationSource, wait);
		case RuntimeLoadBalance:
			return new Parallelx().RuntimeLoadBalanceFor(lowerbound, upperBound, action, cancellationSource, wait);
		}
	}

	public static Vector<AsyncTask> For(Integer lowerbound, Integer upperBound, Action1<Integer> action, boolean wait,
			ParallelForScheduler scheduler) {
		switch (scheduler) {
		default:
		case Blocked:
			return new Parallelx().BlockedPartitionFor(lowerbound, upperBound, action, wait);
		case Cyclic:
			return new Parallelx().CyclicPartitionFor(lowerbound, upperBound, action, wait);
		case RuntimeLoadBalance:
			return new Parallelx().RuntimeLoadBalanceFor(lowerbound, upperBound, action, wait);
		}
	}

	public static Vector<AsyncTask> For(Integer upperBound, Action1<Integer> action, boolean wait,
			ParallelForScheduler scheduler) {
		switch (scheduler) {
		default:
		case Blocked:
			return new Parallelx().BlockedPartitionFor(upperBound, action, wait);
		case Cyclic:
			return new Parallelx().CyclicPartitionFor(upperBound, action, wait);
		case RuntimeLoadBalance:
			return new Parallelx().RuntimeLoadBalanceFor(upperBound, action, wait);
		}

	}

	public static <T> Vector<AsyncTask> Foreach(final List<T> upperBound, final Action1<T> action, boolean wait,
			ParallelForScheduler scheduler) {
		Action1<Integer> actionProxy = (i) -> {
			action.Invoke(upperBound.get(i));
		};
		switch (scheduler) {
		default:
		case Blocked:
			return new Parallelx().BlockedPartitionFor(upperBound.size(), actionProxy, wait);
		case Cyclic:
			return new Parallelx().CyclicPartitionFor(upperBound.size(), actionProxy, wait);
		case RuntimeLoadBalance:
			return new Parallelx().RuntimeLoadBalanceFor(upperBound.size(), actionProxy, wait);
		}
	}

	public static Vector<AsyncTask> Foreach(List upperBound, final Action2<Object, Integer> action, boolean wait,
			ParallelForScheduler scheduler) {

		Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer, CancellationTokenSource, Integer>() {
			public void Invoke(Integer i, CancellationTokenSource dummy, Integer id) {
				action.Invoke(upperBound.get(i), id);
			}
		};
		switch (scheduler) {
		default:
		case Blocked:
			return new Parallelx().BlockedPartitionFor(0, upperBound.size(), actionProxy, null, wait);
		case Cyclic:
			return new Parallelx().CyclicPartitionFor(0, upperBound.size(), actionProxy, null, wait);
		case RuntimeLoadBalance:
			return new Parallelx().RuntimeLoadBalanceFor(0, upperBound.size(), actionProxy, null, wait);
		}
	}

}
