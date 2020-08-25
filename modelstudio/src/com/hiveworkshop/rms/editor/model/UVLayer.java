package com.hiveworkshop.rms.editor.model;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.util.Vertex2;

/**
 * A layer of TVertices (UV Mapping)
 * 
 * Eric Theller
 * 3/10/2012
 */
public class UVLayer
{
    List<Vertex2> tverts;
    public UVLayer()
    {
        tverts = new ArrayList<Vertex2>();
    }
    public void addTVertex(Vertex2 v)
    {
        tverts.add(v);
    }
    public Vertex2 getTVertex(int vertId)
    {
        return tverts.get(vertId);
    }
    public int numTVerteces()
    {
        return tverts.size();
    }
}
