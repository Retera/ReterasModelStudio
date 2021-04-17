package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import java.awt.Component;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public abstract class AbstractObjectField implements EditableOnscreenObjectField {
	private final String displayName;
	private final String sortName;
	private final String rawDataName;
	private final boolean showingLevelDisplay;
	private final War3ID metaKey;
	private final int level;
	private final WorldEditorDataType dataType;
	private final GameObject metaDataField;

	public AbstractObjectField(final String displayName, final String sortName, final String rawDataName,
			final boolean showLevelDisplay, final War3ID metaKey, final int level,
			final MutableObjectData.WorldEditorDataType dataType, final GameObject metaDataField) {
		this.displayName = displayName;
		this.sortName = sortName;
		this.rawDataName = rawDataName;
		this.showingLevelDisplay = showLevelDisplay;
		this.metaKey = metaKey;
		this.level = level;
		this.dataType = dataType;
		this.metaDataField = metaDataField;
	}

	@Override
	public final String getDisplayName(final MutableGameObject gameUnit) {
		return displayName;
	}

	@Override
	public String getSortName(final MutableGameObject gameUnit) {
		return sortName;
	}

	@Override
	public boolean isShowingLevelDisplay() {
		return showingLevelDisplay;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public final String getRawDataName() {
		return rawDataName;
	}

	@Override
	public final Object getValue(final MutableGameObject gameUnit) {
		return getValue(gameUnit, metaKey, level);
	}

	protected abstract Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level);

	@Override
	public boolean popupEditor(final MutableGameObject gameUnit, final Component parent, final boolean editRawData,
			final boolean disableLimits) {
		String worldEditValueStringKey = switch (dataType) {
			case ABILITIES -> "WESTRING_AE_DLG_EDITVALUE";
			case BUFFS_EFFECTS -> "WESTRING_FE_DLG_EDITVALUE";
			case DESTRUCTIBLES -> "WESTRING_BE_DLG_EDITVALUE";
			case DOODADS -> "WESTRING_DE_DLG_EDITVALUE";
			case ITEM -> "WESTRING_IE_DLG_EDITVALUE";
			case UPGRADES -> "WESTRING_GE_DLG_EDITVALUE";
			case UNITS -> "WESTRING_UE_DLG_EDITVALUE";
		};
		final String defaultDialogTitle = WEString.getString(worldEditValueStringKey);
		return popupEditor(gameUnit, parent, editRawData, disableLimits, metaKey, level, defaultDialogTitle,
				metaDataField);
	}

	@Override
	public boolean hasEditedValue(final MutableGameObject gameUnit) {
		return gameUnit.hasCustomField(metaKey, level);
	}

	protected abstract boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
			boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle, GameObject metaDataField);

}
