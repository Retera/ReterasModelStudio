package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.DoNothingActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoManager;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoManagerImpl;
import com.hiveworkshop.wc3.gui.modeledit.activity.ViewportActivityManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

/**
 * The ModelPanel is a pane holding the display of a given MDL model. I plan to
 * tab between them.
 *
 * Eric Theller 6/7/2012
 */
public class ModelPanel extends JPanel implements ActionListener, MouseListener {
	private static final int VERTEX_SIZE = 3;
	private JMenuBar menuBar;
	private JMenu fileMenu, modelMenu;
	private DisplayPanel frontArea, sideArea, botArea;
	private PerspDisplayPanel perspArea;
	private MDL model;
	private File file;
	private final ProgramPreferences prefs;
	private final UndoHandler undoHandler;
	private final ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeNotifier;
	private final ViewportActivityManager viewportActivityManager;
	private final ModelEditorChangeNotifier modelEditorChangeNotifier;
	private final ModelEditorManager modelEditorManager;
	private final ModelViewManager modelView;
	private final UndoManager undoManager;
	private UVPanel editUVPanel;

	private final ModelViewManagingTree modelViewManagingTree;

	public ModelPanel(final File input, final ProgramPreferences prefs, final UndoHandler undoHandler,
			final ToolbarButtonGroup<SelectionItemTypes> notifier, final ToolbarButtonGroup<SelectionMode> modeNotifier,
			final ModelStructureChangeListener modelStructureChangeListener,
			final CoordDisplayListener coordDisplayListener) {
		this(MDL.read(input), prefs, undoHandler, notifier, modeNotifier, modelStructureChangeListener,
				coordDisplayListener);
		file = input;
	}

	public ModelPanel(final MDL input, final ProgramPreferences prefs, final UndoHandler undoHandler,
			final ToolbarButtonGroup<SelectionItemTypes> notifier, final ToolbarButtonGroup<SelectionMode> modeNotifier,
			final ModelStructureChangeListener modelStructureChangeListener,
			final CoordDisplayListener coordDisplayListener) {
		super();
		this.prefs = prefs;
		this.undoHandler = undoHandler;
		this.selectionItemTypeNotifier = notifier;
		viewportActivityManager = new ViewportActivityManager(new DoNothingActivity());
		modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);
		modelView = new ModelViewManager(input);
		undoManager = new UndoManagerImpl();
		modelEditorManager = new ModelEditorManager(modelView, prefs, modeNotifier, modelEditorChangeNotifier,
				viewportActivityManager);
		modelViewManagingTree = new ModelViewManagingTree(modelView, undoManager, modelEditorManager);
		modelViewManagingTree.setFocusable(false);
		selectionItemTypeNotifier.addToolbarButtonListener(new ToolbarButtonListener<SelectionItemTypes>() {
			@Override
			public void typeChanged(final SelectionItemTypes newType) {
				modelEditorManager.setSelectionItemType(newType);
			}
		});
		// Produce the front display panel
		// file = input;
		// model = MDL.read(file);
		// dispModel = new MDLDisplay(model,this);
		loadModel(input);

		frontArea = new DisplayPanel("Front", (byte) 1, (byte) 2, modelView, modelEditorManager.getModelEditor(),
				modelStructureChangeListener, viewportActivityManager, prefs, undoManager, coordDisplayListener,
				undoHandler, modelEditorChangeNotifier);
		// frontArea.setViewport(1,2);
		add(frontArea);
		botArea = new DisplayPanel("Bottom", (byte) 1, (byte) 0, modelView, modelEditorManager.getModelEditor(),
				modelStructureChangeListener, viewportActivityManager, prefs, undoManager, coordDisplayListener,
				undoHandler, modelEditorChangeNotifier);
		// botArea.setViewport(0,1);
		add(botArea);
		sideArea = new DisplayPanel("Side", (byte) 0, (byte) 2, modelView, modelEditorManager.getModelEditor(),
				modelStructureChangeListener, viewportActivityManager, prefs, undoManager, coordDisplayListener,
				undoHandler, modelEditorChangeNotifier);
		// sideArea.setViewport(0,2);
		add(sideArea);

		frontArea.setControlsVisible(prefs.showVMControls());
		botArea.setControlsVisible(prefs.showVMControls());
		sideArea.setControlsVisible(prefs.showVMControls());

		perspArea = new PerspDisplayPanel("Perspective", modelView, prefs);
		// perspAreaPanel.setMinimumSize(new Dimension(200,200));
		// perspAreaPanel.add(Box.createHorizontalStrut(200));
		// perspAreaPanel.add(Box.createVerticalStrut(200));
		// perspAreaPanel.setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
		// botArea.setViewport(0,1);
		add(perspArea);

