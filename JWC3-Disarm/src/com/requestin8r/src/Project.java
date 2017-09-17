package com.requestin8r.src;

import java.awt.Image;
import java.util.LinkedList;
import java.util.Queue;

import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class Project {
	Queue<Operation> operations = new LinkedList<>();
	public String name;
	public Image icon;
	public ModelViewManager model;

	public Project(final ModelViewManager model, final String name, final Image icon) {
		this.model = model;
		this.name = name;
		this.icon = icon;
	}

	public Project(final ModelViewManager model, final Image icon, final String name) {
		this.model = model;
		this.name = name;
		this.icon = icon;
	}
}
