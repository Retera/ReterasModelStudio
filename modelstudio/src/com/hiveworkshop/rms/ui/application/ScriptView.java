package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import net.infonode.docking.View;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;

public class ScriptView {
	public static void openScriptView(MainPanel mainPanel) {
		View hackerView = new ScriptView().createHackerView(mainPanel);
		FloatingWindowFactory.openNewWindow(hackerView, mainPanel.getRootWindow());
//		FloatingWindowFactory.openNewWindow("Matrix Eater Script", new ScriptView().createHackerPanel(mainPanel), mainPanel.getRootWindow());
//		OpenViewAction openViewAction = OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Matrix Eater Script", hackerView);

	}

	public View createHackerView(final MainPanel mainPanel) {
		View hackerView;
		JPanel hackerPanel = new JPanel(new BorderLayout());

		RSyntaxTextArea scriptTextArea = new RSyntaxTextArea(20, 60);
		scriptTextArea.setCodeFoldingEnabled(true);
		scriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		hackerPanel.add(new RTextScrollPane(scriptTextArea), BorderLayout.CENTER);

		ImageIcon icon = new ImageIcon(BLPHandler.get()
				.getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp")
				.getScaledInstance(24, 24, Image.SCALE_FAST));

		JButton runScriptButton = new JButton("Run", icon);
		ScriptEngineManager factory = new ScriptEngineManager();
		runScriptButton.addActionListener(e -> runScript(factory, scriptTextArea, mainPanel));
		hackerPanel.add(runScriptButton, BorderLayout.NORTH);

		hackerView = new View("Matrix Eater Script", null, hackerPanel);
		return hackerView;
	}


	public JPanel createHackerPanel(final MainPanel mainPanel) {
		JPanel hackerPanel = new JPanel(new BorderLayout());

		RSyntaxTextArea scriptTextArea = new RSyntaxTextArea(20, 60);
		scriptTextArea.setCodeFoldingEnabled(true);
		scriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		hackerPanel.add(new RTextScrollPane(scriptTextArea), BorderLayout.CENTER);

		ImageIcon icon = new ImageIcon(BLPHandler.get()
				.getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp")
				.getScaledInstance(24, 24, Image.SCALE_FAST));

		JButton runScriptButton = new JButton("Run", icon);
		ScriptEngineManager factory = new ScriptEngineManager();
		runScriptButton.addActionListener(e -> runScript(factory, scriptTextArea, mainPanel));

		hackerPanel.add(runScriptButton, BorderLayout.NORTH);
		return hackerPanel;
	}

	private void runScript(ScriptEngineManager factory, RSyntaxTextArea matrixEaterScriptTextArea, MainPanel mainPanel) {
		String text = matrixEaterScriptTextArea.getText();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		ModelPanel modelPanel = mainPanel.currentModelPanel();

		if (modelPanel != null) {
			engine.put("modelPanel", modelPanel);
			engine.put("model", modelPanel.getModel());
			engine.put("world", mainPanel);
			try {
				engine.eval(text);
			} catch (ScriptException e) {
				e.printStackTrace();
				ExceptionPopup.display(e, "Script Error");
//				JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(mainPanel, "Must open a file!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
