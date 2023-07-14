package com.hiveworkshop.rms.parsers.simsstuff;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.simsstuff.plain.SimModel2;
import com.hiveworkshop.rms.parsers.simsstuff.smarter.Generator;
import com.hiveworkshop.rms.ui.application.FloatingWindowFactory;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class TwiSKNParserPanel extends JPanel {
	JTextArea textArea = new JTextArea();
	TwiSKNParser twiSKNParser = new TwiSKNParser();

	public TwiSKNParserPanel() {
		super(new MigLayout("fill, ins 0"));
		JScrollPane scrollPane = new JScrollPane(this.textArea);
		scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		this.add(scrollPane, "spanx, growx, growy, wrap");
		JButton validate = new JButton("validate");
		validate.addActionListener((e) -> this.twiSKNParser.parseModel(this.textArea.getText()).makeModel());
		this.add(validate);
		JButton loadModel = new JButton("load Model");
		loadModel.addActionListener((e) -> this.loadModel());
		this.add(loadModel);
		JButton toString = new JButton("to string");
		toString.addActionListener((e) -> this.getStringModel());
		this.add(toString);
	}

	private void loadModel() {

		LineReaderThingi lineReaderThingi = new LineReaderThingi(this.textArea.getText());
		SimModel2 simModel2 = new SimModel2(lineReaderThingi);
//		SimsSkeleton simsSkeleton = new SimsSkeleton()
		Generator generator = new Generator(simModel2).initSkeleton(new SimsSkeleton());

		EditableModel model = generator.getModel();
		ModelLoader.loadModel(false, true, new ModelPanel(new ModelHandler(model, null)));
//		EditableModel model = this.twiSKNParser.parseModel(this.textArea.getText()).makeModel();
//		ModelLoader.loadModel(false, true, new ModelPanel(new ModelHandler(model, null)));
	}

	private void getStringModel() {
		JTextArea textArea = new JTextArea();
		textArea.setText(this.twiSKNParser.getSimModel().updateFromEditor().getNewModelString());
		JScrollPane scrollPane = new JScrollPane(textArea);
		JPanel jPanel = new JPanel(new MigLayout("fill, ins 0"));
		scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		jPanel.add(scrollPane, "growx, growy, wrap");
		JButton validate = new JButton("validate");
		validate.addActionListener((e) -> (new TwiSKNParser(textArea.getText().split("\n"))).makeModel());
		jPanel.add(validate);
		int opt = JOptionPane.showConfirmDialog(null, jPanel, "pasteModelStuff", 2);
		if (opt == 0) {
		}

	}

	public static JMenuItem getSimsSKNMenu(){
		JMenuItem menuItem = new JMenuItem("SKN panel");
		menuItem.addActionListener(e -> TwiSKNParserPanel.showPanel());
		return menuItem;
	}

	public static void showPanel() {
		FloatingWindowFactory.openNewWindow(new View("Load SKN", null, new TwiSKNParserPanel()), ProgramGlobals.getRootWindowUgg());
	}
}