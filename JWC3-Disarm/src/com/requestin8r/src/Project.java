package com.requestin8r.src;

import java.awt.Image;
import java.util.LinkedList;
import java.util.Queue;

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;

public class Project {
	Queue<Operation> operations = new LinkedList<Operation>();
	public String name;
	public Image icon;
	public MDLDisplay model;
	public Project(final MDLDisplay model, final String name, final Image icon) {
		this.model = model;
		this.name = name;
		this.icon = icon;
	}
	public Project(final MDLDisplay model, final Image icon, final String name) {
		this.model = model;
		this.name = name;
		this.icon = icon;
	}
}
