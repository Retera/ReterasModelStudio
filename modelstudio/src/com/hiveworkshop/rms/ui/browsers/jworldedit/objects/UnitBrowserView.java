package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.icons.RMSIcons;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class UnitBrowserView extends View {
	static ImageIcon imageIcon = new ImageIcon(RMSIcons.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));


	public UnitBrowserView() {
		super("Unit Browser", imageIcon, new JScrollPane(createUnitEditorTree()));
	}

	static JPanel getContentPanel(){
		JPanel panel = new JPanel(new MigLayout("fill, ins 0, gap 0, novisualpadding, wrap 1", "[fill, grow]", "[][fill, grow]"));
		UnitEditorTree unitEditorTree = createUnitEditorTree();
		panel.add(unitEditorTree.getSearchBar());
		panel.add(new JScrollPane(unitEditorTree), "growx, growy");
		return panel;
	}
	static UnitEditorTree createUnitEditorTree() {
		return new UnitEditorTreeBrowser(new UnitTabTreeBrowserBuilder(), new UnitEditorSettings());
	}

}
