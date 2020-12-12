package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;

import javax.swing.*;
import java.util.Objects;

abstract class ChooseableDisplayElement<T> {
	protected final ModelViewManager modelViewManager;
	protected final T item;
	private ImageIcon icon;

	public ChooseableDisplayElement(final ImageIcon icon, final ModelViewManager modelViewManager, final T item) {
		this.modelViewManager = modelViewManager;
		this.item = item;
		this.icon = icon;
	}

	public void setIcon(final ImageIcon icon) {
		this.icon = icon;
	}

	public void select(final ComponentsPanel componentsPanel) {
		select(item, componentsPanel);
	}

	public void mouseEntered() {
	}

	public void mouseExited() {
	}

	protected void select(T item, ComponentsPanel panel) {
		panel.setSelectedPanel(item);

	}

	@Override
	public String toString() {
		return getName(item, modelViewManager);
	}

	protected abstract String getName(T item, ModelViewManager modelViewManager);

	public boolean hasSameItem(final ChooseableDisplayElement<?> other) {
		return (getClass() == other.getClass())
				&& (Objects.equals(item, other.item));
	}

	public ImageIcon getIcon(final boolean expanded) {
		return icon;
	}
}