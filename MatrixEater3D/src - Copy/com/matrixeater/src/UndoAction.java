package com.matrixeater.src;

/**
 * Write a description of class UndoAction here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public abstract class UndoAction
{
    public abstract void undo();
    public abstract void redo();
    public abstract String actionName();
}
