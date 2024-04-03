package com.example.events;

import com.example.sharp.IGetter;
import com.example.sharp.ISetter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WritableValue <T> implements IGetter<T>, ISetter<T>{
	protected T value;
	public static <T> WritableValue<T> create(T val){
		return new WritableValue<T>(val);
	}
	public WritableValue() {
		
	}
	public WritableValue(T value) {
		this.value = value;
	}
	
	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return this.value;
	}
	public void dispose() {
		this.value = null;
	}
	public static boolean tryParseInt(String str,WritableValue<Integer> val,int radix){
		try {
			Integer retVal = Integer.parseInt(str,radix);
			val.set(retVal);
			return true;
		}catch(Exception ee){
			return false;
		}
	}

	public static boolean tryParseInt(String str,WritableValue<Integer> val){
		return tryParseInt(str,val,10);
	}
	public static boolean tryParseBool(String str,WritableValue<Boolean> val){
		try {
			Boolean retVal = Boolean.parseBoolean(str);
			val.set(retVal);
			return true;
		}catch(Exception ee){
			return false;
		}
	}

	public static boolean tryParseDouble(String str,WritableValue<Double> val){
		try {
			Double retVal = Double.parseDouble(str);
			val.set(retVal);
			return true;
		}catch(Exception ee){
			return false;
		}
	}

	public static boolean tryParseLong(String str,WritableValue<Long> val,int radix){
		try {
			Long retVal = Long.parseLong(str,radix);
			val.set(retVal);
			return true;
		}catch(Exception ee){
			return false;
		}
	}

	public static boolean tryParseLong(String str,WritableValue<Long> val){
		return tryParseLong(str,val,10);
	}

	public static boolean tryParseDate(String str,WritableValue<Date> val){
		try {
			Date retVal = SimpleDateFormat.getDateInstance().parse(str);
			val.set(retVal);
			return true;
		} catch(Exception ee){
			return false;
		}
	}
}
