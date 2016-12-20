package com.matrixeater.src;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Write a description of class ObjectId here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public abstract class IdObject implements Named
{
    String name;
    Vertex pivotPoint;
    int objectId = -1;
    int parentId = -1;
    IdObject parent;
    public void setName(String text)
    {
        name = text;
    }
    public String getName()
    {
        return name;
    }
    public IdObject()
    {
        
    }
    public IdObject( IdObject host )
    {
        name = host.name;
        pivotPoint = host.pivotPoint;
        objectId = host.objectId;
        parentId = host.parentId;
        parent = host.parent;
    }
    public static IdObject read(BufferedReader mdl)
    {
        return null;
    }
    public abstract void printTo(PrintWriter writer);
    public void setPivotPoint(Vertex p)
    {
        pivotPoint = p;
    }
    public void setParent(IdObject p)
    {
        parent = p;
    }
    public IdObject copy()
    {
        return null;
    }
    
    public boolean childOf(IdObject other)
    {
    	if( parent != null )
    		if( parent == other )
    			return true;
    		else
    			return parent.childOf(other);
    	return false;
    }
    
    public boolean parentOf(IdObject other, HashMap<IdObject,ArrayList<IdObject>> childMap)
    {
    	ArrayList<IdObject> children = childMap.get(this);
    	if( children != null )
    		if( children.contains(other) )
    			return true;
    		else
    		{
    			boolean deepChild = false;
    			for( int i = 0; !deepChild && i < children.size(); i++ )
    			{
    				deepChild = children.get(i).parentOf(other, childMap);
    			}
    			return deepChild;
    		}
    	return false;
    }
    
    public ArrayList<IdObject> getAllChildren(HashMap<IdObject,ArrayList<IdObject>> childMap)
    {
    	ArrayList<IdObject> children = childMap.get(this);
    	ArrayList<IdObject> allChildren = new ArrayList<IdObject>();
    	if( children != null )
    	{
    		for(int i = 0; i < children.size(); i++ )
    		{
    			IdObject child = children.get(i);
    			if( !allChildren.contains(child) )
    			{
    				allChildren.add(child);
    				allChildren.addAll(child.getAllChildren(childMap));
    			}
    		}
    	}
    	
    	return allChildren;
    }
    
    public abstract void flipOver(byte axis);
}
