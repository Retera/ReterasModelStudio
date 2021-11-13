package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.text.ParseException;

public abstract class AbstractObjectField {
	private final String displayName;
	private final String sortName;
	private final String rawDataName;
	private final boolean showingLevelDisplay;
	private final War3ID metaKey;
	private final int level;
	private final WorldEditorDataType dataType;
	private final GameObject metaDataField;

	public AbstractObjectField(String displayName, String sortName, String rawDataName,
	                           boolean showLevelDisplay, War3ID metaKey, int level,
	                           WorldEditorDataType dataType, GameObject metaDataField) {
		this.displayName = displayName;
		this.sortName = sortName;
		this.rawDataName = rawDataName;
		this.showingLevelDisplay = showLevelDisplay;
		this.metaKey = metaKey;
		this.level = level;
		this.dataType = dataType;
		this.metaDataField = metaDataField;
	}

	public final String getDisplayName(MutableGameObject gameUnit) {
		return displayName;
	}

	public String getSortName(MutableGameObject gameUnit) {
		return sortName;
	}

	public boolean isShowingLevelDisplay() {
		return showingLevelDisplay;
	}

	public int getLevel() {
		return level;
	}

	public final String getRawDataName() {
		return rawDataName;
	}

	public final Object getValue(MutableGameObject gameUnit) {
		return getValue(gameUnit, metaKey, level);
	}

	protected abstract Object getValue(MutableGameObject gameUnit, War3ID metaKey, int level);

	public boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData, boolean disableLimits) {
//		String worldEditValueStringKey = switch (dataType) {
//			case ABILITIES -> "WESTRING_AE_DLG_EDITVALUE";
//			case BUFFS_EFFECTS -> "WESTRING_FE_DLG_EDITVALUE";
//			case DESTRUCTIBLES -> "WESTRING_BE_DLG_EDITVALUE";
//			case DOODADS -> "WESTRING_DE_DLG_EDITVALUE";
//			case ITEM -> "WESTRING_IE_DLG_EDITVALUE";
//			case UPGRADES -> "WESTRING_GE_DLG_EDITVALUE";
//			case UNITS -> "WESTRING_UE_DLG_EDITVALUE";
//		};
//		final String defaultDialogTitle = WEString.getString(worldEditValueStringKey);
		final String defaultDialogTitle = WEString.getString(dataType.getEditString());
		return popupEditor(gameUnit, parent, editRawData, disableLimits, metaKey, level, defaultDialogTitle,
				metaDataField);
	}

	public boolean hasEditedValue(MutableGameObject gameUnit) {
		return gameUnit.hasCustomField(metaKey, level);
	}

	protected abstract boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData,
	                                       boolean disableLimits, War3ID metaKey, int level, String defaultDialogTitle, GameObject metaDataField);


	public static String categoryName(String cat) {
		return switch (cat.toLowerCase()) {
			case "abil" -> WEString.getString("WESTRING_OE_CAT_ABILITIES").replace("&", "");
			case "art" -> WEString.getString("WESTRING_OE_CAT_ART").replace("&", "");
			case "combat" -> WEString.getString("WESTRING_OE_CAT_COMBAT").replace("&", "");
			case "data" -> WEString.getString("WESTRING_OE_CAT_DATA").replace("&", "");
			case "editor" -> WEString.getString("WESTRING_OE_CAT_EDITOR").replace("&", "");
			case "move" -> WEString.getString("WESTRING_OE_CAT_MOVEMENT").replace("&", "");
			case "path" -> WEString.getString("WESTRING_OE_CAT_PATHING").replace("&", "");
			case "sound" -> WEString.getString("WESTRING_OE_CAT_SOUND").replace("&", "");
			case "stats" -> WEString.getString("WESTRING_OE_CAT_STATS").replace("&", "");
			case "tech" -> WEString.getString("WESTRING_OE_CAT_TECHTREE").replace("&", "");
			case "text" -> WEString.getString("WESTRING_OE_CAT_TEXT").replace("&", "");
			default -> WEString.getString("WESTRING_UNKNOWN");
		};
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title,
	                                    JComponent componentWantingFocus) {
		PopupContext popupContext = new PopupContext();
		if (componentWantingFocus instanceof JSpinner) {
			JSpinner spinner = (JSpinner) componentWantingFocus;
			spinner.addChangeListener(e -> commitEdit(parentComponent, spinner));

			JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
			JFormattedTextField textField = editor.getTextField();
			textField.addActionListener(e -> disposeExisting(popupContext));
		}
		JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> selectAll(componentWantingFocus)));
			}
		};
		JDialog dialog = pane.createDialog(parentComponent, title);
		popupContext.dialog = dialog;
		popupContext.optionPane = pane;
		dialog.setVisible(true);

		if (pane.getValue() instanceof Integer) {
			return (Integer) pane.getValue();
		}
		return -1;
	}

	private static void disposeExisting(PopupContext popupContext) {
		if (popupContext.dialog != null) {
			popupContext.optionPane.setValue(JOptionPane.OK_OPTION);
			popupContext.dialog.dispose();
		}
	}

	private static void commitEdit(Component parentComponent, JSpinner spinner) {
		try {
			spinner.commitEdit();
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(parentComponent,
					"Unable to commit edit because: " + e.getClass() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void selectAll(JComponent componentWantingFocus) {
		if (componentWantingFocus instanceof JSpinner) {
			JSpinner spinner = (JSpinner) componentWantingFocus;
			((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().selectAll();
		} else if (componentWantingFocus instanceof JTextComponent) {
			((JTextComponent) componentWantingFocus).selectAll();
		}
	}

	private static final class PopupContext {
		private JOptionPane optionPane;
		private JDialog dialog;
	}
}
