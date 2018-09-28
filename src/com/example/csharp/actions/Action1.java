package com.example.csharp.actions;

public class Action1<T> implements Runnable{
    public T arg;
    public void call(T arg){

    }

    public Action1(T arg){
        this.arg = arg;
    }
    public Action1(){
    }

    @Override
    public void run() {
        call(this.arg);
    }
}
