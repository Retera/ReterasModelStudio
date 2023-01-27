package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;

import javax.swing.*;
import java.util.Objects;
import java.util.function.Supplier;

public class ChoosableDisplayElement<T> {
	protected final ModelView modelView;
	private final int id;
	protected final T item;
	private ImageIcon icon;
	Supplier<String> namingFunk;
	DisplayElementType type;

	public ChoosableDisplayElement(DisplayElementType type, ModelView modelView, T item) {
		this(type, modelView, item, null, -1);
	}

	public ChoosableDisplayElement(DisplayElementType type, ModelView modelView, T item, int id) {
		this(type, modelView, item, null, id);
	}

	public ChoosableDisplayElement(DisplayElementType type, ModelView modelView, T item, Supplier<String> namingFunk) {
		this(type, modelView, item, namingFunk, -1);
	}

	public ChoosableDisplayElement(DisplayElementType type, ModelView modelView, T item, Supplier<String> namingFunk, int id) {
		this.modelView = modelView;
		this.type = type;
		this.id = id;
		this.item = item;
		this.icon = type.getIcon();
		this.namingFunk = namingFunk;
	}

	public ChoosableDisplayElement<T> setNameFunc(Supplier<String> namingFunk) {
		this.namingFunk = namingFunk;
		return this;
	}

	public ChoosableDisplayElement<T> setIcon(ImageIcon icon) {
		this.icon = icon;
		return this;
	}

	public ChoosableDisplayElement<T> select(ComponentsPanel componentsPanel) {
		select(item, componentsPanel);
		return this;
	}

	public void mouseEntered() {
		if (item instanceof Geoset) {
			modelView.highlightGeoset((Geoset) item);
		} else if (item instanceof IdObject) {
			modelView.highlightNode((IdObject) item);
		}
	}

	public void mouseExited() {
		if (item instanceof Geoset) {
			modelView.unhighlightGeoset((Geoset) item);
		} else if (item instanceof IdObject) {
			modelView.unhighlightNode((IdObject) item);
		}
	}

	protected ChoosableDisplayElement<T> select(T item, ComponentsPanel panel) {
		panel.setSelectedPanel(item, type);
		return this;
	}

	@Override
	public String toString() {
		return getName(item, modelView);
	}

	protected String getName(T item, ModelView modelViewManager) {
		if (namingFunk != null) {
			return namingFunk.get();
		}
//		return "";
		String idString = id == -1 ? "" : "# " + id + ": ";
		if(item instanceof String){
			return idString + item;
		} else if(item instanceof Named){
			return idString + ((Named) item).getName();
		} else {
			return idString + item.toString();
		}
	}

	public boolean hasSameItem(ChoosableDisplayElement<?> other) {
		return (getClass() == other.getClass())
				&& (Objects.equals(item, other.item)
				&& (this.id == other.id));
	}

	public ImageIcon getIcon(boolean expanded) {
		return icon;
	}

	public T getItem() {
		return item;
	}
}