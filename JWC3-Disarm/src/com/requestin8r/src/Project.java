package com.requestin8r.src;

import java.awt.Image;
import java.util.LinkedList;
import java.util.Queue;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.GeosetVertexModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.GeosetVertexSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.user.SaveProfile;

public class Project {
	Queue<Operation> operations = new LinkedList<>();
	public String name;
	public Image icon;
	public ModelViewManager model;
	public ModelEditor editor;

	public Project(final ModelViewManager model, final String name, final Image icon) {
		this.model = model;
		this.name = name;
		this.icon = icon;
		editor = new GeosetVertexModelEditor(model, SaveProfile.get().getPreferences(),
				new GeosetVertexSelectionManager());
	}

	public Project(final ModelViewManager model, final Image icon, final String name) {
		this.model = model;
		this.name = name;
		this.icon = icon;
		editor = new GeosetVertexModelEditor(model, SaveProfile.get().getPreferences(),
				new GeosetVertexSelectionManager());
	}
}
