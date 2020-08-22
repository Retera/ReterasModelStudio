package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.DoNothingActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorViewportActivityManager;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoManager;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoManagerImpl;
import com.hiveworkshop.wc3.gui.modeledit.components.ComponentsPanel;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.wc3.gui.modelviewer.AnimationController;
import com.hiveworkshop.wc3.gui.modelviewer.ControlledAnimationViewer;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxUtils;

/**
 * The ModelPanel is a pane holding the display of a given MDL model. I plan to
 * tab between them.
 *
 * Eric Theller 6/7/2012
 */
public class ModelPanel implements ActionListener, MouseListener {
	private static final int VERTEX_SIZE = 3;
	private JMenuBar menuBar;
	private JMenu fileMenu, modelMenu;
	private DisplayPanel frontArea, sideArea, botArea;
	private PerspDisplayPanel perspArea;
	private EditableModel model;
	private File file;
	private final ProgramPreferences prefs;
	private final UndoHandler undoHandler;
	private final ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeNotifier;
	private final ModelEditorViewportActivityManager viewportActivityManager;
	private final ModelEditorChangeNotifier modelEditorChangeNotifier;
	private final ModelEditorManager modelEditorManager;
	private final ModelViewManager modelView;
	private final UndoManager undoManager;
	private UVPanel editUVPanel;

	private final ModelViewManagingTree modelViewManagingTree;
	private final ModelComponentBrowserTree modelComponentBrowserTree;
	private final JComponent parent;
	private final Icon icon;
	private JMenuItem menuItem;
	private final ControlledAnimationViewer animationViewer;
	private final RenderModel editorRenderModel;
	private final AnimationController animationController;
	private final ComponentsPanel componentsPanel;

	public ModelPanel(final JComponent parent, final File input, final ProgramPreferences prefs,
			final UndoHandler undoHandler, final ToolbarButtonGroup<SelectionItemTypes> notifier,
			final ToolbarButtonGroup<SelectionMode> modeNotifier,
			final ModelStructureChangeListener modelStructureChangeListener,
			final CoordDisplayListener coordDisplayListener, final ViewportTransferHandler viewportTransferHandler,
			final ViewportListener viewportListener, final Icon icon, final boolean specialBLPModel,
			final TextureExporter textureExporter) throws IOException {
		this(parent, MdxUtils.loadEditable(input), prefs, undoHandler, notifier, modeNotifier,
				modelStructureChangeListener, coordDisplayListener, viewportTransferHandler, viewportListener, icon,
				specialBLPModel, textureExporter);
		this.file = input;
	}

