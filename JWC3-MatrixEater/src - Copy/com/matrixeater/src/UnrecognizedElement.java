package com.matrixeater.src;
import java.util.ArrayList;
/**
 * A class for things which I don't and/or forget to code.
 * 
 * Eric Theller
 * 11/10/2011
 */
public class UnrecognizedElement
{
    private ArrayList<String> m_lines;
    public UnrecognizedElement()
    {
        m_lines = new ArrayList<String>();
    }
    public int size()
    {
        return m_lines.size();
    }
    public void addLine(String line)
    {
        m_lines.add(line);
    }
    public String getLine(int i)
    {
        return m_lines.get(i);
    }
    public String [] getAll()
    {
        return (String[])m_lines.toArray();
    }
}
