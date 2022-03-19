package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.FaceEffect;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Supplier;

public class ComponentFaceEffectPanel extends ComponentPanel<FaceEffect> {
	private FaceEffect faceEffect;
	private final JTextField targetField;
	private final JTextField effectField;
	private final JButton exportButton;
	private final JButton exportInGameButton;


	public ComponentFaceEffectPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill", "[][50%:50%:50%, grow][grow]", "[][][][][grow]"));
		targetField = new JTextField();
		targetField.addFocusListener(setEffectTarget());
		effectField = new JTextField();
		effectField.addFocusListener(setEffect());

		add(new JLabel("Target: "));
		add(targetField, "sg fields, growx, wrap");
		add(new JLabel("Effect"));
		add(effectField, "sg fields, growx, wrap 25px");

		exportButton = new JButton("Export");
		exportButton.addActionListener(e -> export(() -> faceEffect.getFaceEffect()));
		add(exportButton, "wrap");
		exportInGameButton = new JButton("Export _inGame");
		exportInGameButton.addActionListener(e -> export(() -> faceEffect.getFaceEffect() + "_ingame"));
		add(exportInGameButton, "wrap");

	}

	@Override
	public ComponentPanel<FaceEffect> setSelectedItem(FaceEffect itemToSelect) {
		faceEffect = itemToSelect;
		targetField.setText(faceEffect.getFaceEffectTarget());
		effectField.setText(faceEffect.getFaceEffect());

		CompoundDataSource dataSource = GameDataFileSystem.getDefault();
		exportButton.setEnabled(dataSource.has(faceEffect.getFaceEffect()));
		exportInGameButton.setEnabled(dataSource.has(faceEffect.getFaceEffect() + "_ingame"));

		revalidate();
		repaint();
		return this;
	}

	private FocusAdapter setEffectTarget() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				faceEffect.setFaceEffectTarget(targetField.getText());
			}
		};
	}

	private FocusAdapter setEffect() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				faceEffect.setFaceEffect(effectField.getText());
			}
		};
	}

	private void export(Supplier<String> pathSupplier){
		String faceEffect = pathSupplier.get();
		if(!faceEffect.isEmpty()){
			FileDialog fileDialog = new FileDialog(this);
			fileDialog.exportInternalFile(faceEffect);
		}
	}
}
