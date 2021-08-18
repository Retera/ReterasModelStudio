package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public abstract class ModelDependentView extends View {

	public ModelDependentView(String s, Icon icon, Component component) {
		super(s, icon, component);
	}

	public abstract ModelDependentView setModelPanel(ModelPanel modelPanel);

	public ModelDependentView preferencesUpdated() {
		return this;
	}

	public ModelDependentView reload() {
		return this;
	}
}
