package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;

public abstract class AbstractModelEditor<T> implements ModelEditor<T> {
	protected final Set<T> selection;

	public AbstractModelEditor() {
		this.selection = new HashSet<>();
	}

	@Override
	public final Set<T> getSelection() {
		return selection;
	}

	@Override
	public final void setSelectedRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<T> itemsInArea = genericSelect(region, coordinateSystem);
		selection.clear();
		selection.addAll(itemsInArea);
	}

	@Override
	public final void removeSelectedRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<T> itemsInArea = genericSelect(region, coordinateSystem);
		selection.removeAll(itemsInArea);
	}

	@Override
	public final void addSelectedRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<T> itemsInArea = genericSelect(region, coordinateSystem);
		selection.addAll(itemsInArea);
	}

	protected abstract List<T> genericSelect(final Rectangle region, final CoordinateSystem coordinateSystem);

}
