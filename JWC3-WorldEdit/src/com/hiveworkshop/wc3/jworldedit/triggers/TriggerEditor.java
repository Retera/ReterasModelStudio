package com.hiveworkshop.wc3.jworldedit.triggers;

import static com.hiveworkshop.wc3.jworldedit.AbstractWorldEditorPanel.getIcon;
import static com.hiveworkshop.wc3.jworldedit.AbstractWorldEditorPanel.makeButton;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.wc3.jworldedit.objects.ObjectEditorFrame;
import com.hiveworkshop.wc3.jworldedit.triggers.gui.TriggerTree;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerEnvironment;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.util.IconUtils;

import net.sf.image4j.codec.ico.ICODecoder;

public class TriggerEditor extends JPanel {
	private JButton createNewCategoryButton;
	private JButton createNewTriggerButton;
	private JButton createNewCommentButton;
	private JButton pasteButton;
	private JButton copyButton;
	private final TriggerTree triggerTree;

	public TriggerEditor() {
		final TriggerEnvironment triggerEnvironment = new TriggerEnvironment("Untitled");
		triggerTree = new TriggerTree(triggerEnvironment);

		final JSplitPane mainTriggerEditorContentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(triggerTree),
				new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JPanel(), new JScrollPane(new JTree())));

		final JToolBar toolBar = createToolbar(DataTable.getWorldEditorData());
		toolBar.setFloatable(false);

		setLayout(new BorderLayout());

		add(toolBar, BorderLayout.BEFORE_FIRST_LINE);
		add(mainTriggerEditorContentPane, BorderLayout.CENTER);
	}

	private JToolBar createToolbar(final DataTable worldEditorData) {
		final JToolBar toolBar = new JToolBar();
		makeButton(worldEditorData, toolBar, "newMap", "ToolBarIcon_New", "WESTRING_TOOLBAR_NEW");
		makeButton(worldEditorData, toolBar, "openMap", "ToolBarIcon_Open", "WESTRING_TOOLBAR_OPEN");
		final JButton saveButton = makeButton(worldEditorData, toolBar, "saveMap", "ToolBarIcon_Save",
				"WESTRING_TOOLBAR_SAVE");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser jFileChooser = new JFileChooser(
						new File(System.getProperty("user.home") + "/Documents/Warcraft III/Maps"));
				jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jFileChooser.setDialogTitle("Save Map");
				if (jFileChooser.showSaveDialog(TriggerEditor.this) == JFileChooser.APPROVE_OPTION) {

				}

			}
		});
		toolBar.add(Box.createHorizontalStrut(8));
		final TransferActionListener transferActionListener = new TransferActionListener();
		copyButton = makeButton(worldEditorData, toolBar, "copy", "ToolBarIcon_Copy", "WESTRING_MENU_OE_UNIT_COPY");
		copyButton.addActionListener(transferActionListener);
		copyButton.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
		pasteButton = makeButton(worldEditorData, toolBar, "paste", "ToolBarIcon_Paste", "WESTRING_MENU_OE_UNIT_PASTE");
		pasteButton.addActionListener(transferActionListener);
		pasteButton.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
		toolBar.add(Box.createHorizontalStrut(8));
		createNewCategoryButton = makeButton(worldEditorData, toolBar, "createNewCategory",
				"ToolBarIcon_SE_NewCategory", "WESTRING_TOOLBAR_SE_NEWCAT");
		createNewCategoryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final TriggerCategory category = triggerTree.getController().createCategory();
				triggerTree.select(category);
				triggerTree.startEditingAtPath(triggerTree.getSelectionPath());
			}
		});
		createNewTriggerButton = makeButton(worldEditorData, toolBar, "createNewTrigger", "ToolBarIcon_SE_NewTrigger",
				"WESTRING_TOOLBAR_SE_NEWTRIG");
		createNewTriggerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Trigger trigger = triggerTree.createTrigger();
				triggerTree.select(trigger);
				TreePath selectionPath = triggerTree.getSelectionPath();
				triggerTree.startEditingAtPath(selectionPath);
			}
		});
		createNewCommentButton = makeButton(worldEditorData, toolBar, "createNewTriggerComment",
				"ToolBarIcon_SE_NewTriggerComment", "WESTRING_TOOLBAR_SE_NEWTRIGCOM");
		createNewCommentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Trigger trigger = triggerTree.createTriggerComment();
				triggerTree.select(trigger);
				triggerTree.startEditingAtPath(triggerTree.getSelectionPath());
			}
		});
		toolBar.add(Box.createHorizontalStrut(8));
		makeButton(worldEditorData, toolBar, "terrainEditor", "ToolBarIcon_Module_Terrain",
				"WESTRING_MENU_MODULE_TERRAIN");
		final JToggleButton scriptEditorButton = new JToggleButton(
				getIcon(worldEditorData, "ToolBarIcon_Module_Script"));
		scriptEditorButton.setToolTipText(WEString.getString("WESTRING_MENU_MODULE_SCRIPTS").replace("&", ""));
		scriptEditorButton.setPreferredSize(new Dimension(24, 24));
		scriptEditorButton.setMargin(new Insets(1, 1, 1, 1));
		scriptEditorButton.setSelected(true);
		scriptEditorButton.setEnabled(false);
		scriptEditorButton.setDisabledIcon(scriptEditorButton.getIcon());
		toolBar.add(scriptEditorButton);
		makeButton(worldEditorData, toolBar, "soundEditor", "ToolBarIcon_Module_Sound", "WESTRING_MENU_MODULE_SOUND");
		makeButton(worldEditorData, toolBar, "objectEditor", "ToolBarIcon_Module_ObjectEditor",
				"WESTRING_MENU_OBJECTEDITOR");
		makeButton(worldEditorData, toolBar, "campaignEditor", "ToolBarIcon_Module_Campaign",
				"WESTRING_MENU_MODULE_CAMPAIGN");
		makeButton(worldEditorData, toolBar, "aiEditor", "ToolBarIcon_Module_AIEditor", "WESTRING_MENU_MODULE_AI");
		makeButton(worldEditorData, toolBar, "objectEditor", "ToolBarIcon_Module_ObjectManager",
				"WESTRING_MENU_OBJECTMANAGER");
		makeButton(worldEditorData, toolBar, "importEditor", "ToolBarIcon_Module_ImportManager",
				"WESTRING_MENU_IMPORTMANAGER");
		toolBar.add(Box.createHorizontalStrut(8));
		makeButton(worldEditorData, toolBar, "testMap",
				new ImageIcon(IconUtils.worldEditStyleIcon(getIcon(worldEditorData, "ToolBarIcon_TestMap").getImage())),
				"WESTRING_TOOLBAR_TESTMAP");
		return toolBar;
	}

	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		} catch (final Exception exc) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			} catch (final InstantiationException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			} catch (final UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		}

		final JFrame frame = new JFrame("Trigger Editor");

		final TriggerEditor contentPane = new TriggerEditor();
		final JToolBar toolbar = contentPane.createToolbar(DataTable.getWorldEditorData());
		try {
			final List<BufferedImage> images = ICODecoder
					.read(ObjectEditorFrame.class.getResourceAsStream("worldedit.ico"));
			final List<BufferedImage> finalImages = new ArrayList<>();
			BufferedImage lastImage = null;
			for (final BufferedImage image : images) {
				if (lastImage != null && image.getWidth() != lastImage.getWidth()) {
					finalImages.add(lastImage);
				}
				lastImage = image;
			}
			finalImages.add(lastImage);
			frame.setIconImages(finalImages);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(contentPane);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
