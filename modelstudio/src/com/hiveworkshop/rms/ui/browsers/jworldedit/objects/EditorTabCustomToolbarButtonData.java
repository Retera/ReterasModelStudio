package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

public final class EditorTabCustomToolbarButtonData {
	private static final String menuTag = "WESTRING_MENU_OE_";
	private static final String iconTag = "ToolBarIcon_OE_New";
	private final String newCustomObject;
	private final String iconKey;
	private final String copyObject;
	private final String pasteObject;

	public EditorTabCustomToolbarButtonData(String newCustomObject, String iconKey, String copyObject, String pasteObject) {
		this.newCustomObject = newCustomObject;
		this.iconKey = iconKey;
		this.copyObject = copyObject;
		this.pasteObject = pasteObject;
	}

	public EditorTabCustomToolbarButtonData(String upgr, String iconKey) {
		this.newCustomObject = menuTag + upgr + "_NEW";
		this.iconKey = iconTag + iconKey;
		this.copyObject = menuTag + upgr + "_COPY";
		this.pasteObject = menuTag + upgr + "_PASTE";
	}

	public String getNewCustomObject() {
		return newCustomObject;
	}

	public String getIconKey() {
		return iconKey;
	}

	public String getCopyObject() {
		return copyObject;
	}

	public String getPasteObject() {
		return pasteObject;
	}
}