package com.example.sharp.rpc;

import com.example.events.WritableValue;
import com.example.JSON;

public class JSONRPCMessage {
    public String method;
    public String version="1.0";
    public Object result;
    public Object[] params;
    public static boolean getBooleanObject(Object o) {
        if(o instanceof Boolean) {
            return ((Boolean)o).booleanValue();
        }
        else if(o instanceof String) {
            WritableValue<Boolean> val = new WritableValue<Boolean>();
            if(WritableValue.tryParseBool((String)o, val)) {
                return val.get();
            }
        }
        return false;
    }
    public static int getIntegerObject(Object o,int defaultVal) {
        if(o != null) {
            if(o instanceof Double) {
                return (int)((Double)o).doubleValue();
            } else if(o instanceof Float) {
                return (int)((Float)o).floatValue();
            } else if(o instanceof Integer) {
                return ((Integer)o).intValue();
            } else if(o instanceof Short) {
                return ((Short)o).shortValue();
            } else if(o instanceof Long) {
                return (int)((Long)o).longValue();
            }
            else if(o instanceof String) {
                WritableValue<Integer> val = new WritableValue<Integer>();
                if(WritableValue.tryParseInt((String)o, val)) {
                    return val.get();
                }
            }
        }
        return defaultVal;
    }
    public static long getLongObject(Object o, long defaultVal) {
        if(o != null) {
            if(o instanceof Double) {
                return (long)((Double)o).doubleValue();
            } else if(o instanceof Float) {
                return (long)((Float)o).floatValue();
            } else if(o instanceof Integer) {
                return ((Integer)o).intValue();
            } else if(o instanceof Short) {
                return ((Short)o).shortValue();
            } else if(o instanceof Long) {
                return (int)((Long)o).longValue();
            }
            else if(o instanceof String) {
                WritableValue<Integer> val = new WritableValue<Integer>();
                if(WritableValue.tryParseInt((String)o, val)) {
                    return val.get();
                }
            }
        }
        return defaultVal;
    }
    public static double getDoubleObject(Object o, double defaultVal) {
        if(o != null) {
            if(o instanceof Double) {
                return ((Double)o).doubleValue();
            } else if(o instanceof Float) {
                return ((Float)o).floatValue();
            } else if(o instanceof Integer) {
                return ((Integer)o).intValue();
            } else if(o instanceof Short) {
                return ((Short)o).shortValue();
            } else if(o instanceof Long) {
                return (double)((Long)o).longValue();
            }
            else if(o instanceof String) {
                try {
                    return Double.parseDouble((String)o);
                }catch(Exception ee) {
                    ee.printStackTrace();
                    return 0;
                }
            }
        }
        return defaultVal;
    }
    public long getParamLong(int idx,long defaultVal) {
        if(idx >= params.length || idx < 0) {
            return defaultVal;
        }
        return getLongObject(params[idx], defaultVal);
    }
    public int getParamInt(int idx, int defaultVal) {
        if(idx >= params.length || idx < 0) {
            return 0;
        }
        return getIntegerObject(params[idx],defaultVal);
    }
    public double getParamDouble(int idx, double defaultVal) {
        if(idx >= params.length || idx < 0) {
            return 0;
        }
        return getDoubleObject(params[idx],defaultVal);
    }
    public boolean getParamBool(int idx) {
        if(idx >= params.length || idx < 0) {
            return false;
        }
        return getBooleanObject(params[idx]);
    }
    public int getResultInt() {
        if(result == null) {
            return 0;
        }
        return getIntegerObject(result, 0);
    }
    public double getResultDouble(int idx) {
        if(result == null) {
            return 0;
        }
        return getDoubleObject(result, 0);
    }
    public boolean getResultBool(int idx) {
        if(result == null) {
            return false;
        }
        return getBooleanObject(result);
    }
    public JSONRPCMessage assignParam(Object...objects) {
        this.params = objects;
        return this;
    }
    
    protected JSONRPCRemoteObjectReference getRemoteObjectRef(Object obj) {
        
        JSONRPCRemoteObjectReference refObj = null;
        if(obj != null) {
           refObj = JSON.deserialize(obj.toString(), JSONRPCRemoteObjectReference.class);
        }
        long id = -1;
        if(refObj == null) {
            id = getLongObject(obj, -1);
            refObj = new JSONRPCRemoteObjectReference();
            refObj.id = id;
        }
        return refObj;
    }
    public JSONRPCRemoteObjectReference getRemoteObjectRefFromParam(int idx) {
        if(params == null || idx >= params.length || idx < 0) {
            return getRemoteObjectRef(null);
        }
        return getRemoteObjectRef(params[idx]);
    }
    public JSONRPCRemoteObjectReference getRemoteObjectRefFromResult() {
        return getRemoteObjectRef(result);
    }
    
    public JSONRPCMessage() {
        // TODO Auto-generated constructor stub
    }

}