	public ModelPanel(final JComponent parent, final EditableModel input, final ProgramPreferences prefs,
			final UndoHandler undoHandler, final ToolbarButtonGroup<SelectionItemTypes> notifier,
			final ToolbarButtonGroup<SelectionMode> modeNotifier,
			final ModelStructureChangeListener modelStructureChangeListener,
			final CoordDisplayListener coordDisplayListener, final ViewportTransferHandler viewportTransferHandler,
			final ViewportListener viewportListener, final Icon icon, final boolean specialBLPModel,
			final TextureExporter textureExporter) {
		this.parent = parent;
		this.prefs = prefs;
		this.undoHandler = undoHandler;
		this.selectionItemTypeNotifier = notifier;
		this.icon = icon;
		this.viewportActivityManager = new ModelEditorViewportActivityManager(new DoNothingActivity());
		this.modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		this.modelEditorChangeNotifier.subscribe(this.viewportActivityManager);
		this.modelView = new ModelViewManager(input);
		this.undoManager = new UndoManagerImpl(undoHandler);
		this.editorRenderModel = new RenderModel(input, this.modelView);
		this.editorRenderModel.setSpawnParticles((prefs.getRenderParticles() == null) || prefs.getRenderParticles());
		this.editorRenderModel.setAllowInanimateParticles(
				(prefs.getRenderStaticPoseParticles() == null) || prefs.getRenderStaticPoseParticles());
		this.modelEditorManager = new ModelEditorManager(this.modelView, prefs, modeNotifier,
				this.modelEditorChangeNotifier, this.viewportActivityManager, this.editorRenderModel,
				modelStructureChangeListener);
		this.modelViewManagingTree = new ModelViewManagingTree(this.modelView, this.undoManager,
				this.modelEditorManager);
		this.modelViewManagingTree.setFocusable(false);
		this.modelComponentBrowserTree = new ModelComponentBrowserTree(this.modelView, this.undoManager,
				this.modelEditorManager, modelStructureChangeListener);

		this.selectionItemTypeNotifier.addToolbarButtonListener(new ToolbarButtonListener<SelectionItemTypes>() {
			@Override
			public void typeChanged(final SelectionItemTypes newType) {
				ModelPanel.this.modelEditorManager.setSelectionItemType(newType);
			}
		});
		// Produce the front display panel
		// file = input;
		// model = MDL.read(file);
		// dispModel = new MDLDisplay(model,this);
		loadModel(input);

		this.frontArea = new DisplayPanel("Front", (byte) 1, (byte) 2, this.modelView,
				this.modelEditorManager.getModelEditor(), modelStructureChangeListener, this.viewportActivityManager,
				prefs, this.undoManager, coordDisplayListener, undoHandler, this.modelEditorChangeNotifier,
				viewportTransferHandler, this.editorRenderModel, viewportListener);
		// frontArea.setViewport(1,2);
		this.botArea = new DisplayPanel("Bottom", (byte) 1, (byte) 0, this.modelView,
				this.modelEditorManager.getModelEditor(), modelStructureChangeListener, this.viewportActivityManager,
				prefs, this.undoManager, coordDisplayListener, undoHandler, this.modelEditorChangeNotifier,
				viewportTransferHandler, this.editorRenderModel, viewportListener);
		// botArea.setViewport(0,1);
		this.sideArea = new DisplayPanel("Side", (byte) 0, (byte) 2, this.modelView,
				this.modelEditorManager.getModelEditor(), modelStructureChangeListener, this.viewportActivityManager,
				prefs, this.undoManager, coordDisplayListener, undoHandler, this.modelEditorChangeNotifier,
				viewportTransferHandler, this.editorRenderModel, viewportListener);
		// sideArea.setViewport(0,2);

		this.animationViewer = new ControlledAnimationViewer(this.modelView, prefs, !specialBLPModel);

		this.animationController = new AnimationController(this.modelView, true, this.animationViewer,
				this.animationViewer.getCurrentAnimation());

		this.frontArea.setControlsVisible(prefs.showVMControls());
		this.botArea.setControlsVisible(prefs.showVMControls());
		this.sideArea.setControlsVisible(prefs.showVMControls());

		this.perspArea = new PerspDisplayPanel("Perspective", this.modelView, prefs, this.editorRenderModel);
		this.componentsPanel = new ComponentsPanel(textureExporter);

		this.modelComponentBrowserTree.addSelectListener(this.componentsPanel);
		// perspAreaPanel.setMinimumSize(new Dimension(200,200));
		// perspAreaPanel.add(Box.createHorizontalStrut(200));
		// perspAreaPanel.add(Box.createVerticalStrut(200));
		// perspAreaPanel.setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
		// botArea.setViewport(0,1);

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

	public RenderModel getEditorRenderModel() {
		return this.editorRenderModel;
	}

	public ControlledAnimationViewer getAnimationViewer() {
		return this.animationViewer;
	}

	public AnimationController getAnimationController() {
		return this.animationController;
	}

	public JComponent getParent() {
		return this.parent;
	}

	public void setJMenuItem(final JMenuItem item) {
		this.menuItem = item;
	}

	public JMenuItem getMenuItem() {
		return this.menuItem;
	}

	public Icon getIcon() {
		return this.icon;
	}

	public void setFile(final File file) {
		this.file = file;
	}

	public void changeActivity(final ActivityDescriptor activityDescriptor) {
		this.viewportActivityManager.setCurrentActivity(
				activityDescriptor.createActivity(this.modelEditorManager, this.modelView, this.undoManager));
	}

	public ModelEditorManager getModelEditorManager() {
		return this.modelEditorManager;
	}

	public UVPanel getEditUVPanel() {
		return this.editUVPanel;
	}

	public void setEditUVPanel(final UVPanel editUVPanel) {
		this.editUVPanel = editUVPanel;
	}

	public void loadModel(final File input) throws FileNotFoundException, IOException {
		this.file = input;
		if (this.file != null) {
			this.model = MdxUtils.loadEditable(input);
		}
	}

	public void loadModel(final EditableModel model) {
		this.model = model;
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

	public boolean close(final ModelPanelCloseListener listener)// MainPanel parent) TODO fix
	{
		// returns true if closed successfully
		boolean canceled = false;
		// int myIndex = parent.tabbedPane.indexOfComponent(this);
		if (!this.undoManager.isUndoListEmpty()) {
			final Object[] options = { "Yes", "No", "Cancel" };
			final int n = JOptionPane.showOptionDialog(this.parent,
					"Would you like to save " + this.model.getName()/* parent.tabbedPane.getTitleAt(myIndex) */ + " (\""
							+ this.model.getHeaderName() + "\") before closing?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
					options[2]);
			switch (n) {
			case JOptionPane.YES_OPTION:
				// ((ModelPanel)parent.tabbedPane.getComponentAt(myIndex)).getMDLDisplay().getMDL().saveFile();
				listener.save(this.model);
				// parent.tabbedPane.remove(myIndex);
				if (this.editUVPanel != null) {
					this.editUVPanel.view.setVisible(false);
				}
				break;
			case JOptionPane.NO_OPTION:
				// parent.tabbedPane.remove(myIndex);
				if (this.editUVPanel != null) {
					this.editUVPanel.view.setVisible(false);
				}
				break;
			case JOptionPane.CANCEL_OPTION:
				canceled = true;
				break;
			}
		} else {
			// parent.tabbedPane.remove(myIndex);
			if (this.editUVPanel != null) {
				this.editUVPanel.view.setVisible(false);
			}
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
		return this.frontArea;
	}

	public void setFrontArea(final DisplayPanel frontArea) {
		this.frontArea = frontArea;
	}

	public DisplayPanel getSideArea() {
		return this.sideArea;
	}

	public void setSideArea(final DisplayPanel sideArea) {
		this.sideArea = sideArea;
	}

	public DisplayPanel getBotArea() {
		return this.botArea;
	}

	public void setBotArea(final DisplayPanel botArea) {
		this.botArea = botArea;
	}

	public PerspDisplayPanel getPerspArea() {
		return this.perspArea;
	}

	public void setPerspArea(final PerspDisplayPanel perspArea) {
		this.perspArea = perspArea;
	}

	public UndoManager getUndoManager() {
		return this.undoManager;
	}

	public void repaintSelfAndRelatedChildren() {
		this.botArea.repaint();
		this.sideArea.repaint();
		this.frontArea.repaint();
		this.perspArea.repaint();
		this.animationViewer.repaint();
		this.animationController.repaint();
		this.modelViewManagingTree.repaint();
		if (this.editUVPanel != null) {
			this.editUVPanel.repaint();
		}
	}

	public void viewMatrices() {
		final List<Bone> boneRefs = new ArrayList<>();
		for (final Vertex ver : this.modelEditorManager.getSelectionView().getSelectedVertices()) {
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
			if (i == (boneRefs.size() - 2)) {
				boneList = boneList + boneRefs.get(i).getName() + " and ";
			} else if (i == (boneRefs.size() - 1)) {
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

	public EditableModel getModel() {
		return this.model;
	}

	public ModelViewManager getModelViewManager() {
		return this.modelView;
	}

	public ModelViewManagingTree getModelViewManagingTree() {
		return this.modelViewManagingTree;
	}

	public ModelComponentBrowserTree getModelComponentBrowserTree() {
		return this.modelComponentBrowserTree;
	}

	public ComponentsPanel getComponentsPanel() {
		return this.componentsPanel;
	}
}
