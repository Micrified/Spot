/*
 * Copyright (c) 1997 E.J.Dijkstra/R.Smedinga. All Rights Reserved.
 *
 * ideas from:
 * Gary Cornell and Cay S. Horstmann, Core Java (Book/CD-ROM)
 * Published By SunSoft Press/Prentice-Hall
 * Copyright (C) 1996 Sun Microsystems Inc.
 * All Rights Reserved. ISBN 0-13-596891-7
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * This Java source code is part of a course on Object Oriented Techniques
 * developed by E.J.Dijkstra and R.Smedinga for PTS Software, Bussum.
 */
/**
 * @version 1.0
 * @author R.Smedinga@cs.rug.nl
 */

/* Adapted by Charles Randolph & Barnabas Busa */

import java.io.*;
import java.util.*;

/* This class should be used with an ObjectTable to write objects to a stream */
public class PersistentOutputStream extends PrintStream {

    /* Properties */
    private ObjectTable table;
    
    /* Setters */
    public void setTable(ObjectTable table) {
        this.table = table;
    }
    
    /* Constructor */
    public PersistentOutputStream(OutputStream fs) {
        super(fs);
    }
    
    /* Interface */
    
    /* Write's a persistent object to stream */
    public void writePersistent(String s, Persistent x) {
        print(s);
        
        /* If writing a null object */
        if (x == null){
            print("-");
            return;
        }
        int n = this.table.find(x);
        
        /* If n is already in the table */
        if (n < 0){
            writeObject(x);
            return;
        }
        print(n);
    }
    
    /* Write an integer */
    public void writeInt(String s, int n) {
        print(s + n);
    }
    
    /* Write a double */
    public void writeDouble(String s, double n) {
        print(s + n);
    }
    
    /* Write a string */
    public void writeString(String s, String x) {
        print(s);
        if (x == null){ print("-"); }
        StringBuffer b = new StringBuffer(s.length() + x.length() + 2);
        print('"');
        for (int i = 0; i < x.length(); i++){
            char ch = x.charAt(i);
            if (ch == '\\' || ch == '"'){
                print('\\');
            }
            print((char)ch);
        }
        print('"');
    }
    
    /* Write an arrayList, the first data-field will contain the size of the arrayList */
    public void writePersistentArrayList(String s, ArrayList a) {
        print(s);
        writeInt("ArrayList[",a.size());
        for (int i = 0; i < a.size(); i++){
            Persistent x = (Persistent)a.get(i);
            writePersistent(",",x);
        }
        print("]");
    }
    
    /* Write a vector, the first data-field will contain the size of the vector */
    public void writePersistentVector(String s, Vector e) {
        print(s);
        writeInt("Vector[",e.size());
        for (int i = 0; i < e.size(); i++){
            Persistent x = (Persistent)e.elementAt(i);
            writePersistent(",", x);
        }
        print("]");
    }
    
    /* Write a hashtable. First field is the object size. The elements occur in tuples of form (Key, Object) */
    public void writePersistentHashtable(String s, Hashtable e) {
        print(s);
        writeInt("Hashtable[", e.size());
        Enumeration kk = e.keys();
        while (kk.hasMoreElements()){
            String key = (String)kk.nextElement();
            Persistent x = (Persistent)e.get(key);
            writeString(",", key);
            writePersistent(",", x);
        }
        print("]");
    }
    
    /* Write an objectTable */
    public void writeObjectTable(ObjectTable ot) {
        Vector objects = ot.getObjects();
        print("ObjectTable[");
        print(objects.size());
        for (int i = 0; i < objects.size(); i++) {
            writeString(",\nclass=", objects.elementAt(i).getClass().getName());
        }
        print("]\n");
    }
    
    /* Write a start-tag for object-data */
    public void writeObjectsStart() {
        print("Objects[\n");
    }
    
    /* Write an end-tag for object-data */
    public void writeObjectsEnd() {
        print("]\n");
    }
    
    /* Write an object using the persistent-interface method */
    public void writeObject(Persistent x) {
        print( ((Object)x).getClass().getName());
        print("[\n");
        x.save(this);
        print("]\n");
    }
}
