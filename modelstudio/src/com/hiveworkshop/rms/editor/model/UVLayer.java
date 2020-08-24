package com.hiveworkshop.rms.editor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A layer of TVertices (UV Mapping)
 * 
 * Eric Theller
 * 3/10/2012
 */
public class UVLayer
{
    List<TVertex> tverts;
    public UVLayer()
    {
        tverts = new ArrayList<TVertex>();
    }
    public void addTVertex(TVertex v)
    {
        tverts.add(v);
    }
    public TVertex getTVertex(int vertId)
    {
        return tverts.get(vertId);
    }
    public int numTVerteces()
    {
        return tverts.size();
    }
}
