package com.hiveworkshop.wc3.mdl;

import java.util.HashSet;

import com.etheller.warsmash.parsers.mdlx.MdlxAttachment;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

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

	public Attachment(final String name) {
		this.name = name;
	}

	public Attachment(final Attachment attachment) {
		copyObject(attachment);
	
		path = attachment.path;
		attachmentID = attachment.attachmentID;
	}

	public Attachment(final MdlxAttachment attachment) {
		if ((attachment.flags & 2048) != 2048) {
			System.err.println("MDX -> MDL error: A light '" + attachment.name + "' not flagged as light in MDX!");
		}

		loadObject(attachment);

		setAttachmentID(attachment.attachmentId);
		setPath(attachment.path);
	}

	public MdlxAttachment toMdlx() {
		MdlxAttachment attachment = new MdlxAttachment();

		objectToMdlx(attachment);

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

	public void setPath(final String path) {
		if (!"".equals(path)) {
			this.path = path;
		}
	}

	public int getAttachmentID() {
		return attachmentID;
	}

	public void setAttachmentID(final int attachmentID) {
		this.attachmentID = attachmentID;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.attachment(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}
}
