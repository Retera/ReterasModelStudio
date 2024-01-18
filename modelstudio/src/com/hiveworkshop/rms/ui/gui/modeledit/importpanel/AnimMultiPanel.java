package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class AnimMultiPanel extends AnimPanel {
	protected JLabel title;
	private String reciveText = "<html><p>Use the animation data of the following animation<br>in these animations, where applicable:";
	private String timeScaleDonInfo = "<html><p>Use the animation data of this animation in the<br>following animations, where applicable:";

	public AnimMultiPanel(ModelHolderThing mht, TwiList<AnimShell> animJList) {
		super(mht, animJList);
		setLayout(new MigLayout("gap 0, fill", "[][]", "[][][][][grow]"));

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, spanx, wrap");

		animInfo = new JLabel("XX Selected");
		add(animInfo, "align center, spanx, wrap");

		add(new JLabel(""), "align center, spanx, wrap");

		animSrcRenderer = new AnimListCellRenderer(false, this::getSrcStatus, this::isSrcSelected);

		add(getImportCheckBox(this::setImport), "align center, split");
		add(getReverseCheckBox(this::setInReverse), "wrap");

		sourceList = new SearchListPanel<>(reciveText, this::search)
				.setRenderer(animSrcRenderer)
				.setSelectionConsumer(this::onSourceSelected)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(sourceList, "spanx, growx, growy");
	}

	protected String getInfoText() {
		return selectedValuesList.size() + " Selected";
	}

}
