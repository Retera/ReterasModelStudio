package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;

import javax.swing.*;
import java.awt.*;

class GeosetAnimationPanel extends JTabbedPane {
	// Geoset Animation panel for controlling bone attachments and visibility
	EditableModel model;
	Geoset geoset;
	boolean isImported;
	int index;
	BoneAttachmentPanel bap;
	VisibilityPanel vp;
	ImportPanel impPanel;

	public GeosetAnimationPanel(final boolean imported, // Is this Geoset an imported one, or an original?
	                            final EditableModel model, final int geoIndex, final ImportPanel thePanel)
	// which geoset is this for? (starts with 0)
	{
		this.model = model;
		impPanel = thePanel;
		index = geoIndex;
		geoset = model.getGeoset(geoIndex);
		isImported = imported;

		bap = new BoneAttachmentPanel(model, geoset, null, getImportPanel());
		addTab("Bones", ImportPanel.boneIcon, bap, "Allows you to edit bone references.");

		// vp = new
		// VisibilityPane(thePanel.currentModel.m_geosets.size(),thePanel.currentModel.getName(),thePanel.importedModel.m_geosets.size(),thePanel.importedModel.getName(),geoIndex);
		// addTab("Visibility",ImportPanel.animIcon,vp,"Allows you to edit
		// visibility.");
	}

	public void refreshLists() {
		bap.refreshLists();
	}

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while ((temp != null) && (temp.getClass() != ImportPanel.class)) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}
}
