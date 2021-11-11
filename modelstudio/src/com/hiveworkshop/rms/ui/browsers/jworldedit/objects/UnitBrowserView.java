package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTS;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UnitBrowserView extends View {
	static ImageIcon imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));


	public UnitBrowserView() {
		super("Unit Browser", imageIcon, new JScrollPane(createUnitEditorTree()));
	}

	static UnitEditorTree createUnitEditorTree() {
		return new UnitEditorTreeBrowser(getUnitData(), new UnitTabTreeBrowserBuilder(), new UnitEditorSettings(), WorldEditorDataType.UNITS);
	}

	/**
	 * Right now this is a plug to the statics to load unit data.
	 * However, it's a non-static method so that we can have it load from an opened map
	 * in the future -- the MutableObjectData class can parse map unit data!
	 */
	public static MutableObjectData getUnitData() {
		War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('u');
		try {
			CompoundDataSource fs = GameDataFileSystem.getDefault();
			if (fs.has("war3map.w3u")) {
				BlizzardDataInputStream stream = new BlizzardDataInputStream(fs.getResourceAsStream("war3map.w3u"));
				WTS wts = fs.has("war3map.wts") ? new WTSFile(fs.getResourceAsStream("war3map.wts")) : null;
				editorData.load(stream, wts, true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new MutableObjectData(WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(), StandardObjectData.getStandardUnitMeta(), editorData);
	}
}
