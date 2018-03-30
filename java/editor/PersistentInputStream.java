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

/* This class is to be used with our ObjectTable to read objects from a stream */
public class PersistentInputStream extends PushbackInputStream {

    /* Properties */
    private ObjectTable table;
    
    /* Setters */
    public void setTable(ObjectTable table) {
        this.table = table;
    }
    
    /* Constructor */
    public PersistentInputStream(InputStream fs) {
        super(fs);
    }
    
    /* Reads in a persistent object. If already initialized, the data is connected to the object. Else, it is read in entirely */
    public Persistent readPersistent(String skip) throws IOException {
        skipString(skip);
        int ch = read();
        
        /* If a 'null' was saved originally and got retreived */
        if (ch == '-'){
            return null;
        }
        unread(ch);
        
        /* If an integer can be retreived, and exists in the table. Return that object */
        int n;
        if (Character.isDigit((char)ch)){
            n = Format.atoi(getToken());
            return this.table.find(n);
        }
        
        /* If not, initialize the new Object */
        String className = getToken();
        skipString("[");
        Persistent x;
        try {
            Class newClass = Class.forName(className);
            Object object = newClass.newInstance();
            x = (Persistent)object;
        } catch (Exception ex) {
            throw new IOException(ex.toString());
        }
        /* Load information into new initialized class */
        x.load(this);
        skipString("]");
        return x;
    }
    
    /* Read in an integer */
    public int readInt(String skip) throws IOException {
        skipString(skip);
        String s = getToken();
        return Format.atoi(s);
    }
    
    /* Read in a double */
    public double readDouble(String skip) throws IOException {
        skipString(skip);
        String s = getToken();
        return Format.atof(s);
    }
    
    /* Read in a String */
    public String readString(String skip) throws IOException {
        skipString(skip);
        skipWhite();
        int ch = read();
        if (ch == '0'){
            return null;
        }
        if (ch != '"'){
            throw new IOException("String expected!\n");
        }
        StringBuffer b = new StringBuffer(100);
        while (true) {
            ch = read();
            if (ch == -1 || ch == '"'){
                return new String(b);
            }
            if (ch == '\\'){
                ch = read();
            }
            b.append((char)ch);
        }
    }
    
    /* Read in an ArrayList. The first data-field must contain its size */
    public ArrayList readPersistentArrayList(String skip) throws IOException {
        skipString(skip);
        int n = readInt("ArrayList[");
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++){
            Persistent x = readPersistent(",");
            a.add(x);
        }
        skipString("]");
        return a;
    }
    
    /* Read in a Vector. The first data-field must contain its size */
    public Vector readPersistentVector(String skip) throws IOException {
        skipString(skip);
        int n = readInt("Vector[");
        Vector v = new Vector(n);
        for (int i = 0; i < n; i++){
            Persistent x = readPersistent(",");
            v.addElement(x);
        }
        skipString("]");
        return v;
    }
    
    /* Read in a hashtable. The first data-field must contain its size. Elements must occur in tuples: (First key, then object) */
    public Hashtable readPersistentHashtable(String skip) throws IOException {
        skipString(skip);
        int n = readInt("Hashtable[");
        Hashtable v = new Hashtable();
        for (int i = 0; i < n; i++){
            String key = readString(",");
            Persistent x = readPersistent(",");
            v.put(key, x);
        }
        skipString("]");
        return v;
    }
    
    /* Read in an Object-Table */
    public Vector readObjectTable() throws IOException {
        System.out.println("Restoring table...\n");
        int n = readInt("ObjectTable[");
        System.out.println("Number of objects: " + n);
        Vector v = new Vector(n);
        for (int i = 0; i < n; i++){
            skipString(",");
            String className = readString("class=");
            System.out.print("'" + className + "'");
            
            try {
                Class newClass = Class.forName(className);
                Object x = newClass.newInstance();
                System.out.println("success");
                v.addElement(x);
            } catch (Exception ex) {
                throw new IOException(ex.toString());
            }
        }
        skipString("]");
        return v;
    }
    
    /* The very popular string-skipper function! (Throws IOException if can't match the skip) */
    public void skipString(String s) throws IOException {
        skipWhite();
        int i = 0;
        while (i < s.length()){
            int ch;
            ch = read();
            if (ch != s.charAt(i)){
                throw new IOException(s + " expected, but got something else: " + s.charAt(i) + " vs " + (char)ch + "\n");
            }
            i++;
        }
    }
    
    /* Skip the starting tag for object-data */
    public void readObjectsStart() throws IOException {
        skipString("Objects[");
    }
    
    /* Skip the ending tag for object-data */
    public void readObjectsEnd() throws IOException {
        skipString("]");
    }
    
    /* Read in an object. Use the persistent-interface to read its data */
    public void readObject(Persistent x) throws IOException {
        skipString( ((Object)x).getClass().getName() );
        skipString("[");
        System.out.printf("Retreiving: " + ((Object)x).getClass().getName() + "... ");
        x.load(this);
        skipString("]");
        System.out.printf("-Success!-\n");
    }
    
    /* Read's in a token until it hits a delimiter */
    private String getToken() throws IOException {
        skipWhite();
        StringBuffer b = new StringBuffer(100);
        
        while (true) {
            int ch;
            ch = read();
            if (ch == -1 || Character.isSpaceChar((char)ch) || ch == ',' || ch == '[' || ch == ']'){
                unread(ch);
                return new String(b);
            }
            if (ch == '\\'){
                ch = read();
            }
            b.append((char)ch);
        }
        
    }
    
    /* Skip's white-space in a stream */
    private void skipWhite() throws IOException {
        boolean more = true;
        int ch;
        do {
            ch = read();
        } while (ch != -1 && (Character.isSpaceChar((char)ch) || ch == '\n'));
        unread(ch);
    }

}
