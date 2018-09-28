package com.example.csharp.actions;


public class Action4<T1,T2,T3,T4> implements Runnable{
    public T1 arg;
    public T2 arg2;
    public T3 arg3;
    public T4 arg4;
    public void call(T1 arg,T2 arg2, T3 arg3,T4 arg4){
    }

    public Action4(T1 arg, T2 arg2, T3 arg3, T4 arg4){
        this.arg = arg;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
    }

    public Action4(){
    }

    @Override
    public void run() {
        this.call(this.arg,this.arg2,this.arg3,this.arg4);
    }
}