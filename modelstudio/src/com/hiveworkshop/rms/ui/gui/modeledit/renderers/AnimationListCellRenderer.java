package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.UiElementColor;
import com.hiveworkshop.rms.ui.preferences.UiElementColorPrefs;
import net.infonode.util.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AnimationListCellRenderer extends DefaultListCellRenderer {
	private final boolean showLength;
	private final Function<Object, ListStatus> statusFunction;
	private final BiFunction<Object, Boolean, Boolean> isSelected;
	private Function<Object, Integer> oldIndexFunc;
	private static final String uArrow = " \u2b9d  ";
	private static final String dArrow = " \u2b9f  ";
	private static final String dash   = " \u2012  ";

	public AnimationListCellRenderer(boolean showLength,
	                                 Function<Object, ListStatus> statusFunction) {
		this(showLength, statusFunction, (o, s) -> s);
	}

	public AnimationListCellRenderer(boolean showLength,
	                                 Function<Object, ListStatus> statusFunction,
	                                 BiFunction<Object, Boolean, Boolean> isSelected) {
		this.showLength = showLength;
		this.statusFunction = statusFunction;
		this.isSelected = isSelected;
	}

	public AnimationListCellRenderer setOldIndexFunc(Function<Object, Integer> oldIndexFunc) {
		this.oldIndexFunc = oldIndexFunc;
		return this;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, getAnimName(value, index), index, isSelected.apply(value, isSel), hasFoc);

		ListStatus status = statusFunction.apply(value);
		switch (status) {
			case FREE -> {}
			case MODIFIED, ONE_OF_MODIFIED, UNAVAILABLE -> this.setBackground(getBgCol(status));
			case DISABLED -> setEnabled(false);
		}

		return this;
	}

	public Color getBgCol(ListStatus status) {
		UiElementColorPrefs colorPrefs = ProgramGlobals.getPrefs().getUiElementColorPrefs();

		return switch (status) {
			case FREE -> null;
			case MODIFIED -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_EDITED), .5);
			case ONE_OF_MODIFIED -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_EDITED), .3);
			case UNAVAILABLE -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_UNAVAILABLE), .5);
			case DISABLED -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_DISABLED), .5);
		};
	}

	private String getAnimName(Object value, int index) {
		if (value instanceof Animation animation) {
			String animName = getArrowPrefix(value, index) + animation.getName();
			return animName + (showLength ? ("  " + animation.getLength()) : "");
		}
		return "";
	}

	private String getArrowPrefix(Object o, int index) {
		Integer oldInd = oldIndexFunc == null ? null : oldIndexFunc.apply(o);
		oldInd = oldInd == null ? index : oldInd;
		if (oldInd < index) {
			return dArrow;
		} else if (index < oldInd) {
			return uArrow;
		}
		return dash;
	}
}