		setLayout(new GridLayout(2, 2));

		// Hacky viewer
		// frontArea.setVisible(false);
		// sideArea.setVisible(false);
		// botArea.setVisible(false);
		// setLayout(new GridLayout(1,1));
		// GroupLayout layout = new GroupLayout(this);
		//
		// layout.setHorizontalGroup(layout.createSequentialGroup()
		// .addGroup(layout.createParallelGroup()
		// .addComponent(frontArea)
		// .addComponent(sideArea))
		// .addGroup(layout.createParallelGroup()
		// .addComponent(botArea)
		// .addComponent(perspArea)));
		// layout.setVerticalGroup(layout.createSequentialGroup()
		// .addGroup(layout.createParallelGroup()
		// .addComponent(frontArea)
		// .addComponent(botArea))
		// .addGroup(layout.createParallelGroup()
		// .addComponent(sideArea)
		// .addComponent(perspArea)));
		// setLayout(layout);

		// Create a file chooser
	}

	public void changeActivity(final ActivityDescriptor activityDescriptor) {
		viewportActivityManager
				.setCurrentActivity(activityDescriptor.createActivity(modelEditorManager, modelView, undoManager));
	}

	public ModelEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public UVPanel getEditUVPanel() {
		return editUVPanel;
	}

	public void setEditUVPanel(final UVPanel editUVPanel) {
		this.editUVPanel = editUVPanel;
	}

	public void loadModel(final File input) {
		file = input;
		if (file != null) {
			model = MDL.read(file);
			loadModel(model);
		}
	}

	public void loadModel(final MDL model) {
		this.model = model;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		perspArea.getViewport().setMinimumSize(new Dimension(200, 200));
		// botArea.setMaximumSize(frontArea.getSize());
		// sideArea.setMaximumSize(frontArea.getSize());
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		// //Open, off of the file menu:
		// if( e.getSource() == open )
		// {
		// int returnValue = fc.showOpenDialog(this);
		//
		// if( returnValue == JFileChooser.APPROVE_OPTION )
		// {
		// currentFile = fc.getSelectedFile();
		// frontArea.clearGeosets();
		// sideArea.clearGeosets();
		// botArea.clearGeosets();
		// modelMenu.getAccessibleContext().setAccessibleDescription("Allows the
		// user to control which parts of the model are displayed for
		// editing.");
		// modelMenu.setEnabled(true);
		// loadFile(currentFile);
		// }
		//
		// fc.setSelectedFile(null);
		//
		// // //Special thanks to the JWSFileChooserDemo from oracle's Java
		// tutorials, from which many ideas were borrowed for the following
		// // FileOpenService fos = null;
		// // FileContents fileContents = null;
		// //
		// // try
		// // {
		// // fos =
		// (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
		// // }
		// // catch (UnavailableServiceException exc )
		// // {
		// //
		// // }
		// //
		// // if( fos != null )
		// // {
		// // try
		// // {
		// // fileContents = fos.openFileDialog(null, null);
		// // }
		// // catch (Exception exc )
		// // {
		// // JOptionPane.showMessageDialog(this,"Opening command failed:
		// "+exc.getLocalizedMessage());
		// // }
		// // }
		// //
		// // if( fileContents != null)
		// // {
		// // try
		// // {
		// // fileContents.getName();
		// // }
		// // catch (IOException exc)
		// // {
		// // JOptionPane.showMessageDialog(this,"Problem opening file:
		// "+exc.getLocalizedMessage());
		// // }
		// // }
		// }
		// if( e.getSource() == importButton )
		// {
		// int returnValue = fc.showOpenDialog(this);
		//
		// if( returnValue == JFileChooser.APPROVE_OPTION )
		// {
		// currentFile = fc.getSelectedFile();
		// modelMenu.getAccessibleContext().setAccessibleDescription("Allows the
		// user to control which parts of the model are displayed for
		// editing.");
		// modelMenu.setEnabled(true);
		// loadFile(currentFile);
		// }
		//
		// fc.setSelectedFile(null);
		//
		// // //Special thanks to the JWSFileChooserDemo from oracle's Java
		// tutorials, from which many ideas were borrowed for the following
		// // FileOpenService fos = null;
		// // FileContents fileContents = null;
		// //
		// // try
		// // {
		// // fos =
		// (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
		// // }
		// // catch (UnavailableServiceException exc )
		// // {
		// //
		// // }
		// //
		// // if( fos != null )
		// // {
		// // try
		// // {
		// // fileContents = fos.openFileDialog(null, null);
		// // }
		// // catch (Exception exc )
		// // {
		// // JOptionPane.showMessageDialog(this,"Opening command failed:
		// "+exc.getLocalizedMessage());
		// // }
		// // }
		// //
		// // if( fileContents != null)
		// // {
		// // try
		// // {
		// // fileContents.getName();
		// // }
		// // catch (IOException exc)
		// // {
		// // JOptionPane.showMessageDialog(this,"Problem opening file:
		// "+exc.getLocalizedMessage());
		// // }
		// // }
		// }
		// for( int i = 0; i < geoItems.size(); i++ )
		// {
		// JCheckBoxMenuItem geoItem = (JCheckBoxMenuItem)geoItems.get(i);
		// if( e.getSource() == geoItem )
		// {
		// frontArea.setGeosetVisible(i,geoItem.isSelected());
		// frontArea.setGeosetHighlight(i,false);
		// }
		// repaint();
		// }
	}

	public boolean close()// MainPanel parent) TODO fix
	{
		// returns true if closed successfully
		boolean canceled = false;
		// int myIndex = parent.tabbedPane.indexOfComponent(this);
		if (!undoManager.isUndoListEmpty()) {
			final Object[] options = { "Yes", "No", "Cancel" };
			final int n = JOptionPane.showOptionDialog(this, "Would you like to save "
					+ model.getName()/* parent.tabbedPane.getTitleAt(myIndex) */ + " (\"" + model.getHeaderName()
					+ "\") before closing?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[2]);
			switch (n) {
			case 0:
				// ((ModelPanel)parent.tabbedPane.getComponentAt(myIndex)).getMDLDisplay().getMDL().saveFile();
				// parent.tabbedPane.remove(myIndex);
				if (editUVPanel != null) {
					editUVPanel.frame.setVisible(false);
				}
				break;
			case 1:
				// parent.tabbedPane.remove(myIndex);
				if (editUVPanel != null) {
					editUVPanel.frame.setVisible(false);
				}
				break;
			case 2:
				canceled = true;
				break;
			}
		} else {
			// parent.tabbedPane.remove(myIndex);
		}
		return !canceled;
	}

	@Override
	public void mouseEntered(final MouseEvent e) {

	}

	@Override
	public void mouseExited(final MouseEvent e) {

	}

	@Override
	public void mousePressed(final MouseEvent e) {

	}

	@Override
	public void mouseReleased(final MouseEvent e) {

	}

	@Override
	public void mouseClicked(final MouseEvent e) {

	}

	public DisplayPanel getFrontArea() {
		return frontArea;
	}

	public void setFrontArea(final DisplayPanel frontArea) {
		this.frontArea = frontArea;
	}

	public DisplayPanel getSideArea() {
		return sideArea;
	}

	public void setSideArea(final DisplayPanel sideArea) {
		this.sideArea = sideArea;
	}

	public DisplayPanel getBotArea() {
		return botArea;
	}

	public void setBotArea(final DisplayPanel botArea) {
		this.botArea = botArea;
	}

	public PerspDisplayPanel getPerspArea() {
		return perspArea;
	}

	public void setPerspArea(final PerspDisplayPanel perspArea) {
		this.perspArea = perspArea;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public void repaintSelfAndRelatedChildren() {
		repaint();
		if (editUVPanel != null) {
			editUVPanel.repaint();
		}
	}

	public void viewMatrices() {
		final ArrayList<Bone> boneRefs = new ArrayList<>();
		for (final Vertex ver : modelEditorManager.getSelectionView().getSelectedVertices()) {
			if (ver instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) ver;
				for (final Bone b : gv.getBones()) {
					if (!boneRefs.contains(b)) {
						boneRefs.add(b);
					}
				}
			}
		}
		String boneList = "";
		for (int i = 0; i < boneRefs.size(); i++) {
			if (i == boneRefs.size() - 2) {
				boneList = boneList + boneRefs.get(i).getName() + " and ";
			} else if (i == boneRefs.size() - 1) {
				boneList = boneList + boneRefs.get(i).getName();
			} else {
				boneList = boneList + boneRefs.get(i).getName() + ", ";
			}
		}
		if (boneRefs.size() == 0) {
			boneList = "Nothing was selected that was attached to any bones.";
		}
		final JTextArea tpane = new JTextArea(boneList);
		tpane.setLineWrap(true);
		tpane.setWrapStyleWord(true);
		tpane.setEditable(false);
		tpane.setSize(230, 400);

		final JScrollPane jspane = new JScrollPane(tpane);
		jspane.setPreferredSize(new Dimension(270, 230));

		JOptionPane.showMessageDialog(null, jspane);
	}

	public MDL getModel() {
		return model;
	}

	public ModelViewManager getModelViewManager() {
		return modelView;
	}

	public ModelViewManagingTree getModelViewManagingTree() {
		return modelViewManagingTree;
	}
}
