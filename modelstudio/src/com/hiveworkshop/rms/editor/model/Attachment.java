package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxAttachment;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;

/**
 * Write a description of class Attachment here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Attachment extends IdObject {
	String path = null;
	int attachmentID = 0;

	private Attachment() {

	}

	public Attachment(String name) {
		this.name = name;
	}

	public Attachment(Attachment attachment) {
		copyObject(attachment);
	
		path = attachment.path;
		attachmentID = attachment.attachmentID;
	}

	public Attachment(MdlxAttachment attachment) {
		if ((attachment.flags & 2048) != 2048) {
			System.err.println("MDX -> MDL error: A light '" + attachment.name + "' not flagged as light in MDX!");
		}

		loadObject(attachment);

		setAttachmentID(attachment.attachmentId);
		setPath(attachment.path);
	}

	public MdlxAttachment toMdlx(EditableModel model) {
		MdlxAttachment attachment = new MdlxAttachment();

		objectToMdlx(attachment, model);

		attachment.attachmentId = getAttachmentID();

		if (getPath() != null) {
			attachment.path = getPath();
		}

		return attachment;
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

	public int getAttachmentID() {
		return attachmentID;
	}

	public void setAttachmentID(int attachmentID) {
		this.attachmentID = attachmentID;
	}

//	@Override
//	public void apply(IdObjectVisitor visitor) {
//		visitor.attachment(this);
//	}

	@Override
	public double getClickRadius(CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordSysUtils.getZoom(coordinateSystem);
	}
}
