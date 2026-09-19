package com.hiveworkshop.wc3.gui.modeledit.wizards;

import java.awt.Component;

import com.hiveworkshop.wc3.gui.modeledit.ModelPanel;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;

/**
 * What the Add menu wizards need from the main window: the model being edited,
 * where to report structural changes, and how to show the result.
 */
public interface WizardHost {
	/** The active model panel, or null when no model is open. */
	ModelPanel currentModelPanel();

	ModelStructureChangeListener structureListener();

	ModelComponentNavigationListener navigation();

	Component dialogParent();
}
