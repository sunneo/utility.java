package com.example;

import com.example.sharp.BaseDictionary;
import com.example.sharp.CString;
import com.example.sharp.Delegates;
import com.example.sharp.reflection.FieldWrapper;
import com.example.sharp.reflection.ReflectionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class JSON {
    public static String serialize(Object target){
        JSONObject json = new JSONObject();
        BaseDictionary<Object,Object> visited = new BaseDictionary<>();
        serializeObject(json,target,visited);
        return json.toString();
    }
    public static <T> T deserialize(String obj, Class<T> clz){
        T ret = null;
        try {
            JSONObject json = new JSONObject(obj);
            ret = deserializeJSON(json, clz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    protected static <T> T deserializeJSON(JSONObject obj, Class<T> clz){
        T ret = null;
        try {
             ret = clz.newInstance();
             deserializeObject(obj, ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    protected static void deserializeJSONArrayObject(JSONArray arr, Iterable arrayObject, Delegates.Action2<Iterable, Object> addFnc){

        if(arr == null) return;
        int len = arr.length();
        for(int i=0; i<len; ++i){
            try {
                Object jsonObject = arr.get(i);
                addFnc.Invoke(arrayObject, jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    protected static void deserializeJSONArrayToRawArrayObject(JSONArray arr, Object arrayObject, Delegates.Action3<Object, Integer,Object> addFnc){

        if(arr == null) return;
        int len = arr.length();
        for(int i=0; i<len; ++i){
            try {
                Object jsonObject = arr.get(i);
                addFnc.Invoke(arrayObject, i, jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected static Object deserializeGetObject(JSONObject obj, Class<?> subItemClz, String key){
        try {
            if (subItemClz.isPrimitive()) {
                if (subItemClz.equals(String.class)) {
                    return obj.getString(key);
                } else if (subItemClz.equals(Integer.class)) {
                    return obj.getInt(key);
                } else if (subItemClz.equals(Float.class)) {
                    return obj.getDouble(key);
                } else if (subItemClz.equals(Double.class)) {
                    return obj.getDouble(key);
                } else if (subItemClz.equals(Boolean.class)) {
                    return obj.getBoolean(key);
                } else if (subItemClz.equals(Long.class)) {
                    return obj.getLong(key);
                } else if (subItemClz.equals(Character.class)) {
                    return obj.getString(key).charAt(0);
                } else if (subItemClz.equals(Short.class)) {
                    return obj.getInt(key);
                }
            } else if (subItemClz.equals(String.class)){
                return obj.getString(key);
            } else if(subItemClz.isEnum()){
                return Enum.valueOf((Class<Enum>) subItemClz, obj.getString(key));
            } else if(ArrayList.class.isAssignableFrom(subItemClz)){
                ArrayList list = (ArrayList) subItemClz.newInstance();
                JSONArray arr = obj.getJSONArray(key);
                deserializeJSONArrayObject(arr, list, (l,o)->{
                    ((ArrayList)l).add(o);
                });
                return list;
            } else if(Vector.class.isAssignableFrom(subItemClz)){
                Vector list = (Vector) subItemClz.newInstance();
                JSONArray arr = obj.getJSONArray(key);
                deserializeJSONArrayObject(arr, list, (l,o)->{
                    ((ArrayList)l).add(o);
                });
                return list;
            } else {
                // object
                Object subObject = subItemClz.newInstance();
                deserializeObject(obj,subObject);
                return subObject;
            }
        }catch(Exception ee){

        }
        return null;
    }
    protected static <T> void deserializeObject(JSONObject obj, T item){
        ReflectionHelper reflection = new ReflectionHelper(item);
        for(String key: Delegates.forall(obj.keys())){
            if(obj.isNull(key)) continue;
            if(!reflection.fields.containsField(key)) continue;
            if(!reflection.fields.isPublic(key)) continue;
            if(reflection.fields.isTransient(key)) continue;
            FieldWrapper fieldWrapper = reflection.fields.fields.get(key);

            Class<?> subItemClz = fieldWrapper.field.getType();
            try {
                if(subItemClz.isPrimitive() || subItemClz.isEnum() || String.class.equals(subItemClz)) {
                    reflection.fields.set(key,deserializeGetObject(obj,subItemClz,key));
                } else if(ArrayList.class.isAssignableFrom(subItemClz)){
                    reflection.fields.set(key,deserializeGetObject(obj,subItemClz,key));
                } else if(Vector.class.isAssignableFrom(subItemClz)){
                    reflection.fields.set(key,deserializeGetObject(obj,subItemClz,key));
                } else if(subItemClz.isArray()){
                    JSONArray arr = obj.getJSONArray(key);
                    Object list = Array.newInstance(subItemClz.getComponentType(),arr.length());
                    deserializeJSONArrayToRawArrayObject(arr, list, (l, idx,o)->{
                        Array.set(l,idx,o);
                    });
                    reflection.fields.set(key,list);
                } else {
                    // object
                    reflection.fields.set(key,deserializeGetObject(obj,subItemClz,key));
                }
            }catch(Exception ee){
                ee.printStackTrace();;
            }

        }
    }
    protected static void serializeObject(JSONArray arr,  Object obj, BaseDictionary<Object,Object> visited){
        if(obj.getClass().isArray()){
            int len = Array.getLength(obj);
            for(int i=0; i<len; ++i){
                Object subItem = Array.get(obj,i);
                serializeObject(arr, subItem,visited);
            }
        } else if(obj instanceof Iterable){
            Iterable iterable=(Iterable) obj;
            for(Object subItem: iterable){
                serializeObject(arr, subItem,visited);
            }
        } else if(obj.getClass().isPrimitive() || obj instanceof String){
            arr.put(obj);
        } else {
            JSONObject jsonObj = new JSONObject();
            serializeObject(jsonObj, obj, visited);
            arr.put(jsonObj);
        }
    }
    protected static void serializeObject(JSONObject json, Object obj, BaseDictionary<Object,Object> visited){
        if(visited!=null && visited.containsKey(obj)){
            return;
        }
        visited.set(obj,obj);
        ReflectionHelper reflect = new ReflectionHelper(obj);
        for(String key:reflect.fields.fields.keySet()){
            if(!reflect.fields.isPublic(key)){
                continue;
            }
            if(reflect.fields.isTransient(key)){
                continue;
            }
            Object subItem = reflect.fields.get(key);
            if(subItem == null) {
                continue;
            }
            try {
                if (reflect.fields.isPrimitive(key) || reflect.fields.isString(key)) {
                    json.put(key, subItem);
                } else if (reflect.fields.isEnum(key)) {
                    json.put(key, reflect.fields.get(key).toString());
                } else {
                    if (subItem.getClass().isArray() || subItem instanceof Iterable) {
                        JSONArray arr = new JSONArray();
                        serializeObject(arr, subItem, visited);
                        json.put(key, arr);
                    } else {
                        JSONObject subObject = new JSONObject();
                        serializeObject(subObject, subItem, visited);
                        json.put(key, subObject);
                    }
                }
            }catch(Exception ee){
                ee.printStackTrace();;
            }

        }
    }

}
