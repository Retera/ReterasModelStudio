package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;

/**
 * Write a description of class Attachment here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Attachment extends IdObject {
	String path = null;
	int attachmentID = -1;

	public Attachment() {

	}

	public Attachment(String name) {
		this.name = name;
	}

	private Attachment(Attachment attachment) {
		super(attachment);

		path = attachment.path;
		attachmentID = attachment.attachmentID;
	}

	@Override
	public Attachment copy() {
		return new Attachment(this);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		if (!"".equals(path)) {
			this.path = path;
		}
	}

	public int getAttachmentID(EditableModel model) {
		return model.getAttachments().indexOf(this);
//		if(attachmentID == -1){
//			return model.getAttachments().indexOf(this);
//		}
//		return attachmentID;
	}
	public int getRealAttachmentID() {
		return attachmentID;
	}


	public void setAttachmentID(int attachmentID) {
		this.attachmentID = attachmentID;
	}

	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
	}
}
