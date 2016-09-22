package com.matrixeater.src;

/**
 * Something to keep track of which stuff has visibility, 
 * 
 * Eric Theller
 * 6/28/2012
 */
public interface VisibilitySource extends Named
{
    public AnimFlag getVisibilityFlag();
    public void setVisibilityFlag(AnimFlag what);
//     public String getVisTagname();
    public String visFlagName();
}
