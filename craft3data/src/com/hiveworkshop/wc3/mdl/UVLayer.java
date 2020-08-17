package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;

/**
 * A layer of TVertices (UV Mapping)
 * 
 * Eric Theller
 * 3/10/2012
 */
public class UVLayer
{
    ArrayList<TVertex> tverts;
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
