package com.example.sharp.rpc;

import com.example.events.WritableValue;

import java.lang.reflect.Array;

import org.json.JSONArray;

import com.example.JSON;

public class JSONRPCMessage {
	public static class Arguments {
		public Object[] args = new Object[0];

		public void Assign(Object... args) {
			if (args != null && args.length > 0) {
				this.args = args;
			}
		}

		public int GetInt(int idx) {
			if (idx < args.length) {
				return (int) args[idx];
			}
			return -1;
		}

		public double GetDouble(int idx) {
			if (idx < args.length) {
				return (double) args[idx];
			}
			return -1;
		}

	}
    public String method;
    public String version="1.0";
    public Object Result;
    public Arguments Params = new Arguments();
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
        if(idx >= Params.args.length || idx < 0) {
            return defaultVal;
        }
        return getLongObject(Params.args[idx], defaultVal);
    }
    public int getParamInt(int idx, int defaultVal) {
        if(idx >= Params.args.length || idx < 0) {
            return 0;
        }
        return getIntegerObject(Params.args[idx],defaultVal);
    }
    public double getParamDouble(int idx, double defaultVal) {
        if(idx >= Params.args.length || idx < 0) {
            return 0;
        }
        return getDoubleObject(Params.args[idx],defaultVal);
    }
    public boolean getParamBool(int idx) {
        if(idx >= Params.args.length || idx < 0) {
            return false;
        }
        return getBooleanObject(Params.args[idx]);
    }
    public int getResultInt() {
        if(Result == null) {
            return 0;
        }
        return getIntegerObject(Result, 0);
    }
    public double getResultDouble(int idx) {
        if(Result == null) {
            return 0;
        }
        return getDoubleObject(Result, 0);
    }
    public boolean getResultBool(int idx) {
        if(Result == null) {
            return false;
        }
        return getBooleanObject(Result);
    }
    public JSONRPCMessage assignParam(Object...objects) {
        this.Params.Assign(objects);
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
        if(Params == null || idx >= Params.args.length || idx < 0) {
            return getRemoteObjectRef(null);
        }
        return getRemoteObjectRef(Params.args[idx]);
    }
    public JSONRPCRemoteObjectReference getRemoteObjectRefFromResult() {
        return getRemoteObjectRef(Result);
    }
    
    public JSONRPCMessage() {
        // TODO Auto-generated constructor stub
    }

}
