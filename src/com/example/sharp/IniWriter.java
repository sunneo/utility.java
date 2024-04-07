package com.example.sharp;

import com.example.sharp.annotations.FlattenArrayLengthName;
import com.example.sharp.annotations.FlattenArrayName;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;

public class IniWriter {

    protected volatile boolean bIsDisposed = false;

    public static class BasicWriter {
        public void WriteRaw(String line) {

        }

        public void Write(String key, String val) {

        }

        public void Close() {

        }

        public void Save() {

        }

        public void Reset() {

        }

    }

    public static class IniLineWriter extends BasicWriter {
        public String FileName;
        public ArrayList<String> Lines = new ArrayList<String>();

        public void WriteRaw(String line) {
            Lines.add(line);
        }

        public void Write(String key, String val) {
            Lines.add(key + "=" + val);
        }

        public void Save() {
            if (CString.IsNullOrEmpty(FileName)) return;
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(this.FileName))) {
                for (String s : Lines) {
                    wr.write(s + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        public void Close() {
            Save();
            Lines.clear();
            FileName = "";
        }

        public String toString() {
            StringBuilder strb = new StringBuilder();
            for (String s : Lines) {
                strb.append(s + "\n");
            }
            return strb.toString();
        }

        public IniLineWriter(String filename) {
            this.FileName = filename;
        }
    }

    public class DictionaryWriter extends BasicWriter {
        Map<String, Object> dict = null;

        public void WriteRaw(String line) {

        }

        public void Write(String key, String val) {
            dict.put(key, val);
        }

        public void Save() {

        }

        public void Close() {

        }

        public String toString() {
            StringBuilder strb = new StringBuilder();
            for (String s : dict.keySet()) {
                strb.append(s + "=" + dict.get(s) + "\n");
            }
            return strb.toString();
        }

        public DictionaryWriter(Map<String, Object> target) {
            this.dict = target;
        }
    }

    BasicWriter writer = new IniLineWriter(""); // default to ini line writer

    public BasicWriter StartIniLineWriter(String filename) {
        if (writer == null) {
            writer = new IniLineWriter(filename);
        } else if (writer != null && writer instanceof IniLineWriter) {
            ((IniLineWriter) writer).FileName = filename;
        }
        return writer;
    }

    public BasicWriter StartDictionaryWriter(Map<String, Object> dict) {
        if (writer == null) {
            writer = new DictionaryWriter(dict);
        }
        return writer;
    }


    public Dictionary<String, Object> GivenValue = new Dictionary<String, Object>();

    public static IniWriter Open(String file) {
        return Open(file, false);
    }

    public static IniWriter Open(String file, boolean appendFile) {
        IniWriter writer = new IniWriter();
        BasicWriter basicWriter = writer.StartIniLineWriter(file);
        if (new File(file).exists() && appendFile) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) break;
                    basicWriter.WriteRaw(line);
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return writer;
    }

    public static String SerializetoString(Object o) {
        try {
            IniWriter writer = new IniWriter();
            writer.StartIniLineWriter("");
            writer.Serialize(o);
            return writer.toString();
        }catch(Exception ee){
            ee.printStackTrace();
            return "";
        }
    }

    public static void SerializeToDictionary(Map<String, Object> dic, Object o) {
        IniWriter writer = new IniWriter();
        writer.StartDictionaryWriter(dic);
        writer.Serialize(o);
    }

    /// <summary>
/// serialize to a given writer
/// </summary>
/// <param name="basicWriter"></param>
/// <param name="o"></param>
    public static void SerializeToWriter(BasicWriter basicWriter, Object o) {
        IniWriter writer = new IniWriter();
        writer.writer = basicWriter;
        writer.Serialize(o);
    }

    public void WriteCategory(String category) {
        if (this.writer == null) return;
        this.writer.WriteRaw("[" + category + "]");
    }

    public void WriteComment(String content) {
        if (this.writer == null) return;
        BufferedReader reader = new BufferedReader(new StringReader(content));
        ArrayList<String> lines = new ArrayList<String>();
        int maxLength = 0;
        while (true) {
            try {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                maxLength = Math.max(maxLength, s.length());
                lines.add(s);
            } catch (Exception ee) {
                ee.printStackTrace();
                break;
            }
        }
        if (lines.size() == 1) {
            this.writer.WriteRaw("#  " + lines.get(0));
        } else {
            String separator = "###";
            this.writer.WriteRaw(separator);
            for (String s : lines) {
                this.writer.WriteRaw("#  " + s);
            }
            this.writer.WriteRaw(separator);
        }
    }

    public void Write(String key, String val) {
        if (this.writer == null) return;
        this.writer.Write(key, val);

    }

    public void Write(String key, boolean val) {
        this.Write(key, Boolean.toString(val));
    }

    public void Write(String key, int val) {
        this.Write(key, Integer.toString(val));
    }

    public void Write(String key, double val) {
        this.Write(key, Double.toString(val));
    }

    public void Write(String key, int[] val) {
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < val.length; ++i) {
            String v = Integer.toString(val[i]);
            strb.append(v);
            if (i + 1 < val.length) {
                strb.append(',');
            }
        }
        this.Write(key, strb.toString());
    }
    public void Write(String key, String[] val) {
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < val.length; ++i) {
            String v = "\""+String.valueOf(val[i])+"\"";
            strb.append(v);
            if (i + 1 < val.length) {
                strb.append(',');
            }
        }
        this.Write(key, strb.toString());
    }
    public void Write(String key, double[] val) {
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < val.length; ++i) {
            String v = Double.toString(val[i]);
            strb.append(v);
            if (i + 1 < val.length) {
                strb.append(',');
            }
        }
        this.Write(key, strb.toString());
    }


    protected void SerializeObject(Object ret, String prefix) {
        Class<?> t = ret.getClass();

        for (Field field : t.getFields()) {
            try {
                field.setAccessible(true);
                int modifier = field.getModifiers();
                if (Modifier.isPublic(modifier)) {
                    Class<?> fieldType = field.getType();
                    String name = prefix + field.getName();
                    Object val = field.get(ret);
                    if (Modifier.isTransient(modifier)) {
                        continue;
                    }
                    if (GivenValue != null && GivenValue.ContainsKey(name)) {
                        val = GivenValue.get(name);
                    }
                    if (fieldType.isPrimitive()) {

                        if (fieldType.equals(Integer.class)) {
                            Write(name, (int) val);
                        } else if (fieldType.equals(Boolean.class)) {
                            Write(name, (boolean) val);
                        } else if (fieldType.equals(Double.class)) {
                            Write(name, (double) val);
                        } else if (fieldType.isEnum()) {
                            Write(name, val.toString());
                        }
                    } else if (fieldType.equals(Integer.class)) {
                        Write(name, (int) val);
                    } else if (fieldType.equals(Boolean.class)) {
                        Write(name, (boolean) val);
                    } else if (fieldType.equals(Double.class)) {
                        Write(name, (double) val);
                    } else if (fieldType.isEnum()) {
                        Write(name, val.toString());
                    } else if (fieldType.equals(String.class)) {
                        Write(name, (String) val);
                    } else if (fieldType.isArray()) {
                        if (fieldType.getComponentType().equals(Integer.class)) {
                            Write(name, (int[]) val);
                        } else if (fieldType.getComponentType().equals(Double.class)) {
                            Write(name, (double[]) val);
                        } else if (fieldType.getComponentType().equals(String.class)) {
                            Write(name, (String[]) val);
                        } else {
                            // for case of
                            // SomeThing.Count=2
                            // SomeThing
                            String arrayLengthName="Count";
                            FlattenArrayLengthName arrayLengthNameTag = (FlattenArrayLengthName) field.getAnnotation(FlattenArrayLengthName.class);
                            if (arrayLengthNameTag != null && !CString.IsNullOrEmpty(arrayLengthNameTag.name())) {
                                arrayLengthName = arrayLengthNameTag.name();
                            }
                            if (!CString.IsNullOrEmpty(arrayLengthName)) {
                                Class<?> elementType = fieldType.getComponentType();
                                int len = Array.getLength(val);
                                Write(name+"."+arrayLengthName, len);

                                FlattenArrayName arrayName = (FlattenArrayName) field.getAnnotation(FlattenArrayName.class);
                                for (int i = 0; i < len; ++i) {
                                    String flattenArrayName = name + "[" + Integer.toString(i) + "].";
                                    if (arrayName != null && !CString.IsNullOrEmpty(arrayName.name()) && !CString.IsNullOrEmpty(arrayName.replacement())) {
                                        flattenArrayName = arrayName.name().replace(arrayName.replacement(), Integer.toString(i)) + ".";
                                    }
                                    SerializeObject(Array.get(val, i), flattenArrayName);
                                }
                            }
                        }
                    } else {
                        Object fieldContent = null;
                        fieldContent = field.get(ret);
                        if (fieldContent != null) {
                            WriteComment("Class:" + fieldType.getSimpleName() + "\n" +
                                    "FieldName:" + field.getName() + "\n");
                            SerializeObject(fieldContent, field.getName() + ".");
                        }
                    }
                }
            } catch (Exception ee) {

            }
        }
    }

    public void Serialize(Object o) {
        SerializeObject(o, "");
    }

    public void Save() {
        if (writer != null) {
            writer.Save();
        }

    }

    public void Close() {
        if (writer != null) {
            writer.Close();
        }

    }

    public void Dispose() {
        if (bIsDisposed) return;
        this.Close();
        bIsDisposed = true;
    }

    public String toString() {
        if (writer != null) {
            return writer.toString();
        }
        return "";
    }
}

