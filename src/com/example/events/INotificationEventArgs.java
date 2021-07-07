package com.example.events;

import com.example.sharp.Tuples;

/**
 * Base Event argument
 */
public class INotificationEventArgs {
    public Object[] object;
    public INotificationEventArgs(Object...args) {
        object = args;
    }
    public static class INotificationEventArg1<T> extends INotificationEventArgs{
        @SuppressWarnings("unchecked")
        public T get_1() {
        	if(object.length == 0) {
        		return null;
        	}
            return (T)object[0];
        }
        public INotificationEventArg1(Object...args) {
            super(args);
        }
        public Tuples.Tuple<T> tuple(){
        	return Tuples.tuple(get_1());
        }
    }
    public static class INotificationEventArg2<T1,T2> extends INotificationEventArg1<T1>{
        @SuppressWarnings("unchecked")
        public T2 get_2() {
            return (T2)object[1];
        }
        public INotificationEventArg2(Object...args) {
            super(args);
        }
        public Tuples.Tuple2<T1,T2> tuple(){
        	return Tuples.tuple(get_1(),get_2());
        }
    }
    public static class INotificationEventArg3<T1,T2,T3> extends INotificationEventArg2<T1,T2>{
        @SuppressWarnings("unchecked")
        public T3 get_3() {
            return (T3)object[2];
        }
        public INotificationEventArg3(Object...args) {
            super(args);
        }
        public Tuples.Tuple3<T1,T2,T3> tuple(){
        	return Tuples.tuple(get_1(),get_2(),get_3());
        }
    }
    public static class INotificationEventArg4<T1,T2,T3,T4> extends INotificationEventArg3<T1,T2,T3>{
        @SuppressWarnings("unchecked")
        public T4 get_4() {
            return (T4)object[3];
        }
        public INotificationEventArg4(Object...args) {
            super(args);
        }
        public Tuples.Tuple4<T1,T2,T3,T4> tuple(){
        	return Tuples.tuple(get_1(),get_2(),get_3(),get_4());
        }
    }
    public static class INotificationEventArg5<T1,T2,T3,T4,T5> extends INotificationEventArg4<T1,T2,T3,T4>{
        @SuppressWarnings("unchecked")
        public T5 get_5() {
            return (T5)object[4];
        }
        public INotificationEventArg5(Object...args) {
            super(args);
        }
        public Tuples.Tuple5<T1,T2,T3,T4,T5> tuple(){
        	return Tuples.tuple(get_1(),get_2(),get_3(),get_4(),get_5());
        }
    }
    public static class INotificationEventArg6<T1,T2,T3,T4,T5,T6> extends INotificationEventArg5<T1,T2,T3,T4,T5>{
        @SuppressWarnings("unchecked")
        public T6 get_6() {
            return (T6)object[5];
        }
        public INotificationEventArg6(Object...args) {
            super(args);
        }
        public Tuples.Tuple6<T1,T2,T3,T4,T5,T6> tuple(){
        	return Tuples.tuple(get_1(),get_2(),get_3(),get_4(),get_5(),get_6());
        }
    }
    public static class INotificationEventArg7<T1,T2,T3,T4,T5,T6,T7> extends INotificationEventArg6<T1,T2,T3,T4,T5,T6>{
        @SuppressWarnings("unchecked")
        public T7 get_7() {
            return (T7)object[6];
        }
        public INotificationEventArg7(Object...args) {
            super(args);
        }
        public Tuples.Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple(){
        	return Tuples.tuple(get_1(),get_2(),get_3(),get_4(),get_5(),get_6(),get_7());
        }
    }
    public static class INotificationEventArg8<T1,T2,T3,T4,T5,T6,T7,T8> extends INotificationEventArg7<T1,T2,T3,T4,T5,T6,T7>{
        @SuppressWarnings("unchecked")
        public T8 get_8() {
            return (T8)object[7];
        }
        public INotificationEventArg8(Object...args) {
            super(args);
        }
        public Tuples.Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(){
        	return Tuples.tuple(get_1(),get_2(),get_3(),get_4(),get_5(),get_6(),get_7(),get_8());
        }
    }
}