package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.AnimShell;
import com.hiveworkshop.rms.ui.preferences.UiElementColor;
import com.hiveworkshop.rms.ui.preferences.UiElementColorPrefs;
import com.hiveworkshop.rms.ui.util.colorchooser.CharIcon;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.util.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AnimListCellRenderer extends DefaultListCellRenderer {
	protected static final Vec3 recCV = new Vec3(200, 255, 255);
	protected static final Vec3 donCV = new Vec3(220, 180, 255);
	protected static final Color recC = recCV.asIntColor();
	protected static final Color donC = donCV.asIntColor();


	boolean showLength;
	private final Function<AnimShell, ListStatus> statusFunction;
	private final BiFunction<AnimShell, Boolean, Boolean> isSelected;
	private static final String reverseMarker = " \u21a9"; // ↩


	public AnimListCellRenderer() {
		this(false);
	}

	public AnimListCellRenderer(boolean showLength) {
		this(showLength, o -> ListStatus.FREE, (o, s) -> s);
	}
	public AnimListCellRenderer(boolean showLength,
	                            Function<AnimShell, ListStatus> statusFunction,
	                            BiFunction<AnimShell, Boolean, Boolean> isSelected) {
		this.showLength = showLength;
		this.statusFunction = statusFunction;
		this.isSelected = isSelected;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value1, int index, boolean isSel, boolean hasFoc) {
		if (value1 instanceof AnimShell) {
			AnimShell value = (AnimShell) value1;
			String suffix = value.isReverse() ? reverseMarker : "";
			String animName = value.getDisplayName() + suffix;

			super.getListCellRendererComponent(list, animName, index, isSelected.apply(value, isSel), hasFoc);
			setText(animName);

			ListStatus status = statusFunction.apply(value);
			switch (status){
				case FREE -> {}
				case MODIFIED, ONE_OF_MODIFIED, UNAVAILABLE -> this.setBackground(getBgCol(status));
				case DISABLED -> setEnabled(false);
			}

			boolean fD = value.isFromDonating();
			CharIcon charIcon = new CharIcon(fD ? "&" : "#", 7, fD ? donC : recC, 16, 2);
			setIcon(charIcon); // todo choose icon based on import status

			setToolTipText(getHoverText(value));
		}
		return this;
	}


	public Color getBgCol(ListStatus status) {
		UiElementColorPrefs colorPrefs = ProgramGlobals.getPrefs().getUiElementColorPrefs();

		return switch (status){
			case FREE -> null;
			case MODIFIED -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_EDITED), .5);
			case ONE_OF_MODIFIED -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_EDITED), .3);
			case UNAVAILABLE -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_UNAVAILABLE), .5);
			case DISABLED -> ColorUtil.blend(getBackground(), colorPrefs.getColor(UiElementColor.LIST_ENTRY_DISABLED), .5);
		};
	}

	private String getHoverText(AnimShell value) {
		List<String> users = new ArrayList<>();
		for (AnimShell animShell : value.getAnimDataDests()) {
			boolean fD = animShell.isFromDonating();
//			users.add((fD ? "[&]" : "[#]") + "\"" + animShell.getName() + "\"");
			users.add((fD ? "[&]" : "[#]") + animShell.getName());
		}
		if (!users.isEmpty()){
			return "Used by: " + String.join(", ", users);
		}
		return null;
	}

	private String getAnimName(Object value){
		if (value instanceof AnimShell) {
			AnimShell animShell = (AnimShell) value;
			String suffix = animShell.isReverse() ? " \u21a9" : ""; // ↩
			return animShell.getDisplayName() + suffix;
		}
		return "";
	}
}
