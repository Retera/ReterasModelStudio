package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;

import javax.swing.*;

class GeosetAnimationPanel extends JTabbedPane {
	// Geoset Animation panel for controlling bone attachments and visibility
	EditableModel model;
	Geoset geoset;
	boolean isImported;
	int index;
	BoneAttachmentPanel bap;
	VisibilityPanel vp;
	ModelHolderThing mht;

	public GeosetAnimationPanel(ModelHolderThing mht, final boolean imported, // Is this Geoset an imported one, or an original?
	                            final EditableModel model, final int geoIndex) {
		this.mht = mht;
		this.model = model;
		index = geoIndex;
//		geoset = model.getGeoset(geoIndex);
		isImported = imported;

//		bap = new BoneAttachmentPanel(mht, model, geoset, null);
		addTab("Bones", ImportPanel.boneIcon, bap, "Allows you to edit bone references.");

		// vp = new
		// VisibilityPane(thePanel.currentModel.m_geosets.size(),thePanel.currentModel.getName(),thePanel.importedModel.m_geosets.size(),thePanel.importedModel.getName(),geoIndex);
		// addTab("Visibility",ImportPanel.animIcon,vp,"Allows you to edit
		// visibility.");
	}

	public void refreshLists() {
		bap.refreshLists();
	}
}
