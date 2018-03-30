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

/* Adapted by Charles Randolph and Barnabas Busa */

import java.io.*;

public abstract interface Persistent {
    
    /* Registers all persistent connections */
    public void registerConnectionsInTable (ObjectTable ot) ;
    
    /* Stores persistent attributes to a stream */
    public void save(PersistentOutputStream os);
    
    /* Restores persistent attributes from a stream */
    public void load(PersistentInputStream is) throws IOException;
}
