/*
 * Copyright (c) 1997 E.J.Dijkstra/R.Smedinga. All Rights Reserved.
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
import java.lang.*;

public class ObjectTable {

    /* Properties */
    private Vector objects = new Vector();
    
    /* Accessors */
    public Vector getObjects(){
        return this.objects;
    }
    
    /* Interface */
    
    /* Register Object if not present in the table (No connections are used) */
    public void registerObject(Persistent obj){
        if (!contains(obj)){
            objects.addElement((Object)obj);
            obj.registerConnectionsInTable(this);
        }
    }
    
    /* Save the object table to a file. Write all objects in order for which they will appear */
    public void saveToFile(String fileName){
        try {
            PersistentOutputStream os = new PersistentOutputStream(new FileOutputStream(fileName));
            os.setTable(this);
            storeObjectTableOn(os);
            storeObjectsOn(os);
        } catch (IOException ex) {
            System.out.println("Exception while saving to: " + fileName + ": " + ex);
        }
    }
    
    /* Restores an object-table from a file. Reads in objects in the same order for which they appear in the table */
    public void restoreFromFile(String fileName) throws IOException {
        try {
            PersistentInputStream is = new PersistentInputStream(new FileInputStream(fileName));
            is.setTable(this);
            restoreObjectTableFrom(is);
            restoreObjectsFrom(is);
        } catch (IOException ex) {
            System.out.println("Exception while restoring from: " + fileName + ": " + ex);
            throw new IOException();
        }
    }
    
    private void storeObjectTableOn(PersistentOutputStream os) {
        os.writeObjectTable(this);
    }
    
    private void restoreObjectTableFrom(PersistentInputStream is) throws IOException {
        objects = is.readObjectTable();
    }
    
    private void storeObjectsOn(PersistentOutputStream os) {
        System.out.println("Storing objects...\n");
        os.writeObjectsStart();
        for (int i = 0; i < objects.size(); i++){
            os.writeObject((Persistent)objects.elementAt(i));
        }
        os.writeObjectsEnd();
    }
    
    private void restoreObjectsFrom(PersistentInputStream is) throws IOException {
        System.out.println("Restoring objects...\n");
        try {
            is.readObjectsStart();
            for (int i = 0; i < objects.size(); i++){
                is.readObject((Persistent)objects.elementAt(i));
            }
            is.readObjectsEnd();
        } catch (Exception ex) {
            System.out.println("Exception while reading object: " + ex);
            throw new IOException();
        }
    }
    
    /* Checks if Persistent object 'x' is present within the objectTable */
    public boolean contains(Persistent x) {
        if (find(x) == -1){
            return false;
        }
        return true;
    }
    
    /* Returns the identification number of the object if present. Else -1 */
    public int find(Persistent x){
        for (int i = 0; i < objects.size(); i++){
            if (objects.elementAt(i) == x){
                return i;
            }
        }
        return -1;
    }
    
    /* Returns persistent object in table at index i. Null of not-found */
    public Persistent find(int i){
        if (i > -1 && i < objects.size()){
            return (Persistent)(objects.elementAt(i));
        }
        return null;
    }
}
