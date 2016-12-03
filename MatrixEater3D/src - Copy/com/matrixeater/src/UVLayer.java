package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
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
    public static UVLayer read(BufferedReader mdl)
    {
        UVLayer temp = new UVLayer();
        String line = "";
        while( !((line = MDLReader.nextLine(mdl)).contains("\t}") ) )
        {
            temp.addTVertex(TVertex.parseText(line));
        }
        return temp;
    }
    public void printTo(PrintWriter writer, int tabHeight, boolean addHeader)
    {
        //Here we may assume the header "TVertices" to already have been written,
        // based on addHeader
        String tabs = "";
        for( int i = 0; i < tabHeight; i++ )
        {
            tabs = tabs + "\t";
        }
        String inTabs = tabs;
        if( addHeader )
        {
            inTabs = inTabs + "\t";
            writer.println(tabs + "TVertices "+tverts.size() +" {");
        }
        for( int i = 0; i < tverts.size(); i++ )
        {
            writer.println(inTabs+tverts.get(i).toString()+",");
        }
       
        if( addHeader )
        {
            writer.println(tabs+"}");
        }
    }
}
