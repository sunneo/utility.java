package com.example.csharp.actions;


public class Action2<T1,T2> implements  Runnable{
    public T1 arg;
    public T2 arg2;
    public void call(T1 arg,T2 arg2){
    }
    @Override
    public void run() {
        call(this.arg,this.arg2);
    }
    public Action2(T1 arg,T2 arg2){
        this.arg = arg;
        this.arg2 = arg2;
    }
    public Action2(){

    }
}