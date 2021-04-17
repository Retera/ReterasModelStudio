package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

public final class EditorTabCustomToolbarButtonData {
	private final String newCustomObject;
	private final String iconKey;
	private final String copyObject;
	private final String pasteObject;

	public EditorTabCustomToolbarButtonData(final String newCustomObject, final String iconKey, final String copyObject,
			final String pasteObject) {
		this.newCustomObject = newCustomObject;
		this.iconKey = iconKey;
		this.copyObject = copyObject;
		this.pasteObject = pasteObject;
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