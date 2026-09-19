package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import com.hiveworkshop.wc3.mdl.Attachment;

public class ComponentAttachmentCard extends ComponentNodeCard<Attachment> {
	@Override
	protected void addTypeRows() {
		beginSection("Attachment");
		addTextField("Path", Attachment::getPath, (attachment, path) -> attachment.setPath(path == null ? "" : path));
		addIntSpinner("Attachment ID", -1, Integer.MAX_VALUE, Attachment::getAttachmentID,
				Attachment::setAttachmentID);
		endSection();
	}
}
