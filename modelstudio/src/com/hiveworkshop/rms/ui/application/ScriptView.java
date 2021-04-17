package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.View;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScriptView {
	static View createHackerView(final MainPanel mainPanel) {
		final View hackerView;
		final JPanel hackerPanel = new JPanel(new BorderLayout());
		final RSyntaxTextArea matrixEaterScriptTextArea = new RSyntaxTextArea(20, 60);
		matrixEaterScriptTextArea.setCodeFoldingEnabled(true);
		matrixEaterScriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		hackerPanel.add(new RTextScrollPane(matrixEaterScriptTextArea), BorderLayout.CENTER);

		ImageIcon icon = new ImageIcon(BLPHandler.get()
				.getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp")
				.getScaledInstance(24, 24, Image.SCALE_FAST));
		final JButton run = new JButton("Run", icon);
		run.addActionListener(showScriptViewAction(mainPanel, matrixEaterScriptTextArea));
		hackerPanel.add(run, BorderLayout.NORTH);
		hackerView = new View("Matrix Eater Script", null, hackerPanel);
		return hackerView;
	}

	private static ActionListener showScriptViewAction(MainPanel mainPanel, RSyntaxTextArea matrixEaterScriptTextArea) {
		return new ActionListener() {
			final ScriptEngineManager factory = new ScriptEngineManager();

			@Override
			public void actionPerformed(final ActionEvent e) {
				final String text = matrixEaterScriptTextArea.getText();
				final ScriptEngine engine = factory.getEngineByName("JavaScript");
				final ModelPanel modelPanel = mainPanel.currentModelPanel();
				if (modelPanel != null) {
					engine.put("modelPanel", modelPanel);
					engine.put("model", modelPanel.getModel());
					engine.put("world", mainPanel);
					try {
						engine.eval(text);
					} catch (final ScriptException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(mainPanel, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(mainPanel, "Must open a file!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}
}
