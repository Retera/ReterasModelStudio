package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTS;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.War3ID;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class DoodadBrowserView extends View {
	private static ImageIcon icon = new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST));

	public DoodadBrowserView() {
		super("Doodad Browser", icon, new JScrollPane(getUnitEditorTree()));
	}

	private static UnitEditorTree getUnitEditorTree() {
		UnitEditorTree unitEditorTree = new UnitEditorTree(getDoodadData(), new DoodadTabTreeBrowserBuilder(), new UnitEditorSettings());
		unitEditorTree.selectFirstUnit();

		unitEditorTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				try {
					doodadViewerMouseClick(e, unitEditorTree);
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});
		return unitEditorTree;
	}

	public static MutableObjectData getDoodadData() {
		War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('d');
		try {
			CompoundDataSource fs = GameDataFileSystem.getDefault();
			if (fs.has("war3map.w3d")) {
				BlizzardDataInputStream stream = new BlizzardDataInputStream(fs.getResourceAsStream("war3map.w3d"));
				WTS wts = fs.has("war3map.wts") ? new WTSFile(fs.getResourceAsStream("war3map.wts")) : null;
				editorData.load(stream, wts, true);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new MutableObjectData(WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(), StandardObjectData.getStandardDoodadMeta(), editorData);
	}

	private static void doodadViewerMouseClick(MouseEvent e, UnitEditorTree unitEditorTree) {
		if (e.getClickCount() >= 2) {
			TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
			if (currentUnitTreePath != null) {

				DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
				if (o.getUserObject() instanceof MutableGameObject) {

					MutableGameObject obj = (MutableGameObject) o.getUserObject();
					int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
					if (numberOfVariations > 1) {
						for (int i = 0; i < numberOfVariations; i++) {
							String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl";
							InternalFileLoader.loadMdxStream(obj, prePath, i == 0);
						}
					} else {
						String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0);
						InternalFileLoader.loadMdxStream(obj, prePath, true);
					}
					MenuBar.setToolsMenuEnabled(true);
				}
			}
		}
	}
}
