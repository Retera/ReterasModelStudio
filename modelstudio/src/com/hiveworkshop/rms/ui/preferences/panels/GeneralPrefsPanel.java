package com.hiveworkshop.rms.ui.preferences.panels;

import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ExtFilter;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.Objects;

public class GeneralPrefsPanel extends JPanel {

	public GeneralPrefsPanel(ProgramPreferences pref){
		super(new MigLayout());

		add(new JLabel("Show 2D Viewport Gridlines:"));
		add(CheckBox.create("", pref.show2dGrid(), pref::setShow2dGrid), "wrap");

		add(new JLabel("Use Boxes for Nodes:"));
		add(CheckBox.create("", pref.isUseBoxesForPivotPoints(), pref::setUseBoxesForPivotPoints), "wrap");

		add(new JLabel("Bone Box Size:"));
		add(new IntEditorJSpinner(pref.getNodeBoxSize(), 1, pref::setNodeBoxSize), "wrap");

		add(new JLabel("Vertex Square Size:"));
		add(new IntEditorJSpinner(pref.getVertexSize(), 1, pref::setVertexSize), "wrap");


		add(new JLabel("Quick Browse:"));
		JCheckBox quickBrowse = CheckBox.create("", pref.getQuickBrowse(), pref::setQuickBrowse);
		add(CheckBox.setTooltip(quickBrowse, "When opening a new model, close old ones if they have not been modified."), "wrap");

		add(new JLabel("Allow Loading Non BLP Textures:"));
		JCheckBox allowLoadingNonBlpTextures = CheckBox.create("", pref.getAllowLoadingNonBlpTextures(), pref::setAllowLoadingNonBlpTextures);
		add(CheckBox.setTooltip(allowLoadingNonBlpTextures, "Needed for opening PNGs with standard File Open"), "wrap");


		add(new JLabel("Open Browsers On Startup:"));
		add(CheckBox.create("", pref.loadBrowsersOnStartup(), pref::setLoadBrowsersOnStartup), "wrap");

		add(new JLabel("Default Open File Filter:"));
		add(getExtensionFilterBox(pref), "wrap");
	}

	private TwiComboBox<FileNameExtensionFilter> getExtensionFilterBox(ProgramPreferences pref) {
		List<FileNameExtensionFilter> openFilesExtensions = new ExtFilter().getOpenFilesExtensions();
		final TwiComboBox<FileNameExtensionFilter> fileFilterBox = new TwiComboBox<>(openFilesExtensions, openFilesExtensions.get(1));
		fileFilterBox.setStringFunctionRender(o -> (o instanceof FileNameExtensionFilter fnef) ? fnef.getDescription() : "");

		FileNameExtensionFilter filter = openFilesExtensions.stream()
				.filter(f -> Objects.equals(f.getDescription(), pref.getOpenFileFilter()))
				.findFirst()
				.orElse(null);
		fileFilterBox.selectOrFirst(filter);

		fileFilterBox.addOnSelectItemListener(o -> pref.setOpenFileFilter(o.getDescription()));
		return fileFilterBox;
	}
}
