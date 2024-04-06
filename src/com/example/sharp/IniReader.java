/*
 * Copyright (c) 2019-2024 [Open Source Developer, Sunneo].
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the [Open Source Developer, Sunneo] nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE [Open Source Developer, Sunneo] AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE [Open Source Developer, Sunneo] AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.sharp;

import com.example.events.EventDelegate;
import com.example.events.INotification;
import com.example.events.INotificationEventArgs;
import com.example.events.WritableValue;
import com.example.sharp.annotations.IniFieldNameAttribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class IniReader {

    public Dictionary<String, ArrayList<String>> Data = new Dictionary<String, ArrayList<String>>();

    public Dictionary<String, String> FieldSectionMapper = new Dictionary<String, String>();

    public int[] getIntsFromString(String name) {
        String s = getString(name);
        if (CString.IsNullOrEmpty(s)) {
            if (CString.IsNullOrEmpty(name)) {
                return null;
            } else {
                if (name.indexOf(',') > -1) {
                    s = name;
                }
            }
        }
        if (s.indexOf(',') > -1) {
            String[] splits = s.split(",");
            int len = splits.length;
            int[] ret = new int[len];
            for (int i = 0; i < len; ++i) {
                WritableValue<Integer> intVal = new WritableValue<Integer>(0);
                WritableValue.tryParseInt(splits[i], intVal);
                ret[i] = intVal.get();
            }
            return ret;
        } else {
            int dummy = 0;
            WritableValue<Integer> intVal = new WritableValue<Integer>(0);
            WritableValue.tryParseInt(s, intVal);
            dummy = intVal.get();
            return new int[]{dummy};
        }
    }

    public double getDouble(String name) {
        return getDouble(name, 0.0);
    }

    public double getDouble(String name, double defaultValue) {
        ArrayList<String> list = getList(name);
        double ret = defaultValue;
        if (list.size() > 0) {
            WritableValue<Double> val = new WritableValue<Double>(0.0);
            WritableValue.tryParseDouble(list.get(0), val);
            ret = val.get();
        }
        return ret;
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public int getInt(String name, int defaultValue) {
        ArrayList<String> list = getList(name);
        int ret = defaultValue;
        if (list.size() > 0) {
            WritableValue<Integer> val = new WritableValue<Integer>(0);
            WritableValue.tryParseInt(list.get(0), val);
            ret = val.get();
        }
        return ret;
    }

    public ArrayList<String> getList(String s) {
        ArrayList<String> list = new ArrayList<String>();
        if (Data.ContainsKey(s)) {
            list = Data.get(s);
            if (this.recordLoaded) {
                this.NotLoaded.set(s, true);
            }
        }
        return list;
    }

    public boolean getBoolean(String s) {
        return getBoolean(s, false);
    }

    public boolean getBoolean(String s, boolean defaultValue) {
        String str = getString(s);
        if (CString.IsNullOrEmpty(str)) return defaultValue;
        boolean ret = defaultValue;
        WritableValue<Boolean> val = new WritableValue<>(defaultValue);
        WritableValue.tryParseBool(str, val);
        ret = val.get();
        return ret;

    }

    public String getString(String s) {
        return getString(s, "");
    }

    public String getString(String s, String defaultValue) {
        ArrayList<String> list = getList(s);
        String ret = "";
        if (list.size() > 0) {
            if (list.size() == 1) {
                ret = list.get(0);
            } else {
                StringBuilder strb = new StringBuilder();
                for (int i = 0; i < list.size(); ++i) {
                    strb.append(list.get(i) + "\n");
                }
                ret = strb.toString();
            }
        } else {
            return defaultValue;
        }
        return ret;
    }

    private IniReader() {

    }

    public static class OnSerializeNotificationEventArgs  {
        public IniReader Reader;
        public Field Field;
        public String FullName;
        public Object Target;
        public Object FieldValue;
        public String Section;
    }

    public static void DeserializeFields(IniReader reader, Object ret) {
        DeserializeFields(reader, ret, "", null);
    }

    public static void DeserializeFields(IniReader reader, Object ret, String prefix) {
        DeserializeFields(reader, ret, prefix, null);
    }

    public static void DeserializeFields(IniReader reader, Object ret, String prefix, EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<OnSerializeNotificationEventArgs>>> OnSerializingMember) {
        Class t = ret.getClass();

        for (Field field : t.getDeclaredFields()) {
            Object FieldValue = null;
            OnSerializeNotificationEventArgs OnSerializeArgs = new OnSerializeNotificationEventArgs();
            OnSerializeArgs.Reader = reader;
            OnSerializeArgs.Field = field;
            OnSerializeArgs.Target = ret;
            field.setAccessible(true);
            int modifier = field.getModifiers();
            if (Modifier.isPrivate(modifier)) continue;
            if (Modifier.isTransient(modifier)) continue;

            if (Modifier.isPublic(field.getModifiers())) {
                Class<?> fieldType = field.getType();
                String name = prefix + field.getName();
                IniFieldNameAttribute iniFieldName = (IniFieldNameAttribute) field.getAnnotation(IniFieldNameAttribute.class);
                if (iniFieldName != null && !CString.IsNullOrEmpty(iniFieldName.name())) {
                    name = iniFieldName.name();
                }
                OnSerializeArgs.FullName = name;

                if (fieldType.isPrimitive()) {
                    if (fieldType.equals(Integer.class)) {
                        int val = reader.getInt(name);
                        try {
                            field.setInt(ret, val);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        FieldValue = val;
                    } else if (fieldType.equals(Boolean.class)) {
                        boolean val = reader.getBoolean(name);
                        try {
                            field.setBoolean(ret, val);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        FieldValue = val;
                    } else if (fieldType.equals(Double.class)) {
                        double val = reader.getDouble(name);
                        try {
                            field.setDouble(ret, val);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        FieldValue = val;
                    } else if (fieldType.isEnum()) {
                        try {
                            String sval = reader.getString(name);
                            field.set(ret, Enum.valueOf((Class<Enum>) fieldType, sval));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                } else if (fieldType.equals(String.class)) {
                    String val = reader.getString(name);
                    try {
                        field.set(ret, val);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    FieldValue = val;
                } else if (fieldType.isArray() && fieldType.getComponentType().equals(Double.class))
                {
                    String val = reader.getString(name);
                    ArrayList<Double> intLinkedList = DoubleLinkedListFromString(val);
                    Double[] arr = intLinkedList.toArray(new Double[0]);
                    try {
                        field.set(ret, arr);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    FieldValue = arr;
                }
                    else if (fieldType.isArray() && fieldType.getComponentType().equals(Integer.class))
                {
                    String val = reader.getString(name);
                    ArrayList<Integer> intLinkedList = IntLinkedListFromString(val);
                    Integer[] arr = intLinkedList.toArray(new Integer[0]);
                    try {
                        field.set(ret, arr);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    FieldValue = arr;
                } else {
                    Constructor<?> constructor = null;
                    try {
                        constructor = fieldType.getConstructor();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Object fieldContent = null;
                    try {
                        fieldContent = field.get(ret);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fieldContent == null) {
                        if (constructor != null) {
                            try {
                                fieldContent = constructor.newInstance();
                                field.set(ret, fieldContent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    FieldValue = fieldContent;
                    if (fieldContent != null) {
                        DeserializeFields(reader, fieldContent, field.getName() + ".", OnSerializingMember);
                    }
                }
                OnSerializeArgs.FieldValue = FieldValue;
                if (reader.FieldSectionMapper.ContainsKey(name)) {
                    OnSerializeArgs.Section = reader.FieldSectionMapper.get(name);
                }
                if (OnSerializingMember != null) {
                    OnSerializingMember.invoke(reader,OnSerializeArgs);
                }
            }
        }
    }

    SequentialDictionary<String, Boolean> NotLoaded = new SequentialDictionary<String, Boolean>();
    boolean recordLoaded = false;

    public ArrayList<String> GetNotLoaded() {
        ArrayList<String> ret = new ArrayList<String>();
        for (String k : NotLoaded.keySet()) {
            if (!NotLoaded.get(k)) {
                ret.add(k);
            }
        }
        return ret;
    }

    public Property<Boolean> RecordLoaded = new Property<Boolean>() {
        public Boolean get() {
            return recordLoaded;
        }

        public void set(Boolean value) {
            boolean orig = recordLoaded;
            recordLoaded = value;
            if (orig != recordLoaded) {
                if (recordLoaded) {
                    for (String key : Data.keySet()) {
                        NotLoaded.set(key, false);
                    }
                } else {
                    NotLoaded.Clear();
                }
            }
        }
    };

    public static <T> T Deserialize(String filename, Class<T> clz) {
        return Deserialize(filename, clz, null);
    }

    public static <T> T Deserialize(
            String filename, Class<T> clz, EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<OnSerializeNotificationEventArgs>>> OnSerializingMember) {
        IniReader reader = IniReader.FromFile(filename);
        Constructor<T> constructor = null;
        T ret = null;
        try {
            constructor = clz.getConstructor();
            ret = (T) constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        DeserializeFields(reader, ret, "", OnSerializingMember);
        return ret;
    }

    public static <T> T DeserializeString(
            String stringContent, Class<T> t) {
        return DeserializeString(stringContent, t, null);
    }

    public static <T> T DeserializeString(
            String stringContent, Class<T> t, EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<OnSerializeNotificationEventArgs>>> OnSerializingMember) {
        IniReader reader = IniReader.FromString(stringContent);
        try {
            Constructor<T> constructor = null;
            constructor = t.getConstructor();
            T ret = (T) constructor.newInstance();
            DeserializeFields(reader, ret, "", OnSerializingMember);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<Integer> IntLinkedListFromString(String val) {
        if (val.indexOf(',') > -1) {
            Vector<String> splits = CString.tokenize(val, ",");
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (String s : splits) {
                if (!CString.IsNullOrEmpty(s)) {
                    WritableValue<Integer> ival = new WritableValue<Integer>(0);
                    WritableValue.tryParseInt(s, ival);
                    list.add(ival.get());
                }
            }
            return list;
        }
        return new ArrayList<Integer>();
    }

    public static ArrayList<Double> DoubleLinkedListFromString(String val) {
        if (val.indexOf(',') > -1) {
            Vector<String> splits = CString.tokenize(val, ",");
            ArrayList<Double> list = new ArrayList<Double>();
            for (String s : splits) {
                if (!CString.IsNullOrEmpty(s)) {
                    WritableValue<Double> ival = new WritableValue<Double>(0.0);
                    WritableValue.tryParseDouble(s, ival);
                    list.add(ival.get());
                }
            }
            return list;
        }
        return new ArrayList<Double>();
    }

    String CurrentCategory = "";
    private boolean appendOnSameName = true;

    protected static boolean PreprocessScanInclude(LinkedList<String> lines, LinkedList<BaseLinkedListNode<String>> includes) {
        BaseLinkedListNode<String> node = lines.First.get();
        boolean includeAdded = true;
        // initial collect
        while (node != null) {
            String line = node.Value;
            if (line == null) break;
            line = CString.Trim(line);
            if (!CString.IsNullOrEmpty(line)) {
                if (line.toLowerCase().startsWith("!include")) {
                    includes.AddLast(node);
                    includeAdded = true;
                    node = node.Next;
                    continue;
                }

                if (line.charAt(0) == '#') {
                    node = node.Next;
                    continue;
                }
            }
            node = node.Next;
        }
        return includeAdded;
    }

    public static String DumpStringLines(LinkedList<String> lines) {
        StringBuilder strb = new StringBuilder();
        for (String line : lines) {
            strb.append(line + "\n");
        }
        return strb.toString();
    }

    protected static void Preprocess(LinkedList<String> lines) {
        LinkedList<BaseLinkedListNode<String>> includes = new LinkedList<BaseLinkedListNode<String>>();
        PreprocessScanInclude(lines, includes);
        // expand all include statement
        Dictionary<String, String> visited = new Dictionary<String, String>();
        while (includes.size() > 0) {
            BaseLinkedListNode<String> includeNode = includes.First.get().Value;
            String line = includeNode.Value;
            line = line.substring("!Include".length());
            int idxOfComment = line.indexOf('#');
            if (idxOfComment != -1) {
                line = line.substring(0, idxOfComment);
            }
            line = CString.Trim(line);
            if (new File(line).exists()) {
                // prevent it from infinite recursive
                if (visited.ContainsKey(line)) continue;
                visited.set(line, line);
                // read, add
                LinkedList<String> anotherFile = null;
                try (FileReader fsAnother = new FileReader(line)) {
                    anotherFile = ReadAsLines(fsAnother);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                if (anotherFile != null) {
                    PreprocessScanInclude(anotherFile, includes); // scan and add new includes
                    BaseLinkedListNode<String> afterNode = includeNode;
                    BaseLinkedListNode<String> anotherLine = anotherFile.First.get();
                    while (anotherLine != null) {
                        BaseLinkedListNode<String> next = anotherLine.Next;
                        anotherLine.Remove();
                        afterNode.AddAfter(anotherLine);
                        afterNode = afterNode.Next;
                        anotherLine = next;
                    }
                    includeNode.Value = "### Content Included From " + line;
                }
            }
            includes.RemoveFirst();
        }
    }

    protected static LinkedList<String> ReadAsLines(Reader fs) {
        LinkedList<String> lines = new LinkedList<String>();
        try (BufferedReader reader = new BufferedReader(fs)) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                lines.AddLast(line);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return lines;
    }

    private void ParseStream(Reader fs) {
        {
            try {
                LinkedList<String> lines = ReadAsLines(fs);
                Preprocess(lines);
                Iterator<String> enumerator = lines.iterator();
                while (enumerator.hasNext()) {
                    String line = enumerator.next();
                    if (line == null) break;
                    line = CString.Trim(line);
                    if (!CString.IsNullOrEmpty(line)) {
                        if (line.charAt(0) == '#') {
                            continue;
                        }
                        if (line.charAt(0) == '[') {
                            Vector<String> splits = CString.tokenize(line, "[]");
                            if (splits.size() > 0) {
                                String sec = splits.get(0);
                                if (!CurrentCategory.equals(sec)) {
                                    CurrentCategory = sec;
                                }
                            }
                        }
                        if (line.indexOf('=') > -1) {
                            int idx = line.indexOf('=');
                            String k = CString.Trim(line.substring(0, idx));
                            if (idx + 1 < line.length()) {
                                String v = CString.Trim(line.substring(idx + 1));
                                ArrayList<String> list = null;
                                if (Data.ContainsKey(k) && appendOnSameName) {
                                    list = Data.get(k);
                                } else {
                                    list = new ArrayList<String>();
                                    Data.set(k, list);
                                }
                                list.add(v);
                                FieldSectionMapper.set(k, CurrentCategory);
                            }
                        }
                    }
                    if (line == null) {
                        break;
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private void ParseFile(String IniFilePath) {
        try (FileReader fs = new FileReader(IniFilePath)) {
            ParseStream(fs);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void ParseString(String stringContent) {
        try (StringReader fs = new StringReader(stringContent)) {
            ParseStream(fs);
        } catch (Exception ee) {

        }
    }

    public static <T> T Deserialize(
            Map<String, Object> dic, Class<T> clz) {
        return Deserialize(dic, clz, null);
    }

    public static <T> T Deserialize(
            Map<String, Object> dic, Class<T> clz, EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<OnSerializeNotificationEventArgs>>> OnSerializingMember) {
        IniReader reader = IniReader.FromDictionary(dic);
        T ret = null;
        try {
            Constructor<T> constructor = null;
            constructor = clz.getConstructor();
            ret = (T) constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        DeserializeFields(reader, ret, "", OnSerializingMember);
        return ret;
    }

    private void ParseDictionary(Map<String, Object> dic) {
        for (String key : dic.keySet()) {
            ArrayList<String> val = new ArrayList<String>();
            if (Data.ContainsKey(key)) {
                val = Data.get(key);
            }
            Object dicVal = dic.get(key);
            if (dicVal != null) {
                val.add(dicVal.toString());
            } else {

            }
        }
    }

    public static IniReader FromDictionary(Map<String, Object> dic) {
        IniReader ret = new IniReader();
        ret.ParseDictionary(dic);
        return ret;
    }

    public static IniReader FromString(String stringContent) {
        IniReader ret = new IniReader();
        ret.ParseString(stringContent);
        return ret;
    }

    public static IniReader FromFile(String filename) {
        return FromFile(filename, true);
    }

    public static IniReader FromFile(String filename, boolean appendOnSameName) {
        IniReader ret = new IniReader();
        ret.appendOnSameName = appendOnSameName;
        if (new File(filename).exists()) {
            ret.ParseFile(filename);
        }
        return ret;
    }
}