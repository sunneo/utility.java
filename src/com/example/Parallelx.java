package com.example;

import com.example.csharp.actions.Action;
import com.example.csharp.actions.Action2;
import com.example.csharp.actions.Action3;
import com.example.csharp.coroutine.CancellationTokenSource;

import java.util.List;
import java.util.Vector;

public class Parallelx
{
    public static enum ParallelForScheduler
    {
        Blocked ,
        Cyclic,
        RuntimeLoadBalance
    }
    private void WaitForAsyncTasks(Vector<AsyncTask> tasks, boolean wait)
    {
        if (wait)
        {
            for (Integer i = 0; i < tasks.size(); ++i)
            {
                tasks.get(i).Join(0);
            }
        }
    }
    public static Integer Concurrency = Runtime.getRuntime().availableProcessors();
    public Vector<AsyncTask> RuntimeLoadBalanceFor(Integer lowerBound, Integer upperBound, Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource, boolean wait)
    {
        Vector<AsyncTask> ret = new Vector<AsyncTask>();
        Object locker = new Object();
        final Locked<Integer> sharedIdx = new Locked<>(lowerBound);
        Integer threadCount = Concurrency;
        for (Integer i = 0; i < threadCount; ++i)
        {
            final Integer _i = i;
            AsyncTask task = new AsyncTask(new Action() {


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
                            sharedIdx.set(load+1);
                        }
                        if (load >= upperBound) {
                            break;
                        }
                        // consume load
                        action.call(load, cancellationSource,_i);
                    }
                }
            });
            task.Start(false);
            ret.add(task);
        }
        WaitForAsyncTasks(ret, wait);
        return ret;
    }
    private void BlockedParitionRunner(Integer start, Integer end, Action3<Integer, CancellationTokenSource, Integer> action, Integer threadid, CancellationTokenSource cancellationSource)
    {
        for (Integer i = start; i < end; ++i)
        {
            if (cancellationSource != null && cancellationSource.IsCancellationRequested)
            {
                break;
            }
            action.call(i, cancellationSource, threadid);
        }
    }
    private void CyclicParitionRunner(Integer start, Integer end, Integer step, Action3<Integer, CancellationTokenSource, Integer> action, Integer threadid, CancellationTokenSource cancellationSource)
    {
        for (Integer i = start; i < end; i += step)
        {
            if (cancellationSource != null && cancellationSource.IsCancellationRequested)
            {
                break;
            }
            action.call(i, cancellationSource, threadid);
        }
    }
    public Vector<AsyncTask> CyclicPartitionFor(Integer lowerBound, Integer upperBound, Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource, boolean wait)
    {
        Integer len = upperBound - lowerBound + 1;
        Integer threadCount = Concurrency;
        Integer partLen = len / threadCount;
        if (len % threadCount > 0)
        {
            partLen += 1;
        }
        Vector<AsyncTask> ret = new Vector<AsyncTask>();
        for (Integer i = 0; i < threadCount; ++i)
        {
            Integer start = lowerBound + i;
            Integer end = upperBound;
            AsyncTask.ThisAction actionTask = new AsyncTask.ThisAction(){
                public void call(AsyncTask task){
                    Integer id = (Integer)task.DynamicFields.get("ID");
                    CyclicParitionRunner(start, end, threadCount, action, id, cancellationSource);
                }
            };
            AsyncTask thread = new AsyncTask(actionTask);
            thread.DynamicFields.put("ID",i);
            thread.Start(false);
            ret.add(thread);
        }
        WaitForAsyncTasks(ret, wait);
        return ret;
    }
    public Vector<AsyncTask> BlockedPartitionFor(Integer lowerBound, Integer upperBound, final Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource, boolean wait)
    {
        Integer len = upperBound - lowerBound + 1;
        Integer threadCount = Concurrency;
        Integer partLen = len / threadCount;
        if (len % threadCount > 0)
        {
            partLen += 1;
        }
        Vector<AsyncTask> ret = new Vector<AsyncTask>();
        for (Integer i = 0; i < threadCount; ++i)
        {
            Integer start = lowerBound + i * partLen;
            Integer end = start + partLen;
            if (end > upperBound)
            {
                end = upperBound;
            }
            final Integer _start=start;
            final Integer _end=start;
            AsyncTask.ThisAction actionTask = new AsyncTask.ThisAction(){
                public void call(AsyncTask task){
                    Integer id = (Integer)task.DynamicFields.get("ID");
                    BlockedParitionRunner(_start, _end, action, id, cancellationSource);
                }
            };
            AsyncTask thread = new AsyncTask(actionTask);
            thread.DynamicFields.put("ID",i);
            thread.Start(false);
            ret.add(thread);
        }
        WaitForAsyncTasks(ret, wait);
        return ret;
    }


    public Vector<AsyncTask> BlockedPartitionFor(Integer lowerBound, Integer upperBound, final Action2<Integer, CancellationTokenSource> action, boolean wait)
    {
        Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer,CancellationTokenSource , Integer>(upperBound, new CancellationTokenSource(),0) {
            public void call(Integer i, CancellationTokenSource dummy, Integer arg3) {
                action.call(i, dummy);
            }
        };

        return BlockedPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
    }

    public Vector<AsyncTask> CyclicPartitionFor(Integer lowerBound, Integer upperBound, Action2<Integer, CancellationTokenSource> action, boolean wait)
    {
        Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer,CancellationTokenSource , Integer>(upperBound, new CancellationTokenSource(),0) {
            public void call(Integer i, CancellationTokenSource dummy, Integer arg3) {
                action.call(i, dummy);
            }
        };
        return CyclicPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
    }


    public Vector<AsyncTask> BlockedPartitionFor(Integer lowerBound, Integer upperBound, final Action<Integer> action, boolean wait)
    {
        Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer,CancellationTokenSource , Integer>(upperBound,new CancellationTokenSource(),0) {
            public void call(Integer i, CancellationTokenSource dummy, Integer arg3) {
                action.call(i);
            }
        };
        return BlockedPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
    }

    public Vector<AsyncTask> BlockedPartitionFor(Integer upperBound, Action<Integer> action, boolean wait)
    {
        return BlockedPartitionFor(0, upperBound, action, wait);
    }

    public Vector<AsyncTask> CyclicPartitionFor(Integer upperBound, final Action2<Integer, CancellationTokenSource> action, boolean wait)
    {
        return CyclicPartitionFor(0, upperBound, action, wait);
    }
    public Vector<AsyncTask> CyclicPartitionFor(Integer upperBound, Action<Integer> action, boolean wait)
    {
        return CyclicPartitionFor(0, upperBound, action, wait);
    }
    public Vector<AsyncTask> CyclicPartitionFor(Integer lowerBound, Integer upperBound, Action<Integer> action, boolean wait)
    {
        Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer,CancellationTokenSource , Integer>() {
            public void call(Integer i, CancellationTokenSource dummy, Integer arg3) {
                action.call(i);
            }
        };
        return CyclicPartitionFor(lowerBound, upperBound, actionProxy, null, wait);
    }
    public Vector<AsyncTask> RuntimeLoadBalanceFor(Integer lowerBound, Integer upperBound, Action<Integer> action, boolean wait)
    {
        Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer,CancellationTokenSource , Integer>(upperBound, new CancellationTokenSource(),0) {
            public void call(Integer i, CancellationTokenSource dummy, Integer arg3) {
                action.call(i);
            }
        };
        return RuntimeLoadBalanceFor(lowerBound, upperBound, actionProxy, null, wait);
    }
    public Vector<AsyncTask> RuntimeLoadBalanceFor(Integer upperBound, Action<Integer> action, boolean wait)
    {
        return RuntimeLoadBalanceFor(0, upperBound, action, wait);
    }


    public static Vector<AsyncTask> For(Integer lowerbound, Integer upperBound, Action3<Integer, CancellationTokenSource, Integer> action, boolean wait, ParallelForScheduler scheduler)
    {
        switch (scheduler)
        {
            default:
            case Blocked: return new Parallelx().BlockedPartitionFor(lowerbound, upperBound, action, new CancellationTokenSource(), wait);
            case Cyclic: return new Parallelx().CyclicPartitionFor(lowerbound, upperBound, action, new CancellationTokenSource(), wait);
            case RuntimeLoadBalance: return new Parallelx().RuntimeLoadBalanceFor(lowerbound, upperBound, action, new CancellationTokenSource(), wait);
        }

    }
    public static Vector<AsyncTask> For(Integer lowerbound, Integer upperBound, Action3<Integer, CancellationTokenSource, Integer> action, CancellationTokenSource cancellationSource, boolean wait, ParallelForScheduler scheduler)
    {
        switch (scheduler)
        {
            default:
            case Blocked: return new Parallelx().BlockedPartitionFor(lowerbound, upperBound, action, cancellationSource, wait);
            case Cyclic: return new Parallelx().CyclicPartitionFor(lowerbound, upperBound, action, cancellationSource, wait);
            case RuntimeLoadBalance: return new Parallelx().RuntimeLoadBalanceFor(lowerbound, upperBound, action, cancellationSource, wait);
        }
    }

    public static Vector<AsyncTask> For(Integer lowerbound, Integer upperBound, Action<Integer> action, boolean wait, ParallelForScheduler scheduler)
    {
        switch (scheduler)
        {
            default:
            case Blocked: return new Parallelx().BlockedPartitionFor(lowerbound, upperBound, action, wait);
            case Cyclic: return new Parallelx().CyclicPartitionFor(lowerbound, upperBound, action, wait);
            case RuntimeLoadBalance: return new Parallelx().RuntimeLoadBalanceFor(lowerbound, upperBound, action, wait);
        }
    }

    public static Vector<AsyncTask> For(Integer upperBound, Action<Integer> action, boolean wait, ParallelForScheduler scheduler)
    {
        switch (scheduler)
        {
            default:
            case Blocked: return new Parallelx().BlockedPartitionFor(upperBound, action, wait);
            case Cyclic: return new Parallelx().CyclicPartitionFor(upperBound, action, wait);
            case RuntimeLoadBalance: return new Parallelx().RuntimeLoadBalanceFor(upperBound, action, wait);
        }

    }

    public static Vector<AsyncTask> Foreach(final List upperBound, final Action action, boolean wait , ParallelForScheduler scheduler)
    {
        Action<Integer> actionProxy = new Action<Integer>(upperBound.size()){
            public void call(Integer i){
                action.call(upperBound.get(i));
            }
        };
        switch (scheduler)
        {
            default:
            case Blocked: return new Parallelx().BlockedPartitionFor(upperBound.size(), actionProxy, wait);
            case Cyclic: return new Parallelx().CyclicPartitionFor(upperBound.size(), actionProxy, wait);
            case RuntimeLoadBalance: return new Parallelx().RuntimeLoadBalanceFor(upperBound.size(), actionProxy, wait);
        }
    }
    public static Vector<AsyncTask> Foreach(List upperBound, final Action2<Object, Integer> action, boolean wait, ParallelForScheduler scheduler)
    {


        Action3<Integer, CancellationTokenSource, Integer> actionProxy = new Action3<Integer,CancellationTokenSource , Integer>() {
            public void call(Integer i, CancellationTokenSource dummy, Integer id) {
                action.call(upperBound.get(i), id);
            }
        };
        switch (scheduler)
        {
            default:
            case Blocked: return new Parallelx().BlockedPartitionFor(0, upperBound.size(), actionProxy, null, wait);
            case Cyclic: return new Parallelx().CyclicPartitionFor(0, upperBound.size(), actionProxy, null, wait);
            case RuntimeLoadBalance: return new Parallelx().RuntimeLoadBalanceFor(0, upperBound.size(), actionProxy, null, wait);
        }
    }


}
