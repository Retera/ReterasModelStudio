package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;

import javax.swing.*;
import java.util.Objects;

abstract class ChooseableDisplayElement<T> {
	protected final ModelViewManager modelViewManager;
	protected final T item;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private ImageIcon icon;

	public ChooseableDisplayElement(final ModelViewManager modelViewManager,
	                                final UndoActionListener undoActionListener,
	                                final ModelStructureChangeListener modelStructureChangeListener, final T item) {
		this(null, modelViewManager, undoActionListener, modelStructureChangeListener, item);
	}

	public ChooseableDisplayElement(final ImageIcon icon, final ModelViewManager modelViewManager,
	                                final UndoActionListener undoActionListener,
	                                final ModelStructureChangeListener modelStructureChangeListener, final T item) {
		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.item = item;
		this.icon = icon;
	}

	public void setIcon(final ImageIcon icon) {
		this.icon = icon;
	}

	public void select(final ModelComponentTreeListener listener) {
		select(item, modelViewManager, undoActionListener, modelStructureChangeListener, listener);
	}

	public void mouseEntered() {
	}

	public void mouseExited() {
	}

	protected void select(T item, ModelViewManager modelViewManager,
	                      UndoActionListener undoListener, ModelStructureChangeListener modelStructureChangeListener,
	                      ModelComponentTreeListener listener) {
		listener.uggPanel();

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