package com.matrixeater.src;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import com.etheller.warsmash.parsers.mdlx.InterpolationType;
import com.etheller.warsmash.parsers.mdlx.MdlxModel;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.GlobalIcons;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.ControllableTimeBoundProvider;
import com.hiveworkshop.wc3.gui.animedit.TimeBoundChangeListener;
import com.hiveworkshop.wc3.gui.animedit.TimeBoundChooserPanel;
import com.hiveworkshop.wc3.gui.animedit.TimeEnvironmentImpl;
import com.hiveworkshop.wc3.gui.animedit.TimeSliderPanel;
import com.hiveworkshop.wc3.gui.animedit.TimeSliderTimeListener;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.CoordDisplayListener;
import com.hiveworkshop.wc3.gui.modeledit.FaceCreationException;
import com.hiveworkshop.wc3.gui.modeledit.ImportPanel;
import com.hiveworkshop.wc3.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.wc3.gui.modeledit.ModeButton;
import com.hiveworkshop.wc3.gui.modeledit.ModelPanel;
import com.hiveworkshop.wc3.gui.modeledit.ModelPanelCloseListener;
import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.wc3.gui.modeledit.UVPanel;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.UndoHandler;
import com.hiveworkshop.wc3.gui.modeledit.Viewport;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.creator.CreatorModelingPanel;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.ExtendWidgetManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.ExtrudeWidgetManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.MoverWidgetManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.RotatorWidgetManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.ScaleWidgetManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.wc3.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportIconUtils;
import com.hiveworkshop.wc3.gui.modelviewer.AnimationViewer;
import com.hiveworkshop.wc3.gui.mpqbrowser.BLPPanel;
import com.hiveworkshop.wc3.gui.mpqbrowser.MPQBrowser;
import com.hiveworkshop.wc3.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.wc3.jworldedit.objects.DoodadTabTreeBrowserBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorTreeBrowser;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorTreeBrowser.MDLLoadListener;
import com.hiveworkshop.wc3.jworldedit.objects.UnitTabTreeBrowserBuilder;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.UVLayer;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.VisibilitySource;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdl.v2.ModelViewStateListener;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ModelOptionPane;
import com.hiveworkshop.wc3.units.ModelOptionPane.ModelElement;
import com.hiveworkshop.wc3.units.ModelOptionPanel;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.UnitOptionPane;
import com.hiveworkshop.wc3.units.UnitOptionPanel;
import com.hiveworkshop.wc3.units.fields.UnitFields;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.WTSFile;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.hiveworkshop.wc3.user.WarcraftDataSourceChangeListener;
import com.hiveworkshop.wc3.user.WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier;
import com.hiveworkshop.wc3.util.Callback;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.util.ModelUtils;
import com.hiveworkshop.wc3.util.ModelUtils.Mesh;
import com.matrixeater.imp.AnimationTransfer;
import com.owens.oobjloader.builder.Build;
import com.owens.oobjloader.parser.Parse;

import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.title.DockingWindowTitleProvider;
import net.infonode.docking.util.StringViewMap;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabBorderSizePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;
import net.miginfocom.swing.MigLayout;

/**
 * Write a description of class MainPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
/**
 * Write a description of class MainPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class MainPanel extends JPanel
		implements ActionListener, UndoHandler, ModelEditorChangeActivityListener, ModelPanelCloseListener {
	JMenuBar menuBar;
	JMenu fileMenu, recentMenu, editMenu, toolsMenu, mirrorSubmenu, tweaksSubmenu, viewMenu, importMenu, addMenu,
			scriptsMenu, windowMenu, addParticle, animationMenu, singleAnimationMenu, aboutMenu, fetch;
	JCheckBoxMenuItem mirrorFlip, fetchPortraitsToo, showNormals, textureModels, showVertexModifyControls;
	List<JMenuItem> geoItems = new ArrayList<>();
	JMenuItem newModel, open, fetchUnit, fetchModel, fetchObject, save, close, exit, revert, mergeGeoset, saveAs,
			importButton, importUnit, importGameModel, importGameObject, importFromWorkspace, importButtonS,
			newDirectory, creditsButton, changelogButton, clearRecent, nullmodelButton, selectAll, invertSelect,
			expandSelection, snapNormals, snapVertices, flipAllUVsU, flipAllUVsV, inverseAllUVs, mirrorX, mirrorY,
			mirrorZ, insideOut, insideOutNormals, showMatrices, editUVs, exportTextures, editTextures, scaleAnimations,
			animationViewer, animationController, modelingTab, mpqViewer, hiveViewer, unitViewer, preferencesWindow,
			linearizeAnimations, sortBones, simplifyKeyframes, rigButton, duplicateSelection, riseFallBirth,
			animFromFile, animFromUnit, animFromModel, animFromObject, teamColor, teamGlow;
	JMenuItem cut, copy, paste;
	List<RecentItem> recentItems = new ArrayList<>();
	UndoMenuItem undo;
	RedoMenuItem redo;

	JMenu viewMode;
	JRadioButtonMenuItem wireframe, solid;
	ButtonGroup viewModes;

	JFileChooser fc, exportTextureDialog;
	File currentFile;
	ImportPanel importPanel;
	static final ImageIcon MDLIcon = new ImageIcon(MainPanel.class.getResource("ImageBin/MDLIcon_16.png"));
	static final ImageIcon POWERED_BY_HIVE = new ImageIcon(MainPanel.class.getResource("ImageBin/powered_by_hive.png"));
	public static final ImageIcon AnimIcon = new ImageIcon(MainPanel.class.getResource("ImageBin/Anim.png"));
	protected static final boolean OLDMODE = false;
	boolean loading;
	List<ModelPanel> modelPanels;
	ModelPanel currentModelPanel;
	View frontView, leftView, bottomView, perspectiveView;
	private View timeSliderView;
	private View hackerView;
	private View previewView;
	private View creatorView;
	private View animationControllerView;
	JScrollPane geoControl;
	JScrollPane geoControlModelData;
	JTextField[] mouseCoordDisplay = new JTextField[3];
	boolean cheatShift = false;
	boolean cheatAlt = false;
	SaveProfile profile = SaveProfile.get();
	ProgramPreferences prefs = this.profile.getPreferences();// new
	// ProgramPreferences();

	JToolBar toolbar;

	TimeSliderPanel timeSliderPanel;
	private JButton setKeyframe;
	private JButton setTimeBounds;
	private ModeButton animationModeButton;
	private boolean animationModeState = false;
	private BLPPanel blpPanel;

	private final ActiveViewportWatcher activeViewportWatcher = new ActiveViewportWatcher();

	WarcraftDataSourceChangeNotifier directoryChangeNotifier = new WarcraftDataSourceChangeNotifier();

	public boolean showNormals() {
		return this.showNormals.isSelected();
	}

	public boolean showVMControls() {
		return this.showVertexModifyControls.isSelected();
	}

	public boolean textureModels() {
		return this.textureModels.isSelected();
	}

	public int viewMode() {
		if (this.wireframe.isSelected()) {
			return 0;
		} else if (this.solid.isSelected()) {
			return 1;
		}
		return -1;
	}

	JMenuItem contextClose, contextCloseAll, contextCloseOthers;
	int contextClickedTab = 0;
	JPopupMenu contextMenu;
	AbstractAction undoAction = new UndoActionImplementation("Undo", this);
	AbstractAction redoAction = new RedoActionImplementation("Redo", this);
	ClonedNodeNamePicker namePicker = new ClonedNodeNamePickerImplementation(this);
	AbstractAction cloneAction = new AbstractAction("CloneSelection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				try {
					mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor()
							.cloneSelectedComponents(MainPanel.this.namePicker));
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
				}
			}
			refreshUndo();
			repaintSelfAndChildren(mpanel);
		}
	};
	AbstractAction deleteAction = new AbstractAction("Delete") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				if (MainPanel.this.animationModeState) {
					MainPanel.this.timeSliderPanel.deleteSelectedKeyframes();
				} else {
					mpanel.getUndoManager()
							.pushAction(mpanel.getModelEditorManager().getModelEditor().deleteSelectedComponents());
				}
			}
			repaintSelfAndChildren(mpanel);
		}
	};
	AbstractAction cutAction = new AbstractAction("Cut") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				try {
					mpanel.getModelEditorManager();// cut
													// something
													// to
													// clipboard
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
				}
			}
			refreshUndo();
			repaintSelfAndChildren(mpanel);
		}
	};
	AbstractAction copyAction = new AbstractAction("Copy") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				try {
					mpanel.getModelEditorManager();// copy
													// something
													// to
													// clipboard
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
				}
			}
			refreshUndo();
			repaintSelfAndChildren(mpanel);
		}
	};
	AbstractAction pasteAction = new AbstractAction("Paste") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				try {
					mpanel.getModelEditorManager();// paste
													// something
													// from
													// clipboard
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
				}
			}
			refreshUndo();
			repaintSelfAndChildren(mpanel);
		}
	};
	AbstractAction selectAllAction = new AbstractAction("Select All") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().selectAll());
			}
			repaint();
		}
	};
	AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().invertSelection());
			}
			repaint();
		}
	};
	AbstractAction rigAction = new AbstractAction("Rig") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				boolean valid = false;
				for (final Vertex v : mpanel.getModelEditorManager().getSelectionView().getSelectedVertices()) {
					final int index = mpanel.getModel().getPivots().indexOf(v);
					if (index != -1) {
						if (index < mpanel.getModel().getIdObjects().size()) {
							final IdObject node = mpanel.getModel().getIdObject(index);
							if ((node instanceof Bone) && !(node instanceof Helper)) {
								valid = true;
							}
						}
					}
				}
				if (valid) {
					mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().rig());
				} else {
					System.err.println("NOT RIGGING, NOT VALID");
				}
			}
			repaint();
		}
	};
	AbstractAction expandSelectionAction = new AbstractAction("Expand Selection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().expandSelection());
			}
			repaint();
		}
	};
	AbstractAction snapNormalsAction = new AbstractAction("Snap Normals") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().snapNormals());
			}
			repaint();
		}
	};
	AbstractAction snapVerticesAction = new AbstractAction("Snap Vertices") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager()
						.pushAction(mpanel.getModelEditorManager().getModelEditor().snapSelectedVertices());
			}
			repaint();
		}
	};
	AbstractAction recalcNormalsAction = new AbstractAction("RecalculateNormals") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().recalcNormals());
			}
			repaint();
		}
	};
	AbstractAction recalcExtentsAction = new AbstractAction("RecalculateExtents") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				final JPanel messagePanel = new JPanel(new MigLayout());
				messagePanel.add(new JLabel("This will calculate the extents of all model components. Proceed?"),
						"wrap");
				messagePanel.add(new JLabel("(It may destroy existing extents)"), "wrap");
				final JRadioButton considerAllBtn = new JRadioButton("Consider all geosets for calculation");
				final JRadioButton considerCurrentBtn = new JRadioButton(
						"Consider current editable geosets for calculation");
				final ButtonGroup buttonGroup = new ButtonGroup();
				buttonGroup.add(considerAllBtn);
				buttonGroup.add(considerCurrentBtn);
				considerAllBtn.setSelected(true);
				messagePanel.add(considerAllBtn, "wrap");
				messagePanel.add(considerCurrentBtn, "wrap");
				final int userChoice = JOptionPane.showConfirmDialog(MainPanel.this, messagePanel, "Message",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (userChoice == JOptionPane.YES_OPTION) {
					mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor()
							.recalcExtents(considerCurrentBtn.isSelected()));
				}
			}
			repaint();
		}
	};
	AbstractAction flipAllUVsUAction = new AbstractAction("Flip All UVs U") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			for (final Geoset geo : currentMDL().getGeosets()) {
				for (final UVLayer layer : geo.getUVLayers()) {
					for (int i = 0; i < layer.numTVerteces(); i++) {
						final TVertex tvert = layer.getTVertex(i);
						tvert.y = 1.0 - tvert.y;
					}
				}
			}
			repaint();
		}
	};
	AbstractAction flipAllUVsVAction = new AbstractAction("Flip All UVs V") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			// TODO this should be an action
			for (final Geoset geo : currentMDL().getGeosets()) {
				for (final UVLayer layer : geo.getUVLayers()) {
					for (int i = 0; i < layer.numTVerteces(); i++) {
						final TVertex tvert = layer.getTVertex(i);
						tvert.y = 1.0 - tvert.y;
					}
				}
			}
			repaint();
		}
	};
	AbstractAction inverseAllUVsAction = new AbstractAction("Swap UVs U for V") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			// TODO this should be an action
			for (final Geoset geo : currentMDL().getGeosets()) {
				for (final UVLayer layer : geo.getUVLayers()) {
					for (int i = 0; i < layer.numTVerteces(); i++) {
						final TVertex tvert = layer.getTVertex(i);
						final double temp = tvert.x;
						tvert.x = tvert.y;
						tvert.y = temp;
					}
				}
			}
			repaint();
		}
	};
	AbstractAction mirrorXAction = new AbstractAction("Mirror X") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				final Vertex selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();
				mpanel.getUndoManager()
						.pushAction(mpanel.getModelEditorManager().getModelEditor().mirror((byte) 0,
								MainPanel.this.mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
								selectionCenter.z));
			}
			repaint();
		}
	};
	AbstractAction mirrorYAction = new AbstractAction("Mirror Y") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				final Vertex selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();

				mpanel.getUndoManager()
						.pushAction(mpanel.getModelEditorManager().getModelEditor().mirror((byte) 1,
								MainPanel.this.mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
								selectionCenter.z));
			}
			repaint();
		}
	};
	AbstractAction mirrorZAction = new AbstractAction("Mirror Z") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				final Vertex selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();

				mpanel.getUndoManager()
						.pushAction(mpanel.getModelEditorManager().getModelEditor().mirror((byte) 2,
								MainPanel.this.mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
								selectionCenter.z));
			}
			repaint();
		}
	};
	AbstractAction insideOutAction = new AbstractAction("Inside Out") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedFaces());
			}
			repaint();
		}
	};
	AbstractAction insideOutNormalsAction = new AbstractAction("Inside Out Normals") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.getUndoManager()
						.pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedNormals());
			}
			repaint();
		}
	};
	AbstractAction viewMatricesAction = new AbstractAction("View Matrices") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = currentModelPanel();
			if (mpanel != null) {
				mpanel.viewMatrices();
			}
			repaint();
		}
	};
	AbstractAction openAnimationViewerAction = new OpenViewAction("Animation Preview", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.previewView;
		}
	});
	AbstractAction openAnimationControllerAction = new OpenViewAction("Animation Controller", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.animationControllerView;
		}
	});
	AbstractAction openModelingTabAction = new OpenViewAction("Modeling", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.creatorView;
		}
	});
	AbstractAction openPerspectiveAction = new OpenViewAction("Perspective", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.perspectiveView;
		}
	});
	AbstractAction openOutlinerAction = new OpenViewAction("Outliner", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.viewportControllerWindowView;
		}
	});
	AbstractAction openSideAction = new OpenViewAction("Side", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.leftView;
		}
	});
	AbstractAction openTimeSliderAction = new OpenViewAction("Footer", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.timeSliderView;
		}
	});
	AbstractAction openFrontAction = new OpenViewAction("Front", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.frontView;
		}
	});
	AbstractAction openBottomAction = new OpenViewAction("Bottom", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.bottomView;
		}
	});
	AbstractAction openToolsAction = new OpenViewAction("Tools", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.toolView;
		}
	});
	AbstractAction openModelDataContentsViewAction = new OpenViewAction("Model", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.modelDataView;
		}
	});
	AbstractAction hackerViewAction = new OpenViewAction("Matrix Eater Script", new OpenViewGetter() {
		@Override
		public View getView() {
			return MainPanel.this.hackerView;
		}
	});
	AbstractAction openPreferencesAction = new AbstractAction("Open Preferences") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ProgramPreferences programPreferences = new ProgramPreferences();
			programPreferences.loadFrom(MainPanel.this.prefs);
			final List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
			final ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences,
					priorDataSources);
			// final JFrame frame = new JFrame("Preferences");
			// frame.setIconImage(MainFrame.frame.getIconImage());
			// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// frame.setContentPane(programPreferencesPanel);
			// frame.pack();
			// frame.setLocationRelativeTo(MainPanel.this);
			// frame.setVisible(true);

			final int ret = JOptionPane.showConfirmDialog(MainPanel.this, programPreferencesPanel, "Preferences",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (ret == JOptionPane.OK_OPTION) {
				MainPanel.this.prefs.loadFrom(programPreferences);
				final List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
				final boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
				if (changedDataSources) {
					SaveProfile.get().setDataSources(dataSources);
				}
				SaveProfile.save();
				if (changedDataSources) {
					dataSourcesChanged();
				}
				updateUIFromProgramPreferences();
			}
		}
	};
	AbstractAction openMPQViewerAction = new AbstractAction("Open MPQ Browser") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final View view = createMPQBrowser();
			MainPanel.this.rootWindow
					.setWindow(new SplitWindow(true, 0.75f, MainPanel.this.rootWindow.getWindow(), view));
		}
	};
	AbstractAction openUnitViewerAction = new AbstractAction("Open Unit Browser") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final UnitEditorTree unitEditorTree = createUnitEditorTree();
			MainPanel.this.rootWindow.setWindow(new SplitWindow(true, 0.75f, MainPanel.this.rootWindow.getWindow(),
					new View("Unit Browser",
							new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
							new JScrollPane(unitEditorTree))));
		}
	};
	AbstractAction openDoodadViewerAction = new AbstractAction("Open Doodad Browser") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final UnitEditorTree unitEditorTree = new UnitEditorTree(getDoodadData(), new DoodadTabTreeBrowserBuilder(),
					getUnitEditorSettings(), WorldEditorDataType.DOODADS);
			unitEditorTree.selectFirstUnit();
			// final FloatingWindow floatingWindow =
			// rootWindow.createFloatingWindow(rootWindow.getLocation(),
			// mpqBrowser.getPreferredSize(),
			// new View("MPQ Browser",
			// new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16,
			// Image.SCALE_FAST)),
			// mpqBrowser));
			// floatingWindow.getTopLevelAncestor().setVisible(true);
			unitEditorTree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					try {
						if (e.getClickCount() >= 2) {
							final TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
							if (currentUnitTreePath != null) {
								final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath
										.getLastPathComponent();
								if (o.getUserObject() instanceof MutableGameObject) {
									final MutableGameObject obj = (MutableGameObject) o.getUserObject();
									final int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
									if (numberOfVariations > 1) {
										for (int i = 0; i < numberOfVariations; i++) {
											final String path = convertPathToMDX(
													obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl");
											final String portrait = ModelUtils.getPortrait(path);
											final ImageIcon icon = new ImageIcon(com.hiveworkshop.wc3.util.IconUtils
													.getIcon(obj, WorldEditorDataType.DOODADS)
													.getScaledInstance(16, 16, Image.SCALE_DEFAULT));

											System.out.println(path);
											loadStreamMdx(MpqCodebase.get().getResourceAsStream(path), true, i == 0,
													icon);
											if (MainPanel.this.prefs.isLoadPortraits()
													&& MpqCodebase.get().has(portrait)) {
												loadStreamMdx(MpqCodebase.get().getResourceAsStream(portrait), true,
														false, icon);
											}
										}
									} else {
										final String path = convertPathToMDX(
												obj.getFieldAsString(War3ID.fromString("dfil"), 0));
										final String portrait = ModelUtils.getPortrait(path);
										final ImageIcon icon = new ImageIcon(com.hiveworkshop.wc3.util.IconUtils
												.getIcon(obj, WorldEditorDataType.DOODADS)
												.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
										System.out.println(path);
										loadStreamMdx(MpqCodebase.get().getResourceAsStream(path), true, true, icon);
										if (MainPanel.this.prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
											loadStreamMdx(MpqCodebase.get().getResourceAsStream(portrait), true, false,
													icon);
										}
									}
									MainPanel.this.toolsMenu.getAccessibleContext().setAccessibleDescription(
											"Allows the user to control which parts of the model are displayed for editing.");
									MainPanel.this.toolsMenu.setEnabled(true);
								}
							}
						}
					} catch (final Exception exc) {
						exc.printStackTrace();
						ExceptionPopup.display(exc);
					}
				}
			});
			MainPanel.this.rootWindow.setWindow(new SplitWindow(true, 0.75f, MainPanel.this.rootWindow.getWindow(),
					new View("Doodad Browser",
							new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
							new JScrollPane(unitEditorTree))));
		}
	};
	AbstractAction openHiveViewerAction = new AbstractAction("Open Hive Browser") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(BorderLayout.BEFORE_FIRST_LINE, new JLabel(POWERED_BY_HIVE));
			// final JPanel resourceFilters = new JPanel();
			// resourceFilters.setBorder(BorderFactory.createTitledBorder("Resource
			// Filters"));
			// panel.add(BorderLayout.BEFORE_LINE_BEGINS, resourceFilters);
			// resourceFilters.add(new JLabel("Resource Type"));
			// resourceFilters.add(new JComboBox<>(new String[] { "Any" }));
			final JList<String> view = new JList<>(
					new String[] { "Bongo Bongo (Phantom Shadow Beast)", "Other Model", "Other Model" });
			view.setCellRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(final javax.swing.JList<?> list, final Object value,
						final int index, final boolean isSelected, final boolean cellHasFocus) {
					final Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
					final ImageIcon icon = new ImageIcon(MainPanel.class.getResource("ImageBin/deleteme.png"));
					setIcon(new ImageIcon(icon.getImage().getScaledInstance(48, 32, Image.SCALE_DEFAULT)));
					return listCellRendererComponent;
				}
			});
			panel.add(BorderLayout.BEFORE_LINE_BEGINS, new JScrollPane(view));

			final JPanel tags = new JPanel();
			tags.setBorder(BorderFactory.createTitledBorder("Tags"));
			tags.setLayout(new GridLayout(30, 1));
			tags.add(new JCheckBox("Results must include all selected tags"));
			tags.add(new JSeparator());
			tags.add(new JLabel("Types (Models)"));
			tags.add(new JSeparator());
			tags.add(new JCheckBox("Building"));
			tags.add(new JCheckBox("Doodad"));
			tags.add(new JCheckBox("Item"));
			tags.add(new JCheckBox("User Interface"));
			panel.add(BorderLayout.CENTER, tags);
			// final FloatingWindow floatingWindow =
			// rootWindow.createFloatingWindow(rootWindow.getLocation(),
			// mpqBrowser.getPreferredSize(),
			// new View("MPQ Browser",
			// new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16,
			// Image.SCALE_FAST)),
			// mpqBrowser));
			// floatingWindow.getTopLevelAncestor().setVisible(true);
			MainPanel.this.rootWindow.setWindow(new SplitWindow(true, 0.75f, MainPanel.this.rootWindow.getWindow(),
					new View("Hive Browser",
							new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
							panel)));
		}
	};
	private ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeGroup;
	private ToolbarButtonGroup<SelectionMode> selectionModeGroup;
	private ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private JMenuItem combineAnims;
	private JMenuItem exportAnimatedToStaticMesh;
	private JMenuItem exportAnimatedFramePNG;
	private final ViewportTransferHandler viewportTransferHandler;
	private StringViewMap viewMap;
	private RootWindow rootWindow;
	private View viewportControllerWindowView, toolView, modelDataView, modelComponentView;
	private ControllableTimeBoundProvider timeBoundProvider;
	private ActivityDescriptor currentActivity;

	public MainPanel() {
		super();

		add(createJToolBar());
		// testArea = new PerspDisplayPanel("Graphic Test",2,0);
		// //botArea.setViewport(0,1);
		// add(testArea);

		final JLabel[] divider = new JLabel[3];
		for (int i = 0; i < divider.length; i++) {
			divider[i] = new JLabel("----------");
		}
		for (int i = 0; i < this.mouseCoordDisplay.length; i++) {
			this.mouseCoordDisplay[i] = new JTextField("");
			this.mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
			this.mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
			this.mouseCoordDisplay[i].setEditable(false);
		}
		this.modelStructureChangeListener = new ModelStructureChangeListenerImplementation(new ModelReference() {
			@Override
			public EditableModel getModel() {
				return currentModelPanel().getModel();
			}
		});
		this.animatedRenderEnvironment = new TimeEnvironmentImpl();
		this.blpPanel = new BLPPanel(null);
		this.timeSliderPanel = new TimeSliderPanel(this.animatedRenderEnvironment, this.modelStructureChangeListener,
				this.prefs);
		this.timeSliderPanel.setDrawing(false);
		this.timeSliderPanel.addListener(new TimeSliderTimeListener() {
			@Override
			public void timeChanged(final int currentTime) {
				MainPanel.this.animatedRenderEnvironment
						.setCurrentTime(currentTime - MainPanel.this.animatedRenderEnvironment.getStart());
				if (currentModelPanel() != null) {
					currentModelPanel().getEditorRenderModel().updateNodes(true, false);
					currentModelPanel().repaintSelfAndRelatedChildren();
				}
			}
		});
//		timeSliderPanel.addListener(creatorPanel);
		this.animatedRenderEnvironment.addChangeListener(new TimeBoundChangeListener() {
			@Override
			public void timeBoundsChanged(final int start, final int end) {
				final Integer globalSeq = MainPanel.this.animatedRenderEnvironment.getGlobalSeq();
				if (globalSeq != null) {
					MainPanel.this.creatorPanel.setChosenGlobalSeq(globalSeq);
				} else {
					final ModelPanel modelPanel = currentModelPanel();
					if (modelPanel != null) {
						boolean foundAnim = false;
						for (final Animation animation : modelPanel.getModel().getAnims()) {
							if ((animation.getStart() == start) && (animation.getEnd() == end)) {
								MainPanel.this.creatorPanel.setChosenAnimation(animation);
								foundAnim = true;
								break;
							}
						}
						if (!foundAnim) {
							MainPanel.this.creatorPanel.setChosenAnimation(null);
						}
					}

				}
			}
		});
		this.setKeyframe = new JButton(GlobalIcons.setKeyframeIcon);
		this.setKeyframe.setMargin(new Insets(0, 0, 0, 0));
		this.setKeyframe.setToolTipText("Create Keyframe");
		this.setKeyframe.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final ModelPanel mpanel = currentModelPanel();
				if (mpanel != null) {
					mpanel.getUndoManager().pushAction(
							mpanel.getModelEditorManager().getModelEditor().createKeyframe(MainPanel.this.actionType));
				}
				repaintSelfAndChildren(mpanel);
			}
		});
		this.setTimeBounds = new JButton(GlobalIcons.setTimeBoundsIcon);
		this.setTimeBounds.setMargin(new Insets(0, 0, 0, 0));
		this.setTimeBounds.setToolTipText("Choose Time Bounds");
		this.setTimeBounds.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final TimeBoundChooserPanel timeBoundChooserPanel = new TimeBoundChooserPanel(
						currentModelPanel() == null ? null : currentModelPanel().getModelViewManager(),
						MainPanel.this.modelStructureChangeListener);
				final int confirmDialogResult = JOptionPane.showConfirmDialog(MainPanel.this, timeBoundChooserPanel,
						"Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);
				if (confirmDialogResult == JOptionPane.OK_OPTION) {
					timeBoundChooserPanel.applyTo(MainPanel.this.animatedRenderEnvironment);
					if (currentModelPanel() != null) {
						currentModelPanel().getEditorRenderModel().refreshFromEditor(
								MainPanel.this.animatedRenderEnvironment, IDENTITY, IDENTITY, IDENTITY,
								currentModelPanel().getPerspArea().getViewport());
						currentModelPanel().getEditorRenderModel().updateNodes(true, false);
					}
				}
			}
		});

		this.animationModeButton = new ModeButton("Animate");
		this.animationModeButton.setVisible(false);// TODO remove this if unused

		this.contextMenu = new JPopupMenu();
		this.contextClose = new JMenuItem("Close");
		this.contextClose.addActionListener(this);
		this.contextMenu.add(this.contextClose);

		this.contextCloseOthers = new JMenuItem("Close Others");
		this.contextCloseOthers.addActionListener(this);
		this.contextMenu.add(this.contextCloseOthers);

		this.contextCloseAll = new JMenuItem("Close All");
		this.contextCloseAll.addActionListener(this);
		this.contextMenu.add(this.contextCloseAll);

		this.modelPanels = new ArrayList<>();
		final JPanel toolsPanel = new JPanel();
		toolsPanel.setMaximumSize(new Dimension(30, 999999));
		final GroupLayout layout = new GroupLayout(this);
		this.toolbar.setMaximumSize(new Dimension(80000, 48));
		this.viewMap = new StringViewMap();
		this.rootWindow = new RootWindow(this.viewMap);
		this.rootWindow.addListener(new DockingWindowListener() {
			@Override
			public void windowUndocking(final DockingWindow arg0) throws OperationAbortedException {
			}

			@Override
			public void windowUndocked(final DockingWindow dockingWindow) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								if (dockingWindow instanceof View) {
									final Component component = ((View) dockingWindow).getComponent();
									if (component instanceof JComponent) {
										linkActions(((JComponent) component).getRootPane());
//										linkActions(((JComponent) component));
									}
								}
//								final Container topLevelAncestor = dockingWindow.getTopLevelAncestor();
//								if (topLevelAncestor instanceof JComponent) {
//									linkActions(((JComponent) topLevelAncestor).getRootPane());
//									linkActions(((JComponent) topLevelAncestor));
//								}
//								topLevelAncestor.setVisible(false);
							}
						});
					}
				});
			}

			@Override
			public void windowShown(final DockingWindow arg0) {
			}

			@Override
			public void windowRestoring(final DockingWindow arg0) throws OperationAbortedException {
			}

			@Override
			public void windowRestored(final DockingWindow arg0) {
			}

			@Override
			public void windowRemoved(final DockingWindow arg0, final DockingWindow arg1) {
			}

			@Override
			public void windowMinimizing(final DockingWindow arg0) throws OperationAbortedException {
			}

			@Override
			public void windowMinimized(final DockingWindow arg0) {
			}

			@Override
			public void windowMaximizing(final DockingWindow arg0) throws OperationAbortedException {
			}

			@Override
			public void windowMaximized(final DockingWindow arg0) {
			}

			@Override
			public void windowHidden(final DockingWindow arg0) {
			}

			@Override
			public void windowDocking(final DockingWindow arg0) throws OperationAbortedException {
			}

			@Override
			public void windowDocked(final DockingWindow arg0) {
			}

			@Override
			public void windowClosing(final DockingWindow arg0) throws OperationAbortedException {
			}

			@Override
			public void windowClosed(final DockingWindow arg0) {
			}

			@Override
			public void windowAdded(final DockingWindow arg0, final DockingWindow arg1) {
			}

			@Override
			public void viewFocusChanged(final View arg0, final View arg1) {
			}
		});
		final JPanel jPanel = new JPanel();
		jPanel.add(new JLabel("..."));
		this.viewportControllerWindowView = new View("Outliner", null, jPanel);// GlobalIcons.geoIcon
//		viewportControllerWindowView.getWindowProperties().setCloseEnabled(false);
//		viewportControllerWindowView.getWindowProperties().setMaximizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setMinimizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setRestoreEnabled(true);
		this.toolView = new View("Tools", null, new JPanel());
		final JPanel contentsDummy = new JPanel();
		contentsDummy.add(new JLabel("..."));
		this.modelDataView = new View("Contents", null, contentsDummy);
		this.modelComponentView = new View("Component", null, new JPanel());
//		toolView.getWindowProperties().setCloseEnabled(false);
		this.rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties()
				.setSizePolicy(TitledTabSizePolicy.EQUAL_SIZE);
		this.rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(true);
		this.rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties()
				.setBorderSizePolicy(TitledTabBorderSizePolicy.EQUAL_SIZE);
		this.rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties()
				.getTabAreaProperties().setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
		this.rootWindow.setBackground(Color.GREEN);
		this.rootWindow.setForeground(Color.GREEN);
		final Runnable fixit = new Runnable() {
			@Override
			public void run() {
				traverseAndReset(MainPanel.this.rootWindow);
				traverseAndFix(MainPanel.this.rootWindow);
			}
		};
		this.rootWindow.addListener(new DockingWindowListener() {

			@Override
			public void windowUndocking(final DockingWindow removedWindow) throws OperationAbortedException {
				if (OLDMODE) {
					if (removedWindow instanceof View) {
						final View view = (View) removedWindow;
						view.getViewProperties().getViewTitleBarProperties().setVisible(true);
						System.out.println(
								view.getTitle() + ": (windowUndocking removedWindow as view) title bar visible now");
					}
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}

			@Override
			public void windowUndocked(final DockingWindow arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowShown(final DockingWindow arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowRestoring(final DockingWindow arg0) throws OperationAbortedException {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowRestored(final DockingWindow arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
				if (OLDMODE) {
					if (removedFromWindow instanceof TabWindow) {
						if (removedWindow instanceof View) {
							final View view = (View) removedWindow;
							view.getViewProperties().getViewTitleBarProperties().setVisible(true);
							System.out.println(view.getTitle() + ": (removedWindow as view) title bar visible now");
						}
						final TabWindow tabWindow = (TabWindow) removedFromWindow;
						if (tabWindow.getChildWindowCount() == 1) {
							final DockingWindow childWindow = tabWindow.getChildWindow(0);
							if (childWindow instanceof View) {
								final View singleChildView = (View) childWindow;
								System.out.println(singleChildView.getTitle()
										+ ": (singleChildView, windowRemoved()) title bar visible now");
								singleChildView.getViewProperties().getViewTitleBarProperties().setVisible(true);
							}
						} else if (tabWindow.getChildWindowCount() == 0) {
							System.out.println(
									tabWindow.getTitle() + ": force close because 0 child windows in windowRemoved()");
//						tabWindow.close();
						}
					}
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}

			@Override
			public void windowMinimizing(final DockingWindow arg0) throws OperationAbortedException {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowMinimized(final DockingWindow arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowMaximizing(final DockingWindow arg0) throws OperationAbortedException {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowMaximized(final DockingWindow arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowHidden(final DockingWindow arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDocking(final DockingWindow arg0) throws OperationAbortedException {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDocked(final DockingWindow arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(final DockingWindow closingWindow) throws OperationAbortedException {
				if (OLDMODE) {
					if (closingWindow.getWindowParent() instanceof TabWindow) {
						if (closingWindow instanceof View) {
							final View view = (View) closingWindow;
							view.getViewProperties().getViewTitleBarProperties().setVisible(true);
							System.out.println(view.getTitle() + ": (closingWindow as view) title bar visible now");
						}
						final TabWindow tabWindow = (TabWindow) closingWindow.getWindowParent();
						if (tabWindow.getChildWindowCount() == 1) {
							final DockingWindow childWindow = tabWindow.getChildWindow(0);
							if (childWindow instanceof View) {
								final View singleChildView = (View) childWindow;
								singleChildView.getViewProperties().getViewTitleBarProperties().setVisible(true);
								System.out.println(singleChildView.getTitle()
										+ ": (singleChildView, windowClosing()) title bar visible now");
							}
						} else if (tabWindow.getChildWindowCount() == 0) {
							System.out.println(
									tabWindow.getTitle() + ": force close because 0 child windows in windowClosing()");
							tabWindow.close();
						}
					}
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}

			@Override
			public void windowClosed(final DockingWindow closedWindow) {
			}

			@Override
			public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
				if (OLDMODE) {
					if (addedToWindow instanceof TabWindow) {
						final TabWindow tabWindow = (TabWindow) addedToWindow;
						if (tabWindow.getChildWindowCount() == 2) {
							for (int i = 0; i < 2; i++) {
								final DockingWindow childWindow = tabWindow.getChildWindow(i);
								if (childWindow instanceof View) {
									final View singleChildView = (View) childWindow;
									singleChildView.getViewProperties().getViewTitleBarProperties().setVisible(false);
									System.out.println(singleChildView.getTitle()
											+ ": (singleChildView as view, windowAdded()) title bar NOT visible now");
								}
							}
						}
						if (addedWindow instanceof View) {
							final View view = (View) addedWindow;
							view.getViewProperties().getViewTitleBarProperties().setVisible(false);
							System.out.println(view.getTitle() + ": (addedWindow as view) title bar NOT visible now");
						}
					}
				} else {
					SwingUtilities.invokeLater(fixit);
				}
			}

			@Override
			public void viewFocusChanged(final View arg0, final View arg1) {
				// TODO Auto-generated method stub

			}
		});
		this.leftView = new View("Side", null, new JPanel());
		this.frontView = new View("Front", null, new JPanel());
		this.bottomView = new View("Bottom", null, new JPanel());
		this.perspectiveView = new View("Perspective", null, new JPanel());
		this.previewView = new View("Preview", null, new JPanel());
		final JPanel timeSliderAndExtra = new JPanel();
		final GroupLayout tsaeLayout = new GroupLayout(timeSliderAndExtra);
		final Component horizontalGlue = Box.createHorizontalGlue();
		final Component verticalGlue = Box.createVerticalGlue();
		tsaeLayout.setHorizontalGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(this.timeSliderPanel)
				.addGroup(tsaeLayout.createSequentialGroup().addComponent(this.mouseCoordDisplay[0])
						.addComponent(this.mouseCoordDisplay[1]).addComponent(this.mouseCoordDisplay[2])
						.addComponent(horizontalGlue).addComponent(this.setKeyframe).addComponent(this.setTimeBounds)));
		tsaeLayout.setVerticalGroup(tsaeLayout.createSequentialGroup().addComponent(this.timeSliderPanel)
				.addGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(this.mouseCoordDisplay[0]).addComponent(this.mouseCoordDisplay[1])
						.addComponent(this.mouseCoordDisplay[2]).addComponent(horizontalGlue)
						.addComponent(this.setKeyframe).addComponent(this.setTimeBounds)));
		timeSliderAndExtra.setLayout(tsaeLayout);

		this.timeSliderView = new View("Footer", null, timeSliderAndExtra);
		final JPanel hackerPanel = new JPanel(new BorderLayout());
		final RSyntaxTextArea matrixEaterScriptTextArea = new RSyntaxTextArea(20, 60);
		matrixEaterScriptTextArea.setCodeFoldingEnabled(true);
		matrixEaterScriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		hackerPanel.add(new RTextScrollPane(matrixEaterScriptTextArea), BorderLayout.CENTER);
		final JButton run = new JButton("Run",
				new ImageIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp")
						.getScaledInstance(24, 24, Image.SCALE_FAST)));
		run.addActionListener(new ActionListener() {
			ScriptEngineManager factory = new ScriptEngineManager();

			@Override
			public void actionPerformed(final ActionEvent e) {
				final String text = matrixEaterScriptTextArea.getText();
				final ScriptEngine engine = this.factory.getEngineByName("JavaScript");
				final ModelPanel modelPanel = currentModelPanel();
				if (modelPanel != null) {
					engine.put("modelPanel", modelPanel);
					engine.put("model", modelPanel.getModel());
					engine.put("world", MainPanel.this);
					try {
						engine.eval(text);
					} catch (final ScriptException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(MainPanel.this, "Must open a file!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		hackerPanel.add(run, BorderLayout.NORTH);
		this.hackerView = new View("Matrix Eater Script", null, hackerPanel);
		this.creatorPanel = new CreatorModelingPanel(new ModelEditorChangeActivityListener() {

			@Override
			public void changeActivity(final ActivityDescriptor newType) {
				MainPanel.this.actionTypeGroup.maybeSetButtonType(newType);
				MainPanel.this.changeActivity(newType);
			}
		}, this.prefs, this.actionTypeGroup, this.activeViewportWatcher, this.animatedRenderEnvironment);
		this.creatorView = new View("Modeling", null, this.creatorPanel);
		this.animationControllerView = new View("Animation Controller", null, new JPanel());
		final TabWindow startupTabWindow = createMainLayout();
		this.rootWindow.setWindow(startupTabWindow);
		this.rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
		startupTabWindow.setSelectedTab(0);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.toolbar)
				.addComponent(this.rootWindow));
		layout.setVerticalGroup(
				layout.createSequentialGroup().addComponent(this.toolbar).addComponent(this.rootWindow));
		setLayout(layout);
		// Create a file chooser
		this.fc = new JFileChooser();
		this.fc.setAcceptAllFileFilterUsed(false);
		this.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model '-.mdx'", "mdx"));
		this.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Texture '-.blp'", "blp"));
		this.fc.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image '-.png'", "png"));
		this.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Text Model '-.mdl'", "mdl"));
		this.fc.addChoosableFileFilter(new FileNameExtensionFilter("Wavefront OBJ '-.obj'", "obj"));
		this.exportTextureDialog = new JFileChooser();
		this.exportTextureDialog.setDialogTitle("Export Texture");
		final String[] imageTypes = ImageIO.getWriterFileSuffixes();
		for (final String suffix : imageTypes) {
			this.exportTextureDialog
					.addChoosableFileFilter(new FileNameExtensionFilter(suffix.toUpperCase() + " Image File", suffix));
		}

		// getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );

		// getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );

		// setFocusable(true);
		// selectButton.requestFocus();
		this.selectionItemTypeGroup.addToolbarButtonListener(new ToolbarButtonListener<SelectionItemTypes>() {
			@Override
			public void typeChanged(final SelectionItemTypes newType) {
				MainPanel.this.animationModeState = newType == SelectionItemTypes.ANIMATE;
				// we need to refresh the state of stuff AFTER the ModelPanels, this
				// is a pretty signficant design flaw, so we're just going to
				// post to the EDT to get behind them (they're called
				// on the same notifier as this method)
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						refreshAnimationModeState();
					}
				});

				if (newType == SelectionItemTypes.TPOSE) {

					final Object[] settings = { "Move Linked", "Move Single" };
					final Object dialogResult = JOptionPane.showInputDialog(null, "Choose settings:", "T-Pose Settings",
							JOptionPane.PLAIN_MESSAGE, null, settings, settings[0]);
					final boolean moveLinked = dialogResult == settings[0];
					ModelEditorManager.MOVE_LINKED = moveLinked;
				}
				repaint();
			}
		});

		this.actionTypeGroup.addToolbarButtonListener(new ToolbarButtonListener<ToolbarActionButtonType>() {
			@Override
			public void typeChanged(final ToolbarActionButtonType newType) {
				if (newType != null) {
					changeActivity(newType);
				}
			}
		});
		this.actionTypeGroup.setToolbarButtonType(this.actionTypeGroup.getToolbarButtonTypes()[0]);
		this.viewportTransferHandler = new ViewportTransferHandler();
		this.coordDisplayListener = new CoordDisplayListener() {
			@Override
			public void notifyUpdate(final byte dimension1, final byte dimension2, final double coord1,
					final double coord2) {
				MainPanel.this.setMouseCoordDisplay(dimension1, dimension2, coord1, coord2);
			}
		};
	}

	private TabWindow createMainLayout() {
		final TabWindow leftHandTabWindow = new TabWindow(
				new DockingWindow[] { this.viewportControllerWindowView, this.toolView });
		leftHandTabWindow.setSelectedTab(0);
//		leftHandTabWindow.getWindowProperties().setCloseEnabled(false);
		final SplitWindow editingTab = new SplitWindow(false, 0.875f,
				new SplitWindow(true, 0.2f, leftHandTabWindow,
						new SplitWindow(true, 0.8f,
								new SplitWindow(false, new SplitWindow(true, this.frontView, this.bottomView),
										new SplitWindow(true, this.leftView, this.perspectiveView)),
								this.creatorView)),
				this.timeSliderView);
		editingTab.getWindowProperties().setCloseEnabled(false);
		editingTab.getWindowProperties().setTitleProvider(new DockingWindowTitleProvider() {
			@Override
			public String getTitle(final DockingWindow arg0) {
				return "Edit";
			}
		});
		ImageIcon imageIcon;
		imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));

		final View mpqBrowserView = createMPQBrowser(imageIcon);

		final UnitEditorTree unitEditorTree = createUnitEditorTree();
		final TabWindow tabWindow = new TabWindow(new DockingWindow[] {
				new View("Unit Browser", imageIcon, new JScrollPane(unitEditorTree)), mpqBrowserView });
		tabWindow.setSelectedTab(0);
		final SplitWindow viewingTab = new SplitWindow(true, 0.8f,
				new SplitWindow(true, 0.8f, this.previewView, this.animationControllerView), tabWindow);
		viewingTab.getWindowProperties().setTitleProvider(new DockingWindowTitleProvider() {
			@Override
			public String getTitle(final DockingWindow arg0) {
				return "View";
			}
		});
		viewingTab.getWindowProperties().setCloseEnabled(false);

		final SplitWindow modelTab = new SplitWindow(true, 0.2f, this.modelDataView, this.modelComponentView);
		modelTab.getWindowProperties().setTitleProvider(new DockingWindowTitleProvider() {
			@Override
			public String getTitle(final DockingWindow arg0) {
				return "Model";
			}
		});

		final TabWindow startupTabWindow = new TabWindow(new DockingWindow[] { viewingTab, editingTab, modelTab });
		traverseAndFix(startupTabWindow);
		return startupTabWindow;
	}

	private View createMPQBrowser(final ImageIcon imageIcon) {
		final MPQBrowser mpqBrowser = new MPQBrowser(MpqCodebase.get(), new Callback<String>() {
			@Override
			public void run(final String filepath) {
				if (filepath.toLowerCase().endsWith(".mdx")) {
					loadFile(MpqCodebase.get().getFile(filepath), true);
				} else if (filepath.toLowerCase().endsWith(".blp")) {
					loadBLPPathAsModel(filepath);
				} else if (filepath.toLowerCase().endsWith(".png")) {
					loadBLPPathAsModel(filepath);
				} else if (filepath.toLowerCase().endsWith(".dds")) {
					loadBLPPathAsModel(filepath, null, 1000);
				}
			}
		}, new Callback<String>() {
			@Override
			public void run(final String path) {
				final int modIndex = Math.max(path.lastIndexOf(".w3mod/"), path.lastIndexOf(".w3mod\\"));
				String finalPath;
				if (modIndex == -1) {
					finalPath = path;
				} else {
					finalPath = path.substring(modIndex + ".w3mod/".length());
				}
				final ModelPanel modelPanel = currentModelPanel();
				if (modelPanel != null) {
					if (modelPanel.getModel().getFormatVersion() > 800) {
						finalPath = finalPath.replace("\\", "/"); // Reforged prefers forward slash
					}
					modelPanel.getModel().add(new Bitmap(finalPath));
					MainPanel.this.modelStructureChangeListener.texturesChanged();
				}
			}
		});
		final View view = new View("Data Browser", imageIcon, mpqBrowser);
		view.getWindowProperties().setCloseEnabled(true);
		return view;
	}

	private View createMPQBrowser() {
		return createMPQBrowser(
				new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)));
	}

	private void traverseAndFix(final DockingWindow window) {
		final boolean tabWindow = window instanceof TabWindow;
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndFix(childWindow);
			if (tabWindow && (childWindowCount != 1) && (childWindow instanceof View)) {
				final View view = (View) childWindow;
				view.getViewProperties().getViewTitleBarProperties().setVisible(false);
			}
		}
	}

	private void traverseAndReset(final DockingWindow window) {
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndReset(childWindow);
			if (childWindow instanceof View) {
				final View view = (View) childWindow;
				view.getViewProperties().getViewTitleBarProperties().setVisible(true);
			}
		}
	}

	private void traverseAndReloadData(final DockingWindow window) {
		final int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			final DockingWindow childWindow = window.getChildWindow(i);
			traverseAndReloadData(childWindow);
			if (childWindow instanceof View) {
				final View view = (View) childWindow;
				final Component component = view.getComponent();
				if (component instanceof JScrollPane) {
					final JScrollPane pane = (JScrollPane) component;
					final Component viewportView = pane.getViewport().getView();
					if (viewportView instanceof UnitEditorTree) {
						final UnitEditorTree unitEditorTree = (UnitEditorTree) viewportView;
						final WorldEditorDataType dataType = unitEditorTree.getDataType();
						if (dataType == WorldEditorDataType.UNITS) {
							System.out.println("saw unit tree");
							unitEditorTree.setUnitDataAndReloadVerySlowly(getUnitData());
						} else if (dataType == WorldEditorDataType.DOODADS) {
							System.out.println("saw doodad tree");
							unitEditorTree.setUnitDataAndReloadVerySlowly(getDoodadData());
						}
					}
				} else if (component instanceof MPQBrowser) {
					System.out.println("saw mpq tree");
					final MPQBrowser comp = (MPQBrowser) component;
					comp.refreshTree();
				}
			}
		}
	}

	private UnitEditorTree createUnitEditorTree() {
		final UnitEditorTree unitEditorTree = new UnitEditorTreeBrowser(getUnitData(), new UnitTabTreeBrowserBuilder(),
				getUnitEditorSettings(), WorldEditorDataType.UNITS, new MDLLoadListener() {
					@Override
					public void loadFile(final String mdxFilePath, final boolean b, final boolean c,
							final ImageIcon icon) {
						MainPanel.this.loadStreamMdx(MpqCodebase.get().getResourceAsStream(mdxFilePath), b, c, icon);
					}
				}, this.prefs);
		return unitEditorTree;
	}

	@Override
	public void changeActivity(final ActivityDescriptor newType) {
		this.currentActivity = newType;
		for (final ModelPanel modelPanel : this.modelPanels) {
			modelPanel.changeActivity(newType);
		}
		this.creatorPanel.changeActivity(newType);
	}

	private static final Quaternion IDENTITY = new Quaternion();
	private TimeEnvironmentImpl animatedRenderEnvironment;
	private JButton snapButton;
	private CoordDisplayListener coordDisplayListener;
	protected ModelEditorActionType actionType;
	private JMenu teamColorMenu;
	private CreatorModelingPanel creatorPanel;
	private ToolbarActionButtonType selectAndMoveDescriptor;
	private ToolbarActionButtonType selectAndRotateDescriptor;
	private ToolbarActionButtonType selectAndScaleDescriptor;
	private ToolbarActionButtonType selectAndExtrudeDescriptor;
	private ToolbarActionButtonType selectAndExtendDescriptor;

	public void refreshAnimationModeState() {
		if (this.animationModeState) {
			if ((currentModelPanel() != null) && (currentModelPanel().getModel() != null)) {
				if (currentModelPanel().getModel().getAnimsSize() > 0) {
					final Animation anim = currentModelPanel().getModel().getAnim(0);
					this.animatedRenderEnvironment.setBounds(anim.getStart(), anim.getEnd());
				}
				currentModelPanel().getEditorRenderModel().refreshFromEditor(this.animatedRenderEnvironment, IDENTITY,
						IDENTITY, IDENTITY, currentModelPanel().getPerspArea().getViewport());
				currentModelPanel().getEditorRenderModel().updateNodes(true, false);
				this.timeSliderPanel.setNodeSelectionManager(
						currentModelPanel().getModelEditorManager().getNodeAnimationSelectionManager());
			}
			if ((this.actionTypeGroup.getActiveButtonType() == this.actionTypeGroup.getToolbarButtonTypes()[3])
					|| (this.actionTypeGroup
							.getActiveButtonType() == this.actionTypeGroup.getToolbarButtonTypes()[4])) {
				this.actionTypeGroup.setToolbarButtonType(this.actionTypeGroup.getToolbarButtonTypes()[0]);
			}
		}
		this.animatedRenderEnvironment.setStaticViewMode(!this.animationModeState);
		if (!this.animationModeState) {
			if ((currentModelPanel() != null) && (currentModelPanel().getModel() != null)) {
				currentModelPanel().getEditorRenderModel().refreshFromEditor(this.animatedRenderEnvironment, IDENTITY,
						IDENTITY, IDENTITY, currentModelPanel().getPerspArea().getViewport());
				currentModelPanel().getEditorRenderModel().updateNodes(true, false); // update to 0 position
			}
		}
		final List<ToolbarButtonGroup<ToolbarActionButtonType>.ToolbarButtonAction> buttons = this.actionTypeGroup
				.getButtons();
		final int numberOfButtons = buttons.size();
		for (int i = 3; i < numberOfButtons; i++) {
			buttons.get(i).getButton().setVisible(!this.animationModeState);
		}
		this.snapButton.setVisible(!this.animationModeState);
		this.timeSliderPanel.setDrawing(this.animationModeState);
		this.setKeyframe.setVisible(this.animationModeState);
		this.setTimeBounds.setVisible(this.animationModeState);
		this.timeSliderPanel.setKeyframeModeActive(this.animationModeState);
		if (this.animationModeState) {
			this.animationModeButton.setColors(this.prefs.getActiveColor1(), this.prefs.getActiveColor2());
		} else {
			this.animationModeButton.resetColors();
		}
		this.timeSliderPanel.repaint();
		this.creatorPanel.setAnimationModeState(this.animationModeState);
	}

	private void reloadGeosetManagers(final ModelPanel display) {
		this.geoControl.repaint();
		display.getModelViewManagingTree().reloadFromModelView();
		this.geoControl.setViewportView(display.getModelViewManagingTree());
		reloadComponentBrowser(display);
		display.getPerspArea().reloadTextures();// .mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
		display.getAnimationViewer().reload();
		display.getAnimationController().reload();
		this.creatorPanel.reloadAnimationList();

		display.getEditorRenderModel().refreshFromEditor(this.animatedRenderEnvironment, IDENTITY, IDENTITY, IDENTITY,
				display.getPerspArea().getViewport());
	}

	private void reloadComponentBrowser(final ModelPanel display) {
		this.geoControlModelData.repaint();
		display.getModelComponentBrowserTree().reloadFromModelView();
		this.geoControlModelData.setViewportView(display.getModelComponentBrowserTree());
	}

	public void reloadGUI() {
		refreshUndo();
		refreshController();
		refreshAnimationModeState();
		reloadGeosetManagers(currentModelPanel());

	}

	/**
	 * Right now this is a plug to the statics to load unit data. However, it's a
	 * non-static method so that we can have it load from an opened map in the
	 * future -- the MutableObjectData class can parse map unit data!
	 *
	 * @return
	 */
	public MutableObjectData getUnitData() {
		final War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('u');
		try {
			final MpqCodebase mpqCodebase = MpqCodebase.get();
			if (mpqCodebase.has("war3map.w3u")) {
				editorData.load(new BlizzardDataInputStream(mpqCodebase.getResourceAsStream("war3map.w3u")),
						mpqCodebase.has("war3map.wts") ? new WTSFile(mpqCodebase.getResourceAsStream("war3map.wts"))
								: null,
						true);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new MutableObjectData(WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(),
				StandardObjectData.getStandardUnitMeta(), editorData);
	}

	public MutableObjectData getDoodadData() {
		final War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('d');
		try {
			final MpqCodebase mpqCodebase = MpqCodebase.get();
			if (mpqCodebase.has("war3map.w3d")) {
				editorData.load(new BlizzardDataInputStream(mpqCodebase.getResourceAsStream("war3map.w3d")),
						mpqCodebase.has("war3map.wts") ? new WTSFile(mpqCodebase.getResourceAsStream("war3map.wts"))
								: null,
						true);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new MutableObjectData(WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(),
				StandardObjectData.getStandardDoodadMeta(), editorData);
	}

	public UnitEditorSettings getUnitEditorSettings() {
		return new UnitEditorSettings();
	}

	public JToolBar createJToolBar() {
		this.toolbar = new JToolBar(JToolBar.HORIZONTAL);
		this.toolbar.setFloatable(false);
		this.toolbar.add(new AbstractAction("New", ViewportIconUtils.loadImageIcon("icons/actions/new.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					newModel();
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});
		this.toolbar.add(new AbstractAction("Open", ViewportIconUtils.loadImageIcon("icons/actions/open.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					onClickOpen();
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});
		this.toolbar.add(new AbstractAction("Save", ViewportIconUtils.loadImageIcon("icons/actions/save.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					onClickSave();
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});
		this.toolbar.addSeparator();
		this.toolbar.add(new AbstractAction("Undo", ViewportIconUtils.loadImageIcon("icons/actions/undo.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					currentModelPanel().getUndoManager().undo();
				} catch (final NoSuchElementException exc) {
					JOptionPane.showMessageDialog(MainPanel.this, "Nothing to undo!");
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
					// exc.printStackTrace();
				}
				repaint();
			}
		});
		this.toolbar.add(new AbstractAction("Redo", ViewportIconUtils.loadImageIcon("icons/actions/redo.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					currentModelPanel().getUndoManager().redo();
				} catch (final NoSuchElementException exc) {
					JOptionPane.showMessageDialog(MainPanel.this, "Nothing to redo!");
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
					// exc.printStackTrace();
				}
				repaint();
			}
		});
		this.toolbar.addSeparator();
		this.selectionModeGroup = new ToolbarButtonGroup<>(this.toolbar, SelectionMode.values());
		this.toolbar.addSeparator();
		this.selectionItemTypeGroup = new ToolbarButtonGroup<>(this.toolbar, SelectionItemTypes.values());
		this.toolbar.addSeparator();
		this.selectAndMoveDescriptor = new ToolbarActionButtonType(
				ViewportIconUtils.loadImageIcon("icons/actions/move2.png"), "Select and Move") {
			@Override
			public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
					final ModelView modelView, final UndoActionListener undoActionListener) {
				MainPanel.this.actionType = ModelEditorActionType.TRANSLATION;
				return new ModelEditorMultiManipulatorActivity(
						new MoverWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
								modelEditorManager.getViewportSelectionHandler(), MainPanel.this.prefs, modelView),
						undoActionListener, modelEditorManager.getSelectionView());
			}
		};
		this.selectAndRotateDescriptor = new ToolbarActionButtonType(
				ViewportIconUtils.loadImageIcon("icons/actions/rotate.png"), "Select and Rotate") {
			@Override
			public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
					final ModelView modelView, final UndoActionListener undoActionListener) {
				MainPanel.this.actionType = ModelEditorActionType.ROTATION;
				return new ModelEditorMultiManipulatorActivity(
						new RotatorWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
								modelEditorManager.getViewportSelectionHandler(), MainPanel.this.prefs, modelView),
						undoActionListener, modelEditorManager.getSelectionView());
			}
		};
		this.selectAndScaleDescriptor = new ToolbarActionButtonType(
				ViewportIconUtils.loadImageIcon("icons/actions/scale.png"), "Select and Scale") {
			@Override
			public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
					final ModelView modelView, final UndoActionListener undoActionListener) {
				MainPanel.this.actionType = ModelEditorActionType.SCALING;
				return new ModelEditorMultiManipulatorActivity(
						new ScaleWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
								modelEditorManager.getViewportSelectionHandler(), MainPanel.this.prefs, modelView),
						undoActionListener, modelEditorManager.getSelectionView());
			}
		};
		this.selectAndExtrudeDescriptor = new ToolbarActionButtonType(
				ViewportIconUtils.loadImageIcon("icons/actions/extrude.png"), "Select and Extrude") {
			@Override
			public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
					final ModelView modelView, final UndoActionListener undoActionListener) {
				MainPanel.this.actionType = ModelEditorActionType.TRANSLATION;
				return new ModelEditorMultiManipulatorActivity(
						new ExtrudeWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
								modelEditorManager.getViewportSelectionHandler(), MainPanel.this.prefs, modelView),
						undoActionListener, modelEditorManager.getSelectionView());
			}
		};
		this.selectAndExtendDescriptor = new ToolbarActionButtonType(
				ViewportIconUtils.loadImageIcon("icons/actions/extend.png"), "Select and Extend") {
			@Override
			public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
					final ModelView modelView, final UndoActionListener undoActionListener) {
				MainPanel.this.actionType = ModelEditorActionType.TRANSLATION;
				return new ModelEditorMultiManipulatorActivity(
						new ExtendWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
								modelEditorManager.getViewportSelectionHandler(), MainPanel.this.prefs, modelView),
						undoActionListener, modelEditorManager.getSelectionView());
			}
		};
		this.actionTypeGroup = new ToolbarButtonGroup<>(this.toolbar,
				new ToolbarActionButtonType[] { this.selectAndMoveDescriptor, this.selectAndRotateDescriptor,
						this.selectAndScaleDescriptor, this.selectAndExtrudeDescriptor,
						this.selectAndExtendDescriptor, });
		this.currentActivity = this.actionTypeGroup.getActiveButtonType();
		this.toolbar.addSeparator();
		this.snapButton = this.toolbar
				.add(new AbstractAction("Snap", ViewportIconUtils.loadImageIcon("icons/actions/snap.png")) {
					@Override
					public void actionPerformed(final ActionEvent e) {
						try {
							final ModelPanel currentModelPanel = currentModelPanel();
							if (currentModelPanel != null) {
								currentModelPanel.getUndoManager().pushAction(currentModelPanel.getModelEditorManager()
										.getModelEditor().snapSelectedVertices());
							}
						} catch (final NoSuchElementException exc) {
							JOptionPane.showMessageDialog(MainPanel.this, "Nothing to undo!");
						} catch (final Exception exc) {
							ExceptionPopup.display(exc);
						}
					}
				});

		return this.toolbar;
	}

	public void init() {
		final JRootPane root = getRootPane();
		// JPanel root = this;
		linkActions(root);

		updateUIFromProgramPreferences();
		// if( wireframe.isSelected() ){
		// prefs.setViewMode(0);
		// }
		// else if( solid.isSelected() ){
		// prefs.setViewMode(1);
		// }
		// else {
		// prefs.setViewMode(-1);
		// }

		// defaultModelStartupHack();
	}

	private void linkActions(final JComponent root) {
		root.getActionMap().put("Undo", this.undoAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),
				"Undo");

		root.getActionMap().put("Redo", this.redoAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"),
				"Redo");

		root.getActionMap().put("Delete", this.deleteAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");

		root.getActionMap().put("CloneSelection", this.cloneAction);

		root.getActionMap().put("MaximizeSpacebar", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				final View focusedView = MainPanel.this.rootWindow.getFocusedView();
				if (focusedView != null) {
					if (focusedView.isMaximized()) {
						MainPanel.this.rootWindow.setMaximizedWindow(null);
					} else {
						focusedView.maximize();
					}
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("SPACE"),
				"MaximizeSpacebar");

		root.getActionMap().put("PressRight", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (MainPanel.this.animationModeState) {
					MainPanel.this.timeSliderPanel.jumpRight();
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("RIGHT"),
				"PressRight");
		root.getActionMap().put("PressLeft", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (MainPanel.this.animationModeState) {
					MainPanel.this.timeSliderPanel.jumpLeft();
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("LEFT"),
				"PressLeft");
		root.getActionMap().put("PressUp", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (MainPanel.this.animationModeState) {
					MainPanel.this.timeSliderPanel.jumpFrames(1);
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"), "PressUp");
		root.getActionMap().put("PressShiftUp", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (MainPanel.this.animationModeState) {
					MainPanel.this.timeSliderPanel.jumpFrames(10);
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift UP"),
				"PressShiftUp");
		root.getActionMap().put("PressDown", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (MainPanel.this.animationModeState) {
					MainPanel.this.timeSliderPanel.jumpFrames(-1);
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"),
				"PressDown");
		root.getActionMap().put("PressShiftDown", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (MainPanel.this.animationModeState) {
					MainPanel.this.timeSliderPanel.jumpFrames(-10);
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift DOWN"),
				"PressShiftDown");

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control SPACE"),
				"PlayKeyboardKey");
		root.getActionMap().put("PlayKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.timeSliderPanel.play();
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("W"),
				"QKeyboardKey");
		root.getActionMap().put("QKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.actionTypeGroup
						.setToolbarButtonType(MainPanel.this.actionTypeGroup.getToolbarButtonTypes()[0]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("E"),
				"WKeyboardKey");
		root.getActionMap().put("WKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.actionTypeGroup
						.setToolbarButtonType(MainPanel.this.actionTypeGroup.getToolbarButtonTypes()[1]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("R"),
				"EKeyboardKey");
		root.getActionMap().put("EKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.actionTypeGroup
						.setToolbarButtonType(MainPanel.this.actionTypeGroup.getToolbarButtonTypes()[2]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("T"),
				"RKeyboardKey");
		root.getActionMap().put("RKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (!MainPanel.this.animationModeState) {
					MainPanel.this.actionTypeGroup
							.setToolbarButtonType(MainPanel.this.actionTypeGroup.getToolbarButtonTypes()[3]);
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Y"),
				"TKeyboardKey");
		root.getActionMap().put("TKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (!MainPanel.this.animationModeState) {
					MainPanel.this.actionTypeGroup
							.setToolbarButtonType(MainPanel.this.actionTypeGroup.getToolbarButtonTypes()[4]);
				}
			}
		});

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("A"),
				"AKeyboardKey");
		root.getActionMap().put("AKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.selectionItemTypeGroup
						.setToolbarButtonType(MainPanel.this.selectionItemTypeGroup.getToolbarButtonTypes()[0]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("S"),
				"SKeyboardKey");
		root.getActionMap().put("SKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.selectionItemTypeGroup
						.setToolbarButtonType(MainPanel.this.selectionItemTypeGroup.getToolbarButtonTypes()[1]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("D"),
				"DKeyboardKey");
		root.getActionMap().put("DKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.selectionItemTypeGroup
						.setToolbarButtonType(MainPanel.this.selectionItemTypeGroup.getToolbarButtonTypes()[2]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F"),
				"FKeyboardKey");
		root.getActionMap().put("FKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.selectionItemTypeGroup
						.setToolbarButtonType(MainPanel.this.selectionItemTypeGroup.getToolbarButtonTypes()[3]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("G"),
				"GKeyboardKey");
		root.getActionMap().put("GKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.selectionItemTypeGroup
						.setToolbarButtonType(MainPanel.this.selectionItemTypeGroup.getToolbarButtonTypes()[4]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Z"),
				"ZKeyboardKey");
		root.getActionMap().put("ZKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				MainPanel.this.prefs.setViewMode(MainPanel.this.prefs.getViewMode() == 1 ? 0 : 1);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"),
				"CreateFaceShortcut");
		root.getActionMap().put("CreateFaceShortcut", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				if (!MainPanel.this.animationModeState) {
					try {
						final ModelPanel modelPanel = currentModelPanel();
						if (modelPanel != null) {
							final Viewport viewport = MainPanel.this.activeViewportWatcher.getViewport();
							final Vertex facingVector = viewport == null ? new Vertex(0, 0, 1)
									: viewport.getFacingVector();
							final UndoAction createFaceFromSelection = modelPanel.getModelEditorManager()
									.getModelEditor().createFaceFromSelection(facingVector);
							modelPanel.getUndoManager().pushAction(createFaceFromSelection);
						}
					} catch (final FaceCreationException exc) {
						JOptionPane.showMessageDialog(MainPanel.this, exc.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					} catch (final Exception exc) {
						ExceptionPopup.display(exc);
					}
				}
			}
		});
		for (int i = 1; i <= 9; i++) {
			root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
					.put(KeyStroke.getKeyStroke("alt pressed " + i), i + "KeyboardKey");
			final int index = i;
			root.getActionMap().put(i + "KeyboardKey", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final DockingWindow window = MainPanel.this.rootWindow.getWindow();
					if (window instanceof TabWindow) {
						final TabWindow tabWindow = (TabWindow) window;
						final int tabCount = tabWindow.getChildWindowCount();
						if ((index - 1) < tabCount) {
							tabWindow.setSelectedTab(index - 1);
						}
					}
				}
			});
		}
		// root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control
		// V"), null);
		// root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control
		// V"),
		// "CloneSelection");

		root.getActionMap().put("shiftSelect", new AbstractAction("shiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				// if (prefs.getSelectionType() == 0) {
				// for (int b = 0; b < 3; b++) {
				// buttons.get(b).resetColors();
				// }
				// addButton.setColors(prefs.getActiveColor1(),
				// prefs.getActiveColor2());
				// prefs.setSelectionType(1);
				// cheatShift = true;
				// }
				if (MainPanel.this.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
					MainPanel.this.selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
					MainPanel.this.cheatShift = true;
				}
			}
		});
		root.getActionMap().put("altSelect", new AbstractAction("altSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// if (prefs.getSelectionType() == 0) {
				// for (int b = 0; b < 3; b++) {
				// buttons.get(b).resetColors();
				// }
				// deselectButton.setColors(prefs.getActiveColor1(),
				// prefs.getActiveColor2());
				// prefs.setSelectionType(2);
				// cheatAlt = true;
				// }
				if (MainPanel.this.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
					MainPanel.this.selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
					MainPanel.this.cheatAlt = true;
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
		// root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
		// .put(KeyStroke.getKeyStroke("control pressed CONTROL"), "shiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"),
				"altSelect");

		root.getActionMap().put("unShiftSelect", new AbstractAction("unShiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Component focusedComponent = getFocusedComponent();
				if (focusedComponentNeedsTyping(focusedComponent)) {
					return;
				}
				// if (prefs.getSelectionType() == 1 && cheatShift) {
				// for (int b = 0; b < 3; b++) {
				// buttons.get(b).resetColors();
				// }
				// selectButton.setColors(prefs.getActiveColor1(),
				// prefs.getActiveColor2());
				// prefs.setSelectionType(0);
				// cheatShift = false;
				// }
				if ((MainPanel.this.selectionModeGroup.getActiveButtonType() == SelectionMode.ADD)
						&& MainPanel.this.cheatShift) {
					MainPanel.this.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					MainPanel.this.cheatShift = false;
				}
			}
		});
		root.getActionMap().put("unAltSelect", new AbstractAction("unAltSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// if (prefs.getSelectionType() == 2 && cheatAlt) {
				// for (int b = 0; b < 3; b++) {
				// buttons.get(b).resetColors();
				// }
				// selectButton.setColors(prefs.getActiveColor1(),
				// prefs.getActiveColor2());
				// prefs.setSelectionType(0);
				// cheatAlt = false;
				// }
				if ((MainPanel.this.selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT)
						&& MainPanel.this.cheatAlt) {
					MainPanel.this.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					MainPanel.this.cheatAlt = false;
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"),
				"unShiftSelect");
		// root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released
		// CONTROL"),
		// "unShiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"),
				"unAltSelect");

		root.getActionMap().put("Select All", this.selectAllAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"),
				"Select All");

		root.getActionMap().put("Invert Selection", this.invertSelectAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"),
				"Invert Selection");

		root.getActionMap().put("Expand Selection", this.expandSelectionAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"),
				"Expand Selection");

		root.getActionMap().put("RigAction", this.rigAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control W"),
				"RigAction");
	}

	private void updateUIFromProgramPreferences() {
		// prefs.setShowVertexModifierControls(showVertexModifyControls.isSelected());
		this.showVertexModifyControls.setSelected(this.prefs.isShowVertexModifierControls());
		// prefs.setTextureModels(textureModels.isSelected());
		this.textureModels.setSelected(this.prefs.isTextureModels());
		// prefs.setShowNormals(showNormals.isSelected());
		this.showNormals.setSelected(this.prefs.isShowNormals());
		// prefs.setLoadPortraits(true);
		this.fetchPortraitsToo.setSelected(this.prefs.isLoadPortraits());
		// prefs.setUseNativeMDXParser(useNativeMDXParser.isSelected());
		switch (this.prefs.getViewMode()) {
		case 0:
			this.wireframe.setSelected(true);
			break;
		case 1:
			this.solid.setSelected(true);
			break;
		default:
			break;
		}
		for (final ModelPanel mpanel : this.modelPanels) {
			mpanel.getEditorRenderModel()
					.setSpawnParticles((this.prefs.getRenderParticles() == null) || this.prefs.getRenderParticles());
			mpanel.getEditorRenderModel().setAllowInanimateParticles(
					(this.prefs.getRenderStaticPoseParticles() == null) || this.prefs.getRenderStaticPoseParticles());
			mpanel.getAnimationViewer()
					.setSpawnParticles((this.prefs.getRenderParticles() == null) || this.prefs.getRenderParticles());
		}
	}

	public JMenuBar createMenuBar() {
		// Create my menu bar
		this.menuBar = new JMenuBar();

		// Build the file menu
		this.fileMenu = new JMenu("File");
		this.fileMenu.setMnemonic(KeyEvent.VK_F);
		this.fileMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");
		this.menuBar.add(this.fileMenu);

		this.recentMenu = new JMenu("Open Recent");
		this.recentMenu.setMnemonic(KeyEvent.VK_R);
		this.recentMenu.getAccessibleContext().setAccessibleDescription("Allows you to access recently opened files.");

		this.editMenu = new JMenu("Edit");
		this.editMenu.setMnemonic(KeyEvent.VK_E);
		// editMenu.addMouseListener(this);
		this.editMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to use various tools to edit the currently selected model.");
		this.menuBar.add(this.editMenu);

		this.toolsMenu = new JMenu("Tools");
		this.toolsMenu.setMnemonic(KeyEvent.VK_T);
		this.toolsMenu.getAccessibleContext().setAccessibleDescription(
				"Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
		this.toolsMenu.setEnabled(false);
		this.menuBar.add(this.toolsMenu);

		this.viewMenu = new JMenu("View");
		// viewMenu.setMnemonic(KeyEvent.VK_V);
		this.viewMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");
		this.menuBar.add(this.viewMenu);

		this.teamColorMenu = new JMenu("Team Color");
		this.teamColorMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to control team color settings.");
		this.menuBar.add(this.teamColorMenu);

		this.directoryChangeNotifier.subscribe(new WarcraftDataSourceChangeListener() {
			@Override
			public void dataSourcesChanged() {
				MpqCodebase.get().refresh(SaveProfile.get().getDataSources());
				// cache priority order...
				UnitOptionPanel.dropRaceCache();
				DataTable.dropCache();
				ModelOptionPanel.dropCache();
				WEString.dropCache();
				BLPHandler.get().dropCache();
				MainPanel.this.teamColorMenu.removeAll();
				createTeamColorMenuItems();
				traverseAndReloadData(MainPanel.this.rootWindow);
			}
		});
		createTeamColorMenuItems();

		this.windowMenu = new JMenu("Window");
		this.windowMenu.setMnemonic(KeyEvent.VK_W);
		this.windowMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to open various windows containing the program features.");
		this.menuBar.add(this.windowMenu);

		final JMenuItem resetViewButton = new JMenuItem("Reset Layout");
		resetViewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				traverseAndReset(MainPanel.this.rootWindow);
				final TabWindow startupTabWindow = createMainLayout();
				MainPanel.this.rootWindow.setWindow(startupTabWindow);
				traverseAndFix(MainPanel.this.rootWindow);
			}
		});
		this.windowMenu.add(resetViewButton);

		final JMenu viewsMenu = new JMenu("Views");
		viewsMenu.setMnemonic(KeyEvent.VK_V);
		this.windowMenu.add(viewsMenu);

		final JMenuItem testItem = new JMenuItem("test");
		testItem.addActionListener(new OpenViewAction("Animation Preview", new OpenViewGetter() {
			@Override
			public View getView() {
				final JPanel testPanel = new JPanel();

				for (int i = 0; i < 3; i++) {
//					final ControlledAnimationViewer animationViewer = new ControlledAnimationViewer(
//							currentModelPanel().getModelViewManager(), prefs);
//					animationViewer.setMinimumSize(new Dimension(400, 400));
//					final AnimationController animationController = new AnimationController(
//							currentModelPanel().getModelViewManager(), true, animationViewer);

					final AnimationViewer animationViewer2 = new AnimationViewer(
							currentModelPanel().getModelViewManager(), MainPanel.this.prefs, false);
					animationViewer2.setMinimumSize(new Dimension(400, 400));
					testPanel.add(animationViewer2);
//					testPanel.add(animationController);
				}
				testPanel.setLayout(new GridLayout(1, 4));
				return new View("Test", null, testPanel);
			}
		}));

//		viewsMenu.add(testItem);

		this.animationViewer = new JMenuItem("Animation Preview");
		this.animationViewer.setMnemonic(KeyEvent.VK_A);
		this.animationViewer.addActionListener(this.openAnimationViewerAction);
		viewsMenu.add(this.animationViewer);

		this.animationController = new JMenuItem("Animation Controller");
		this.animationController.setMnemonic(KeyEvent.VK_C);
		this.animationController.addActionListener(this.openAnimationControllerAction);
		viewsMenu.add(this.animationController);

		this.modelingTab = new JMenuItem("Modeling");
		this.modelingTab.setMnemonic(KeyEvent.VK_M);
		this.modelingTab.addActionListener(this.openModelingTabAction);
		viewsMenu.add(this.modelingTab);

		final JMenuItem outlinerItem = new JMenuItem("Outliner");
		outlinerItem.setMnemonic(KeyEvent.VK_O);
		outlinerItem.addActionListener(this.openOutlinerAction);
		viewsMenu.add(outlinerItem);

		final JMenuItem perspectiveItem = new JMenuItem("Perspective");
		perspectiveItem.setMnemonic(KeyEvent.VK_P);
		perspectiveItem.addActionListener(this.openPerspectiveAction);
		viewsMenu.add(perspectiveItem);

		final JMenuItem frontItem = new JMenuItem("Front");
		frontItem.setMnemonic(KeyEvent.VK_F);
		frontItem.addActionListener(this.openFrontAction);
		viewsMenu.add(frontItem);

		final JMenuItem sideItem = new JMenuItem("Side");
		sideItem.setMnemonic(KeyEvent.VK_S);
		sideItem.addActionListener(this.openSideAction);
		viewsMenu.add(sideItem);

		final JMenuItem bottomItem = new JMenuItem("Bottom");
		bottomItem.setMnemonic(KeyEvent.VK_B);
		bottomItem.addActionListener(this.openBottomAction);
		viewsMenu.add(bottomItem);

		final JMenuItem toolsItem = new JMenuItem("Tools");
		toolsItem.setMnemonic(KeyEvent.VK_T);
		toolsItem.addActionListener(this.openToolsAction);
		viewsMenu.add(toolsItem);

		final JMenuItem contentsItem = new JMenuItem("Contents");
		contentsItem.setMnemonic(KeyEvent.VK_C);
		contentsItem.addActionListener(this.openModelDataContentsViewAction);
		viewsMenu.add(contentsItem);

		final JMenuItem timeItem = new JMenuItem("Footer");
		timeItem.addActionListener(this.openTimeSliderAction);
		viewsMenu.add(timeItem);

		final JMenuItem hackerViewItem = new JMenuItem("Matrix Eater Script");
		hackerViewItem.setMnemonic(KeyEvent.VK_H);
		hackerViewItem.setAccelerator(KeyStroke.getKeyStroke("control P"));
		hackerViewItem.addActionListener(this.hackerViewAction);
		viewsMenu.add(hackerViewItem);

		final JMenu browsersMenu = new JMenu("Browsers");
		browsersMenu.setMnemonic(KeyEvent.VK_B);
		this.windowMenu.add(browsersMenu);

		this.mpqViewer = new JMenuItem("Data Browser");
		this.mpqViewer.setMnemonic(KeyEvent.VK_A);
		this.mpqViewer.addActionListener(this.openMPQViewerAction);
		browsersMenu.add(this.mpqViewer);

		this.unitViewer = new JMenuItem("Unit Browser");
		this.unitViewer.setMnemonic(KeyEvent.VK_U);
		this.unitViewer.addActionListener(this.openUnitViewerAction);
		browsersMenu.add(this.unitViewer);

		final JMenuItem doodadViewer = new JMenuItem("Doodad Browser");
		doodadViewer.setMnemonic(KeyEvent.VK_D);
		doodadViewer.addActionListener(this.openDoodadViewerAction);
		browsersMenu.add(doodadViewer);

		this.hiveViewer = new JMenuItem("Hive Browser");
		this.hiveViewer.setMnemonic(KeyEvent.VK_H);
		this.hiveViewer.addActionListener(this.openHiveViewerAction);
//		browsersMenu.add(hiveViewer);

		this.windowMenu.addSeparator();

		this.addMenu = new JMenu("Add");
		this.addMenu.setMnemonic(KeyEvent.VK_A);
		this.addMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to add new components to the model.");
		this.menuBar.add(this.addMenu);

		this.addParticle = new JMenu("Particle");
		this.addParticle.setMnemonic(KeyEvent.VK_P);
		this.addMenu.add(this.addParticle);

		final File stockFolder = new File("stock/particles");
		final File[] stockFiles = stockFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".mdx");
			}
		});
		for (final File file : stockFiles) {
			final String basicName = file.getName().split("\\.")[0];
			final File pngImage = new File(file.getParent() + File.separatorChar + basicName + ".png");
			if (pngImage.exists()) {
				try {
					final Image image = ImageIO.read(pngImage);
					final JMenuItem particleItem = new JMenuItem(basicName,
							new ImageIcon(image.getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
					particleItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							ParticleEmitter2 particle;
							try {
								particle = MdxUtils.loadEditable(file).sortedIdObjects(ParticleEmitter2.class).get(0);
							} catch (final IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								return;
							}

							final JPanel particlePanel = new JPanel();
							final List<IdObject> idObjects = new ArrayList<>(currentMDL().getIdObjects());
							final Bone nullBone = new Bone("No parent");
							idObjects.add(0, nullBone);
							final JComboBox<IdObject> parent = new JComboBox<>(idObjects.toArray(new IdObject[0]));
							parent.setRenderer(new BasicComboBoxRenderer() {
								@Override
								public Component getListCellRendererComponent(final JList list, final Object value,
										final int index, final boolean isSelected, final boolean cellHasFocus) {
									final IdObject idObject = (IdObject) value;
									if (idObject == nullBone) {
										return super.getListCellRendererComponent(list, "No parent", index, isSelected,
												cellHasFocus);
									}
									return super.getListCellRendererComponent(list,
											value.getClass().getSimpleName() + " \"" + idObject.getName() + "\"", index,
											isSelected, cellHasFocus);
								}
							});
							final JLabel parentLabel = new JLabel("Parent:");
							final JLabel imageLabel = new JLabel(
									new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
							final JLabel titleLabel = new JLabel("Add " + basicName);
							titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

							final JLabel nameLabel = new JLabel("Particle Name:");
							final JTextField nameField = new JTextField("MyBlizParticle");

							final JLabel xLabel = new JLabel("Z:");
							final JSpinner xSpinner = new JSpinner(
									new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));

							final JLabel yLabel = new JLabel("X:");
							final JSpinner ySpinner = new JSpinner(
									new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));

							final JLabel zLabel = new JLabel("Y:");
							final JSpinner zSpinner = new JSpinner(
									new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));
							parent.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									final IdObject choice = parent.getItemAt(parent.getSelectedIndex());
									xSpinner.setValue(choice.getPivotPoint().x);
									ySpinner.setValue(choice.getPivotPoint().y);
									zSpinner.setValue(choice.getPivotPoint().z);
								}
							});

							final JPanel animPanel = new JPanel();
							final List<Animation> anims = currentMDL().getAnims();
							animPanel.setLayout(new GridLayout(anims.size() + 1, 1));
							final JCheckBox[] checkBoxes = new JCheckBox[anims.size()];
							int animIndex = 0;
							for (final Animation anim : anims) {
								animPanel.add(checkBoxes[animIndex] = new JCheckBox(anim.getName()));
								checkBoxes[animIndex].setSelected(true);
								animIndex++;
							}
							final JButton chooseAnimations = new JButton("Choose when to show!");
							chooseAnimations.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									JOptionPane.showMessageDialog(particlePanel, animPanel);
								}
							});
							final JButton[] colorButtons = new JButton[3];
							final Color[] colors = new Color[colorButtons.length];
							for (int i = 0; i < colorButtons.length; i++) {
								final Vertex colorValues = particle.getSegmentColor(i);
								final Color color = new Color((int) (colorValues.z * 255), (int) (colorValues.y * 255),
										(int) (colorValues.x * 255));

								final JButton button = new JButton("Color " + (i + 1),
										new ImageIcon(IconUtils.createBlank(color, 32, 32)));
								colors[i] = color;
								final int index = i;
								button.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(final ActionEvent e) {
										final Color colorChoice = JColorChooser.showDialog(MainPanel.this,
												"Chooser Color", colors[index]);
										if (colorChoice != null) {
											colors[index] = colorChoice;
											button.setIcon(new ImageIcon(IconUtils.createBlank(colors[index], 32, 32)));
										}
									}
								});
								colorButtons[i] = button;
							}

							final GroupLayout layout = new GroupLayout(particlePanel);

							layout.setHorizontalGroup(
									layout.createSequentialGroup().addComponent(imageLabel).addGap(8)
											.addGroup(layout.createParallelGroup(Alignment.CENTER)
													.addComponent(titleLabel)
													.addGroup(layout.createSequentialGroup().addComponent(nameLabel)
															.addGap(4).addComponent(nameField))
													.addGroup(layout.createSequentialGroup().addComponent(parentLabel)
															.addGap(4).addComponent(parent))
													.addComponent(chooseAnimations)
													.addGroup(layout.createSequentialGroup().addComponent(xLabel)
															.addComponent(xSpinner).addGap(4).addComponent(yLabel)
															.addComponent(ySpinner).addGap(4).addComponent(zLabel)
															.addComponent(zSpinner))
													.addGroup(
															layout.createSequentialGroup().addComponent(colorButtons[0])
																	.addGap(4).addComponent(colorButtons[1]).addGap(4)
																	.addComponent(colorButtons[2]))));
							layout.setVerticalGroup(
									layout.createParallelGroup(Alignment.CENTER).addComponent(imageLabel)
											.addGroup(
													layout.createSequentialGroup().addComponent(titleLabel)
															.addGroup(layout.createParallelGroup(Alignment.CENTER)
																	.addComponent(nameLabel).addComponent(nameField))
															.addGap(4)
															.addGroup(layout.createParallelGroup(Alignment.CENTER)
																	.addComponent(parentLabel).addComponent(parent))
															.addGap(4).addComponent(chooseAnimations).addGap(4)
															.addGroup(layout.createParallelGroup(Alignment.CENTER)
																	.addComponent(xLabel).addComponent(xSpinner)
																	.addComponent(yLabel).addComponent(ySpinner)
																	.addComponent(zLabel).addComponent(zSpinner))
															.addGap(4)
															.addGroup(layout.createParallelGroup(Alignment.CENTER)
																	.addComponent(colorButtons[0])
																	.addComponent(colorButtons[1])
																	.addComponent(colorButtons[2]))));
							particlePanel.setLayout(layout);
							final int x = JOptionPane.showConfirmDialog(MainPanel.this, particlePanel,
									"Add " + basicName, JOptionPane.OK_CANCEL_OPTION);
							if (x == JOptionPane.OK_OPTION) {
								// do stuff
								particle.setPivotPoint(new Vertex(((Number) xSpinner.getValue()).doubleValue(),
										((Number) ySpinner.getValue()).doubleValue(),
										((Number) zSpinner.getValue()).doubleValue()));
								for (int i = 0; i < colors.length; i++) {
									particle.setSegmentColor(i, new Vertex(colors[i].getBlue() / 255.00,
											colors[i].getGreen() / 255.00, colors[i].getRed() / 255.00));
								}
								final IdObject parentChoice = parent.getItemAt(parent.getSelectedIndex());
								if (parentChoice == nullBone) {
									particle.setParent(null);
								} else {
									particle.setParent(parentChoice);
								}
								AnimFlag oldFlag = particle.getVisibilityFlag();
								if (oldFlag == null) {
									oldFlag = new AnimFlag("Visibility");
								}
								final AnimFlag visFlag = AnimFlag.buildEmptyFrom(oldFlag);
								animIndex = 0;
								for (final Animation anim : anims) {
									if (!checkBoxes[animIndex].isSelected()) {
										visFlag.addEntry(anim.getStart(), Integer.valueOf(0));
									}
									animIndex++;
								}
								particle.setVisibilityFlag(visFlag);
								particle.setName(nameField.getText());
								currentMDL().add(particle);
								MainPanel.this.modelStructureChangeListener
										.nodesAdded(Collections.<IdObject>singletonList(particle));
							}
						}
					});
					this.addParticle.add(particleItem);
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		this.animationMenu = new JMenu("Animation");
		this.animationMenu.setMnemonic(KeyEvent.VK_A);
		this.addMenu.add(this.animationMenu);

		this.riseFallBirth = new JMenuItem("Rising/Falling Birth/Death");
		this.riseFallBirth.setMnemonic(KeyEvent.VK_R);
		this.riseFallBirth.addActionListener(this);
		this.animationMenu.add(this.riseFallBirth);

		this.singleAnimationMenu = new JMenu("Single");
		this.singleAnimationMenu.setMnemonic(KeyEvent.VK_S);
		this.animationMenu.add(this.singleAnimationMenu);

		this.animFromFile = new JMenuItem("From File");
		this.animFromFile.setMnemonic(KeyEvent.VK_F);
		this.animFromFile.addActionListener(this);
		this.singleAnimationMenu.add(this.animFromFile);

		this.animFromUnit = new JMenuItem("From Unit");
		this.animFromUnit.setMnemonic(KeyEvent.VK_U);
		this.animFromUnit.addActionListener(this);
		this.singleAnimationMenu.add(this.animFromUnit);

		this.animFromModel = new JMenuItem("From Model");
		this.animFromModel.setMnemonic(KeyEvent.VK_M);
		this.animFromModel.addActionListener(this);
		this.singleAnimationMenu.add(this.animFromModel);

		this.animFromObject = new JMenuItem("From Object");
		this.animFromObject.setMnemonic(KeyEvent.VK_O);
		this.animFromObject.addActionListener(this);
		this.singleAnimationMenu.add(this.animFromObject);

		this.scriptsMenu = new JMenu("Scripts");
		this.scriptsMenu.setMnemonic(KeyEvent.VK_A);
		this.scriptsMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to execute model edit scripts.");
		this.menuBar.add(this.scriptsMenu);

		this.importButtonS = new JMenuItem("Oinkerwinkle-Style AnimTransfer");
		this.importButtonS.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
		this.importButtonS.setMnemonic(KeyEvent.VK_P);
		this.importButtonS.addActionListener(this);
		// importButtonS.setEnabled(false);
		this.scriptsMenu.add(this.importButtonS);

		this.mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
		this.mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
		this.mergeGeoset.setMnemonic(KeyEvent.VK_M);
		this.mergeGeoset.addActionListener(this);
		this.scriptsMenu.add(this.mergeGeoset);

		this.nullmodelButton = new JMenuItem("Edit/delete model components");
		this.nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
		this.nullmodelButton.setMnemonic(KeyEvent.VK_E);
		this.nullmodelButton.addActionListener(this);
		this.scriptsMenu.add(this.nullmodelButton);

		this.exportAnimatedToStaticMesh = new JMenuItem("Export Animated to Static Mesh");
		this.exportAnimatedToStaticMesh.setMnemonic(KeyEvent.VK_E);
		this.exportAnimatedToStaticMesh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!MainPanel.this.animationModeState) {
					JOptionPane.showMessageDialog(MainPanel.this, "You must be in the Animation Editor to use that!",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				final Vector4f vertexHeap = new Vector4f();
				final Vector4f appliedVertexHeap = new Vector4f();
				final Vector4f vertexSumHeap = new Vector4f();
				final Vector4f normalHeap = new Vector4f();
				final Vector4f appliedNormalHeap = new Vector4f();
				final Vector4f normalSumHeap = new Vector4f();
				final ModelPanel modelContext = currentModelPanel();
				final RenderModel editorRenderModel = modelContext.getEditorRenderModel();
				final EditableModel model = modelContext.getModel();
				final ModelViewManager modelViewManager = modelContext.getModelViewManager();
				final EditableModel snapshotModel = EditableModel.deepClone(model, model.getHeaderName() + "At"
						+ editorRenderModel.getAnimatedRenderEnvironment().getAnimationTime());
				for (int geosetIndex = 0; geosetIndex < snapshotModel.getGeosets().size(); geosetIndex++) {
					final Geoset geoset = model.getGeoset(geosetIndex);
					final Geoset snapshotGeoset = snapshotModel.getGeoset(geosetIndex);
					for (int vertexIndex = 0; vertexIndex < geoset.getVertices().size(); vertexIndex++) {
						final GeosetVertex vertex = geoset.getVertex(vertexIndex);
						final GeosetVertex snapshotVertex = snapshotGeoset.getVertex(vertexIndex);
						final List<Bone> bones = vertex.getBones();
						vertexHeap.x = (float) vertex.x;
						vertexHeap.y = (float) vertex.y;
						vertexHeap.z = (float) vertex.z;
						vertexHeap.w = 1;
						if (bones.size() > 0) {
							vertexSumHeap.set(0, 0, 0, 0);
							for (final Bone bone : bones) {
								Matrix4f.transform(editorRenderModel.getRenderNode(bone).getWorldMatrix(), vertexHeap,
										appliedVertexHeap);
								Vector4f.add(vertexSumHeap, appliedVertexHeap, vertexSumHeap);
							}
							final int boneCount = bones.size();
							vertexSumHeap.x /= boneCount;
							vertexSumHeap.y /= boneCount;
							vertexSumHeap.z /= boneCount;
							vertexSumHeap.w /= boneCount;
						} else {
							vertexSumHeap.set(vertexHeap);
						}
						snapshotVertex.x = vertexSumHeap.x;
						snapshotVertex.y = vertexSumHeap.y;
						snapshotVertex.z = vertexSumHeap.z;

						normalHeap.x = (float) vertex.getNormal().x;
						normalHeap.y = (float) vertex.getNormal().y;
						normalHeap.z = (float) vertex.getNormal().z;
						normalHeap.w = 0;
						if (bones.size() > 0) {
							normalSumHeap.set(0, 0, 0, 0);
							for (final Bone bone : bones) {
								Matrix4f.transform(editorRenderModel.getRenderNode(bone).getWorldMatrix(), normalHeap,
										appliedNormalHeap);
								Vector4f.add(normalSumHeap, appliedNormalHeap, normalSumHeap);
							}

							if (normalSumHeap.length() > 0) {
								normalSumHeap.normalise();
							} else {
								normalSumHeap.set(0, 1, 0, 0);
							}
						} else {
							normalSumHeap.set(normalHeap);
						}
						snapshotVertex.getNormal().x = normalSumHeap.x;
						snapshotVertex.getNormal().y = normalSumHeap.y;
						snapshotVertex.getNormal().z = normalSumHeap.z;
					}
				}
				snapshotModel.getIdObjects().clear();
				final Bone boneRoot = new Bone("Bone_Root");
				boneRoot.setPivotPoint(new Vertex(0, 0, 0));
				snapshotModel.add(boneRoot);
				for (final Geoset geoset : snapshotModel.getGeosets()) {
					for (final GeosetVertex vertex : geoset.getVertices()) {
						vertex.getBones().clear();
						vertex.getBones().add(boneRoot);
					}
				}
				final Iterator<Geoset> geosetIterator = snapshotModel.getGeosets().iterator();
				while (geosetIterator.hasNext()) {
					final Geoset geoset = geosetIterator.next();
					final GeosetAnim geosetAnim = geoset.getGeosetAnim();
					if (geosetAnim != null) {
						final Object visibilityValue = geosetAnim.getVisibilityFlag()
								.interpolateAt(editorRenderModel.getAnimatedRenderEnvironment());
						if (visibilityValue instanceof Double) {
							final Double visibility = (Double) visibilityValue;
							final double visvalue = visibility.doubleValue();
							if (visvalue < 0.01) {
								geosetIterator.remove();
								snapshotModel.remove(geosetAnim);
							}
						}

					}
				}
				snapshotModel.getAnims().clear();
				snapshotModel.add(new Animation("Stand", 333, 1333));
				final List<AnimFlag> allAnimFlags = snapshotModel.getAllAnimFlags();
				for (final AnimFlag flag : allAnimFlags) {
					if (!flag.hasGlobalSeq()) {
						if (flag.size() > 0) {
							final Object value = flag.interpolateAt(MainPanel.this.animatedRenderEnvironment);
							flag.setInterpType(InterpolationType.DONT_INTERP);
							flag.getValues().clear();
							flag.getTimes().clear();
							flag.getInTans().clear();
							flag.getOutTans().clear();
							flag.addEntry(333, value);
						}
					}
				}
				MainPanel.this.fc.setDialogTitle("Export Static Snapshot");
				final int result = MainPanel.this.fc.showSaveDialog(MainPanel.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = MainPanel.this.fc.getSelectedFile();
					if (selectedFile != null) {
						if (!selectedFile.getPath().toLowerCase().endsWith(".mdx")) {
							selectedFile = new File(selectedFile.getPath() + ".mdx");
						}
						try {
							MdxUtils.saveMdx(snapshotModel, selectedFile);
						} catch (final IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}

			}
		});
		this.scriptsMenu.add(this.exportAnimatedToStaticMesh);

		this.exportAnimatedFramePNG = new JMenuItem("Export Animated Frame PNG");
		this.exportAnimatedFramePNG.setMnemonic(KeyEvent.VK_F);
		this.exportAnimatedFramePNG.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final BufferedImage fBufferedImage = currentModelPanel().getAnimationViewer().getBufferedImage();

				if (MainPanel.this.exportTextureDialog.getCurrentDirectory() == null) {
					final EditableModel current = currentMDL();
					if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
						MainPanel.this.fc.setCurrentDirectory(current.getFile().getParentFile());
					} else if (MainPanel.this.profile.getPath() != null) {
						MainPanel.this.fc.setCurrentDirectory(new File(MainPanel.this.profile.getPath()));
					}
				}
				if (MainPanel.this.exportTextureDialog.getCurrentDirectory() == null) {
					MainPanel.this.exportTextureDialog.setSelectedFile(
							new File(MainPanel.this.exportTextureDialog.getCurrentDirectory() + File.separator));
				}

				final int x = MainPanel.this.exportTextureDialog.showSaveDialog(MainPanel.this);
				if (x == JFileChooser.APPROVE_OPTION) {
					final File file = MainPanel.this.exportTextureDialog.getSelectedFile();
					if (file != null) {
						try {
							if (file.getName().lastIndexOf('.') >= 0) {
								BufferedImage bufferedImage = fBufferedImage;
								String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
										.toUpperCase();
								if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
										|| fileExtension.equals("JPEG")) {
									JOptionPane.showMessageDialog(MainPanel.this,
											"Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
									bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
								}
								if (fileExtension.equals("BLP")) {
									fileExtension = "blp";
								}
								final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
								if (!write) {
									JOptionPane.showMessageDialog(MainPanel.this, "File type unknown or unavailable");
								}
							} else {
								JOptionPane.showMessageDialog(MainPanel.this, "No file type was specified");
							}
						} catch (final IOException e1) {
							ExceptionPopup.display(e1);
							e1.printStackTrace();
						} catch (final Exception e2) {
							ExceptionPopup.display(e2);
							e2.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(MainPanel.this, "No output file was specified");
					}
				}
			}
		});
		this.scriptsMenu.add(this.exportAnimatedFramePNG);

		this.combineAnims = new JMenuItem("Create Back2Back Animation");
		this.combineAnims.setMnemonic(KeyEvent.VK_P);
		this.combineAnims.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final List<Animation> anims = currentMDL().getAnims();
				final Animation[] array = anims.toArray(new Animation[0]);
				final Object choice = JOptionPane.showInputDialog(MainPanel.this, "Pick the first animation",
						"Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
				final Animation animation = (Animation) choice;

				final Object choice2 = JOptionPane.showInputDialog(MainPanel.this, "Pick the second animation",
						"Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
				final Animation animation2 = (Animation) choice2;

				final String nameChoice = JOptionPane.showInputDialog(MainPanel.this,
						"What should the combined animation be called?");
				if (nameChoice != null) {
					final int anim1Length = animation.getEnd() - animation.getStart();
					final int anim2Length = animation2.getEnd() - animation2.getStart();
					final int totalLength = anim1Length + anim2Length;

					final EditableModel model = currentMDL();
					final int animTrackEnd = model.animTrackEnd();
					final int start = animTrackEnd + 1000;
					animation.copyToInterval(start, start + anim1Length, model.getAllAnimFlags(),
							model.sortedIdObjects(EventObject.class));
					animation2.copyToInterval(start + anim1Length, start + totalLength, model.getAllAnimFlags(),
							model.sortedIdObjects(EventObject.class));

					final Animation newAnimation = new Animation(nameChoice, start, start + totalLength);
					model.add(newAnimation);
					newAnimation.setNonLooping(true);
					newAnimation.setExtents(new ExtLog(animation.getExtents()));
					JOptionPane.showMessageDialog(MainPanel.this,
							"DONE! Made a combined animation called " + newAnimation.getName(), "Success",
							JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		this.scriptsMenu.add(this.combineAnims);

		this.scaleAnimations = new JMenuItem("Change Animation Lengths by Scaling");
		this.scaleAnimations.setMnemonic(KeyEvent.VK_A);
		this.scaleAnimations.addActionListener(this);
		this.scriptsMenu.add(this.scaleAnimations);

		final JMenuItem version800Toggle = new JMenuItem("Assign FormatVersion 800");
		version800Toggle.setMnemonic(KeyEvent.VK_A);
		version800Toggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				currentMDL().setFormatVersion(800);
			}
		});
		this.scriptsMenu.add(version800Toggle);

		final JMenuItem version1000Toggle = new JMenuItem("Assign FormatVersion 1000");
		version1000Toggle.setMnemonic(KeyEvent.VK_A);
		version1000Toggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				currentMDL().setFormatVersion(1000);
			}
		});
		this.scriptsMenu.add(version1000Toggle);

		final JMenuItem makeItHDItem = new JMenuItem("SD -> HD (highly experimental, requires 900 or 1000)");
		makeItHDItem.setMnemonic(KeyEvent.VK_A);
		makeItHDItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				EditableModel.makeItHD(currentMDL());
			}
		});
		this.scriptsMenu.add(makeItHDItem);

		final JMenuItem version800EditingToggle = new JMenuItem("HD -> SD (highly experimental, becomes 800)");
		version800EditingToggle.setMnemonic(KeyEvent.VK_A);
		version800EditingToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				EditableModel.convertToV800(1, currentMDL());
			}
		});
		this.scriptsMenu.add(version800EditingToggle);

		final JMenuItem recalculateTangents = new JMenuItem("Recalculate Tangents (requires 900 or 1000)");
		recalculateTangents.setMnemonic(KeyEvent.VK_A);
		recalculateTangents.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				EditableModel.recalculateTangents(currentMDL(), MainPanel.this);
			}
		});
		this.scriptsMenu.add(recalculateTangents);

		final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
		jokebutton.setMnemonic(KeyEvent.VK_A);
		jokebutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final StringBuilder sb = new StringBuilder();
				for (final File file : new File(
						"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\CustomMapData\\LuaFpsMap\\Maps\\MultiplayerFun004")
								.listFiles()) {
					if (!file.getName().toLowerCase().endsWith("_init.txt")) {
						sb.setLength(0);
						try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
							String line;
							while ((line = reader.readLine()) != null) {
								if (line.contains("BlzSetAbilityActivatedIcon")) {
									final int startIndex = line.indexOf('"') + 1;
									final int endIndex = line.lastIndexOf('"');
									final String dataString = line.substring(startIndex, endIndex);
									sb.append(dataString);
								}
							}
						} catch (final FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (final IOException e1) {
							e1.printStackTrace();
						}
						final String dataString = sb.toString();
						for (int i = 0; (i + 23) < dataString.length(); i += 24) {
							final Geoset geo = new Geoset();
							currentMDL().addGeoset(geo);
							geo.setParentModel(currentMDL());
							geo.setMaterial(new Material(new Layer("Blend", new Bitmap("textures\\white.blp"))));
							final String data = dataString.substring(i, i + 24);
							final int x = Integer.parseInt(data.substring(0, 3));
							final int y = Integer.parseInt(data.substring(3, 6));
							final int z = Integer.parseInt(data.substring(6, 9));
							final int sX = Integer.parseInt(data.substring(9, 10));
							final int sY = Integer.parseInt(data.substring(10, 11));
							final int sZ = Integer.parseInt(data.substring(11, 12));
							final int red = Integer.parseInt(data.substring(12, 15));
							final int green = Integer.parseInt(data.substring(15, 18));
							final int blue = Integer.parseInt(data.substring(18, 21));
							final int alpha = Integer.parseInt(data.substring(21, 24));
							final GeosetAnim forceGetGeosetAnim = geo.forceGetGeosetAnim();
							forceGetGeosetAnim.setStaticColor(new Vertex(blue / 255.0, green / 255.0, red / 255.0));
							forceGetGeosetAnim.setStaticAlpha(alpha / 255.0);
							System.out.println(x + "," + y + "," + z);

							final Mesh mesh = ModelUtils.createBox(new Vertex(x * 10, y * 10, z * 10),
									new Vertex((x * 10) + (sX * 10), (y * 10) + (sY * 10), (z * 10) + (sZ * 10)), 1, 1,
									1, geo);
							geo.getVertices().addAll(mesh.getVertices());
							geo.getTriangles().addAll(mesh.getTriangles());
						}
					}

				}
				MainPanel.this.modelStructureChangeListener.geosetsAdded(new ArrayList<>(currentMDL().getGeosets()));
			}
		});
//		scriptsMenu.add(jokebutton);

		final JMenuItem fixReteraLand = new JMenuItem("Fix Retera Land");
		fixReteraLand.setMnemonic(KeyEvent.VK_A);
		fixReteraLand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final EditableModel currentMDL = currentMDL();
				for (final Geoset geo : currentMDL.getGeosets()) {
					final Animation anim = new Animation(new ExtLog(currentMDL.getExtents()));
					geo.add(anim);
				}
			}
		});
//		scriptsMenu.add(fixReteraLand);

		this.aboutMenu = new JMenu("Help");
		this.aboutMenu.setMnemonic(KeyEvent.VK_H);
		this.menuBar.add(this.aboutMenu);

		this.recentMenu.add(new JSeparator());

		this.clearRecent = new JMenuItem("Clear");
		this.clearRecent.setMnemonic(KeyEvent.VK_C);
		this.clearRecent.addActionListener(this);
		this.recentMenu.add(this.clearRecent);

		updateRecent();

		this.changelogButton = new JMenuItem("Changelog");
		this.changelogButton.setMnemonic(KeyEvent.VK_A);
		this.changelogButton.addActionListener(this);
		this.aboutMenu.add(this.changelogButton);

		this.creditsButton = new JMenuItem("About");
		this.creditsButton.setMnemonic(KeyEvent.VK_A);
		this.creditsButton.addActionListener(this);
		this.aboutMenu.add(this.creditsButton);

		this.showMatrices = new JMenuItem("View Selected \"Matrices\"");
		// showMatrices.setMnemonic(KeyEvent.VK_V);
		this.showMatrices.addActionListener(this.viewMatricesAction);
		this.toolsMenu.add(this.showMatrices);

		this.insideOut = new JMenuItem("Flip all selected faces");
		this.insideOut.setMnemonic(KeyEvent.VK_I);
		this.insideOut.addActionListener(this.insideOutAction);
		this.insideOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
		this.toolsMenu.add(this.insideOut);

		this.insideOutNormals = new JMenuItem("Flip all selected normals");
		this.insideOutNormals.addActionListener(this.insideOutNormalsAction);
		this.toolsMenu.add(this.insideOutNormals);

		this.toolsMenu.add(new JSeparator());

		this.editUVs = new JMenuItem("Edit UV Mapping");
		this.editUVs.setMnemonic(KeyEvent.VK_U);
		this.editUVs.addActionListener(this);
		this.toolsMenu.add(this.editUVs);

		this.editTextures = new JMenuItem("Edit Textures");
		this.editTextures.setMnemonic(KeyEvent.VK_T);
		this.editTextures.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final TextureManager textureManager = new TextureManager(currentModelPanel().getModelViewManager(),
						MainPanel.this.modelStructureChangeListener, MainPanel.this.textureExporter);
				final JFrame frame = new JFrame("Edit Textures");
				textureManager.setSize(new Dimension(800, 650));
				frame.setContentPane(textureManager);
				frame.setSize(textureManager.getSize());
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			}
		});
		this.toolsMenu.add(this.editTextures);

		this.rigButton = new JMenuItem("Rig Selection");
		this.rigButton.setMnemonic(KeyEvent.VK_R);
		this.rigButton.setAccelerator(KeyStroke.getKeyStroke("control W"));
		this.rigButton.addActionListener(this.rigAction);
		this.toolsMenu.add(this.rigButton);

		this.tweaksSubmenu = new JMenu("Tweaks");
		this.tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
		this.tweaksSubmenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to tweak conversion mistakes.");
		this.toolsMenu.add(this.tweaksSubmenu);

		this.flipAllUVsU = new JMenuItem("Flip All UVs U");
		this.flipAllUVsU.setMnemonic(KeyEvent.VK_U);
		this.flipAllUVsU.addActionListener(this.flipAllUVsUAction);
		this.tweaksSubmenu.add(this.flipAllUVsU);

		this.flipAllUVsV = new JMenuItem("Flip All UVs V");
		// flipAllUVsV.setMnemonic(KeyEvent.VK_V);
		this.flipAllUVsV.addActionListener(this.flipAllUVsVAction);
		this.tweaksSubmenu.add(this.flipAllUVsV);

		this.inverseAllUVs = new JMenuItem("Swap All UVs U for V");
		this.inverseAllUVs.setMnemonic(KeyEvent.VK_S);
		this.inverseAllUVs.addActionListener(this.inverseAllUVsAction);
		this.tweaksSubmenu.add(this.inverseAllUVs);

		this.mirrorSubmenu = new JMenu("Mirror");
		this.mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		this.mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
		this.toolsMenu.add(this.mirrorSubmenu);

		this.mirrorX = new JMenuItem("Mirror X");
		this.mirrorX.setMnemonic(KeyEvent.VK_X);
		this.mirrorX.addActionListener(this.mirrorXAction);
		this.mirrorSubmenu.add(this.mirrorX);

		this.mirrorY = new JMenuItem("Mirror Y");
		this.mirrorY.setMnemonic(KeyEvent.VK_Y);
		this.mirrorY.addActionListener(this.mirrorYAction);
		this.mirrorSubmenu.add(this.mirrorY);

		this.mirrorZ = new JMenuItem("Mirror Z");
		this.mirrorZ.setMnemonic(KeyEvent.VK_Z);
		this.mirrorZ.addActionListener(this.mirrorZAction);
		this.mirrorSubmenu.add(this.mirrorZ);

		this.mirrorSubmenu.add(new JSeparator());

		this.mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
		this.mirrorFlip.setMnemonic(KeyEvent.VK_A);
		this.mirrorSubmenu.add(this.mirrorFlip);

		this.textureModels = new JCheckBoxMenuItem("Texture Models", true);
		this.textureModels.setMnemonic(KeyEvent.VK_T);
		this.textureModels.setSelected(true);
		this.textureModels.addActionListener(this);
		this.viewMenu.add(this.textureModels);

		this.newDirectory = new JMenuItem("Change Game Directory");
		this.newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
		this.newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
		this.newDirectory.setMnemonic(KeyEvent.VK_D);
		this.newDirectory.addActionListener(this);
//		viewMenu.add(newDirectory);

		this.viewMenu.add(new JSeparator());

		this.showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
		// showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
		this.showVertexModifyControls.addActionListener(this);
		this.viewMenu.add(this.showVertexModifyControls);

		this.viewMenu.add(new JSeparator());

		this.showNormals = new JCheckBoxMenuItem("Show Normals", true);
		this.showNormals.setMnemonic(KeyEvent.VK_N);
		this.showNormals.setSelected(false);
		this.showNormals.addActionListener(this);
		this.viewMenu.add(this.showNormals);

		this.viewMode = new JMenu("3D View Mode");
		this.viewMenu.add(this.viewMode);

		this.viewModes = new ButtonGroup();

		final ActionListener repainter = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (MainPanel.this.wireframe.isSelected()) {
					MainPanel.this.prefs.setViewMode(0);
				} else if (MainPanel.this.solid.isSelected()) {
					MainPanel.this.prefs.setViewMode(1);
				} else {
					MainPanel.this.prefs.setViewMode(-1);
				}
				repaint();
			}
		};

		this.wireframe = new JRadioButtonMenuItem("Wireframe");
		this.wireframe.addActionListener(repainter);
		this.viewMode.add(this.wireframe);
		this.viewModes.add(this.wireframe);

		this.solid = new JRadioButtonMenuItem("Solid");
		this.solid.addActionListener(repainter);
		this.viewMode.add(this.solid);
		this.viewModes.add(this.solid);

		this.viewModes.setSelected(this.solid.getModel(), true);

		this.newModel = new JMenuItem("New");
		this.newModel.setAccelerator(KeyStroke.getKeyStroke("control N"));
		this.newModel.setMnemonic(KeyEvent.VK_N);
		this.newModel.addActionListener(this);
		this.fileMenu.add(this.newModel);

		this.open = new JMenuItem("Open");
		this.open.setAccelerator(KeyStroke.getKeyStroke("control O"));
		this.open.setMnemonic(KeyEvent.VK_O);
		this.open.addActionListener(this);
		this.fileMenu.add(this.open);

		this.fileMenu.add(this.recentMenu);

		this.fetch = new JMenu("Open Internal");
		this.fetch.setMnemonic(KeyEvent.VK_F);
		this.fileMenu.add(this.fetch);

		this.fetchUnit = new JMenuItem("Unit");
		this.fetchUnit.setAccelerator(KeyStroke.getKeyStroke("control U"));
		this.fetchUnit.setMnemonic(KeyEvent.VK_U);
		this.fetchUnit.addActionListener(this);
		this.fetch.add(this.fetchUnit);

		this.fetchModel = new JMenuItem("Model");
		this.fetchModel.setAccelerator(KeyStroke.getKeyStroke("control M"));
		this.fetchModel.setMnemonic(KeyEvent.VK_M);
		this.fetchModel.addActionListener(this);
		this.fetch.add(this.fetchModel);

		this.fetchObject = new JMenuItem("Object Editor");
		this.fetchObject.setAccelerator(KeyStroke.getKeyStroke("control O"));
		this.fetchObject.setMnemonic(KeyEvent.VK_O);
		this.fetchObject.addActionListener(this);
		this.fetch.add(this.fetchObject);

		this.fetch.add(new JSeparator());

		this.fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
		this.fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
		this.fetchPortraitsToo.setSelected(true);
		this.fetchPortraitsToo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				MainPanel.this.prefs.setLoadPortraits(MainPanel.this.fetchPortraitsToo.isSelected());
			}

		});
		this.fetch.add(this.fetchPortraitsToo);

		this.fileMenu.add(new JSeparator());

		this.importMenu = new JMenu("Import");
		this.importMenu.setMnemonic(KeyEvent.VK_I);
		this.fileMenu.add(this.importMenu);

		this.importButton = new JMenuItem("From File");
		this.importButton.setAccelerator(KeyStroke.getKeyStroke("control shift I"));
		this.importButton.setMnemonic(KeyEvent.VK_I);
		this.importButton.addActionListener(this);
		this.importMenu.add(this.importButton);

		this.importUnit = new JMenuItem("From Unit");
		this.importUnit.setMnemonic(KeyEvent.VK_U);
		this.importUnit.setAccelerator(KeyStroke.getKeyStroke("control shift U"));
		this.importUnit.addActionListener(this);
		this.importMenu.add(this.importUnit);

		this.importGameModel = new JMenuItem("From WC3 Model");
		this.importGameModel.setMnemonic(KeyEvent.VK_M);
		this.importGameModel.addActionListener(this);
		this.importMenu.add(this.importGameModel);

		this.importGameObject = new JMenuItem("From Object Editor");
		this.importGameObject.setMnemonic(KeyEvent.VK_O);
		this.importGameObject.addActionListener(this);
		this.importMenu.add(this.importGameObject);

		this.importFromWorkspace = new JMenuItem("From Workspace");
		this.importFromWorkspace.setMnemonic(KeyEvent.VK_O);
		this.importFromWorkspace.addActionListener(this);
		this.importMenu.add(this.importFromWorkspace);

		this.save = new JMenuItem("Save");
		this.save.setMnemonic(KeyEvent.VK_S);
		this.save.setAccelerator(KeyStroke.getKeyStroke("control S"));
		this.save.addActionListener(this);
		this.fileMenu.add(this.save);

		this.saveAs = new JMenuItem("Save as");
		this.saveAs.setMnemonic(KeyEvent.VK_A);
		this.saveAs.setAccelerator(KeyStroke.getKeyStroke("control Q"));
		this.saveAs.addActionListener(this);
		this.fileMenu.add(this.saveAs);

		this.fileMenu.add(new JSeparator());

		this.exportTextures = new JMenuItem("Export Texture");
		this.exportTextures.setMnemonic(KeyEvent.VK_E);
		this.exportTextures.addActionListener(this);
		this.fileMenu.add(this.exportTextures);

		this.fileMenu.add(new JSeparator());

		this.revert = new JMenuItem("Revert");
		this.revert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final ModelPanel modelPanel = currentModelPanel();
				final int oldIndex = MainPanel.this.modelPanels.indexOf(modelPanel);
				if (modelPanel != null) {
					if (modelPanel.close(MainPanel.this)) {
						MainPanel.this.modelPanels.remove(modelPanel);
						MainPanel.this.windowMenu.remove(modelPanel.getMenuItem());
						if (MainPanel.this.modelPanels.size() > 0) {
							final int newIndex = Math.min(MainPanel.this.modelPanels.size() - 1, oldIndex);
							setCurrentModel(MainPanel.this.modelPanels.get(newIndex));
						} else {
							// TODO remove from notifiers to fix leaks
							setCurrentModel(null);
						}
						final File fileToRevert = modelPanel.getModel().getFile();
						loadFile(fileToRevert);
					}
				}
			}
		});
		this.fileMenu.add(this.revert);

		this.close = new JMenuItem("Close");
		this.close.setAccelerator(KeyStroke.getKeyStroke("control E"));
		this.close.setMnemonic(KeyEvent.VK_E);
		this.close.addActionListener(this);
		this.fileMenu.add(this.close);

		this.fileMenu.add(new JSeparator());

		this.exit = new JMenuItem("Exit");
		this.exit.setMnemonic(KeyEvent.VK_E);
		this.exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (closeAll()) {
					MainFrame.frame.dispose();
				}
			}
		});
		this.fileMenu.add(this.exit);

		this.undo = new UndoMenuItem("Undo");
		this.undo.addActionListener(this.undoAction);
		this.undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
		// undo.addMouseListener(this);
		this.editMenu.add(this.undo);
		this.undo.setEnabled(this.undo.funcEnabled());

		this.redo = new RedoMenuItem("Redo");
		this.redo.addActionListener(this.redoAction);
		this.redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
		// redo.addMouseListener(this);
		this.editMenu.add(this.redo);
		this.redo.setEnabled(this.redo.funcEnabled());

		this.editMenu.add(new JSeparator());

		final JMenu optimizeMenu = new JMenu("Optimize");
		optimizeMenu.setMnemonic(KeyEvent.VK_O);
		this.editMenu.add(optimizeMenu);

		this.linearizeAnimations = new JMenuItem("Linearize Animations");
		this.linearizeAnimations.setMnemonic(KeyEvent.VK_L);
		this.linearizeAnimations.addActionListener(this);
		optimizeMenu.add(this.linearizeAnimations);

		this.simplifyKeyframes = new JMenuItem("Simplify Keyframes (Experimental)");
		this.simplifyKeyframes.setMnemonic(KeyEvent.VK_K);
		this.simplifyKeyframes.addActionListener(this);
		optimizeMenu.add(this.simplifyKeyframes);

		final JMenuItem minimizeGeoset = new JMenuItem("Minimize Geosets");
		minimizeGeoset.setMnemonic(KeyEvent.VK_K);
		minimizeGeoset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int confirm = JOptionPane.showConfirmDialog(MainPanel.this,
						"This is experimental and I did not code the Undo option for it yet. Continue?\nMy advice is to click cancel and save once first.",
						"Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm != JOptionPane.OK_OPTION) {
					return;
				}

				currentMDL().doSavePreps();

				final Map<Geoset, Geoset> sourceToDestination = new HashMap<>();
				final List<Geoset> retainedGeosets = new ArrayList<>();
				for (final Geoset geoset : currentMDL().getGeosets()) {
					boolean alreadyRetained = false;
					for (final Geoset retainedGeoset : retainedGeosets) {
						if (retainedGeoset.getMaterial().equals(geoset.getMaterial())
								&& (retainedGeoset.getSelectionGroup() == geoset.getSelectionGroup())
								&& (retainedGeoset.getUnselectable() == geoset.getUnselectable())
								&& mergableGeosetAnims(retainedGeoset.getGeosetAnim(), geoset.getGeosetAnim())) {
							alreadyRetained = true;
							for (final GeosetVertex gv : geoset.getVertices()) {
								retainedGeoset.add(gv);
							}
							for (final Triangle t : geoset.getTriangles()) {
								retainedGeoset.add(t);
							}
							break;
						}
					}
					if (!alreadyRetained) {
						retainedGeosets.add(geoset);
					}
				}
				final EditableModel currentMDL = currentMDL();
				final List<Geoset> geosets = currentMDL.getGeosets();
				final List<Geoset> geosetsRemoved = new ArrayList<>();
				final Iterator<Geoset> iterator = geosets.iterator();
				while (iterator.hasNext()) {
					final Geoset geoset = iterator.next();
					if (!retainedGeosets.contains(geoset)) {
						iterator.remove();
						final GeosetAnim geosetAnim = geoset.getGeosetAnim();
						if (geosetAnim != null) {
							currentMDL.remove(geosetAnim);
						}
						geosetsRemoved.add(geoset);
					}
				}
				MainPanel.this.modelStructureChangeListener.geosetsRemoved(geosetsRemoved);
			}

			private boolean mergableGeosetAnims(final GeosetAnim first, final GeosetAnim second) {
				if ((first == null) && (second == null)) {
					return true;
				}
				if ((first == null) || (second == null)) {
					return false;
				}
				final AnimFlag firstVisibilityFlag = first.getVisibilityFlag();
				final AnimFlag secondVisibilityFlag = second.getVisibilityFlag();
				if ((firstVisibilityFlag == null) != (secondVisibilityFlag == null)) {
					return false;
				}
				if ((firstVisibilityFlag != null) && !firstVisibilityFlag.equals(secondVisibilityFlag)) {
					return false;
				}
				if (first.isDropShadow() != second.isDropShadow()) {
					return false;
				}
				if (Math.abs(first.getStaticAlpha() - second.getStaticAlpha()) > 0.001) {
					return false;
				}
				if ((first.getStaticColor() == null) != (second.getStaticColor() == null)) {
					return false;
				}
				if ((first.getStaticColor() != null) && !first.getStaticColor().equalLocs(second.getStaticColor())) {
					return false;
				}
				final AnimFlag firstAnimatedColor = first.find("Color");
				final AnimFlag secondAnimatedColor = second.find("Color");
				if ((firstAnimatedColor == null) != (secondAnimatedColor == null)) {
					return false;
				}
				if ((firstAnimatedColor != null) && !firstAnimatedColor.equals(secondAnimatedColor)) {
					return false;
				}
				return true;
			}
		});
		optimizeMenu.add(minimizeGeoset);

		this.sortBones = new JMenuItem("Sort Nodes");
		this.sortBones.setMnemonic(KeyEvent.VK_S);
		this.sortBones.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final EditableModel model = currentMDL();
				final List<IdObject> roots = new ArrayList<>();
				final List<IdObject> modelList = model.getIdObjects();
				for (final IdObject object : modelList) {
					if (object.getParent() == null) {
						roots.add(object);
					}
				}
				final Queue<IdObject> bfsQueue = new LinkedList<>(roots);
				final List<IdObject> result = new ArrayList<>();
				while (!bfsQueue.isEmpty()) {
					final IdObject nextItem = bfsQueue.poll();
					bfsQueue.addAll(nextItem.getChildrenNodes());
					result.add(nextItem);
				}
				for (final IdObject node : result) {
					model.remove(node);
				}
				MainPanel.this.modelStructureChangeListener.nodesRemoved(result);
				for (final IdObject node : result) {
					model.add(node);
				}
				MainPanel.this.modelStructureChangeListener.nodesAdded(result);
			}
		});
		optimizeMenu.add(this.sortBones);

		final JMenuItem flushUnusedTexture = new JMenuItem("Flush Unused Texture");
		flushUnusedTexture.setEnabled(false);
		flushUnusedTexture.setMnemonic(KeyEvent.VK_F);
		optimizeMenu.add(flushUnusedTexture);

		final JMenuItem recalcNormals = new JMenuItem("Recalculate Normals");
		recalcNormals.setAccelerator(KeyStroke.getKeyStroke("control N"));
		recalcNormals.addActionListener(this.recalcNormalsAction);
		this.editMenu.add(recalcNormals);

		final JMenuItem recalcExtents = new JMenuItem("Recalculate Extents");
		recalcExtents.setAccelerator(KeyStroke.getKeyStroke("control shift E"));
		recalcExtents.addActionListener(this.recalcExtentsAction);
		this.editMenu.add(recalcExtents);

		this.editMenu.add(new JSeparator());

		final TransferActionListener transferActionListener = new TransferActionListener();
		final ActionListener copyActionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!MainPanel.this.animationModeState) {
					transferActionListener.actionPerformed(e);
				} else {
					if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
						MainPanel.this.timeSliderPanel.cut();
					} else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
						MainPanel.this.timeSliderPanel.copy();
					} else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
						MainPanel.this.timeSliderPanel.paste();
					}
				}
			}
		};
		this.cut = new JMenuItem("Cut");
		this.cut.addActionListener(copyActionListener);
		this.cut.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
		this.cut.setAccelerator(KeyStroke.getKeyStroke("control X"));
		this.editMenu.add(this.cut);

		this.copy = new JMenuItem("Copy");
		this.copy.addActionListener(copyActionListener);
		this.copy.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
		this.copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
		this.editMenu.add(this.copy);

		this.paste = new JMenuItem("Paste");
		this.paste.addActionListener(copyActionListener);
		this.paste.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
		this.paste.setAccelerator(KeyStroke.getKeyStroke("control V"));
		this.editMenu.add(this.paste);

		this.duplicateSelection = new JMenuItem("Duplicate");
		// divideVertices.setMnemonic(KeyEvent.VK_V);
		this.duplicateSelection.setAccelerator(KeyStroke.getKeyStroke("control D"));
		this.duplicateSelection.addActionListener(this.cloneAction);
		this.editMenu.add(this.duplicateSelection);

		this.editMenu.add(new JSeparator());

		this.snapVertices = new JMenuItem("Snap Vertices");
		this.snapVertices.setAccelerator(KeyStroke.getKeyStroke("control shift W"));
		this.snapVertices.addActionListener(this.snapVerticesAction);
		this.editMenu.add(this.snapVertices);

		this.snapNormals = new JMenuItem("Snap Normals");
		this.snapNormals.setAccelerator(KeyStroke.getKeyStroke("control L"));
		this.snapNormals.addActionListener(this.snapNormalsAction);
		this.editMenu.add(this.snapNormals);

		this.editMenu.add(new JSeparator());

		this.selectAll = new JMenuItem("Select All");
		this.selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));
		this.selectAll.addActionListener(this.selectAllAction);
		this.editMenu.add(this.selectAll);

		this.invertSelect = new JMenuItem("Invert Selection");
		this.invertSelect.setAccelerator(KeyStroke.getKeyStroke("control I"));
		this.invertSelect.addActionListener(this.invertSelectAction);
		this.editMenu.add(this.invertSelect);

		this.expandSelection = new JMenuItem("Expand Selection");
		this.expandSelection.setAccelerator(KeyStroke.getKeyStroke("control E"));
		this.expandSelection.addActionListener(this.expandSelectionAction);
		this.editMenu.add(this.expandSelection);

		this.editMenu.addSeparator();

		final JMenuItem deleteButton = new JMenuItem("Delete");
		deleteButton.setMnemonic(KeyEvent.VK_D);
		deleteButton.addActionListener(this.deleteAction);
		this.editMenu.add(deleteButton);

		this.editMenu.addSeparator();

		this.preferencesWindow = new JMenuItem("Preferences Window");
		this.preferencesWindow.setMnemonic(KeyEvent.VK_P);
		this.preferencesWindow.addActionListener(this.openPreferencesAction);
		this.editMenu.add(this.preferencesWindow);

		for (int i = 0; i < this.menuBar.getMenuCount(); i++) {
			this.menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
		}
		return this.menuBar;
	}

	private void createTeamColorMenuItems() {
		for (int i = 0; i < 25; i++) {
			final String colorNumber = String.format("%2s", Integer.toString(i)).replace(' ', '0');
			try {
				final String colorName = WEString.getString("WESTRING_UNITCOLOR_" + colorNumber);
				final JMenuItem menuItem = new JMenuItem(colorName, new ImageIcon(BLPHandler.get()
						.getGameTex("ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp")));
				this.teamColorMenu.add(menuItem);
				final int teamColorValueNumber = i;
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						Material.teamColor = teamColorValueNumber;
						final ModelPanel modelPanel = currentModelPanel();
						if (modelPanel != null) {
							modelPanel.getAnimationViewer().reloadAllTextures();
							modelPanel.getPerspArea().reloadAllTextures();

							reloadComponentBrowser(modelPanel);
						}
						MainPanel.this.profile.getPreferences().setTeamColor(teamColorValueNumber);
					}
				});
			} catch (final Exception ex) {
				// load failed
				break;
			}
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		// Open, off of the file menu:
		refreshUndo();
		try {
			if (e.getSource() == this.newModel) {
				newModel();
			} else if (e.getSource() == this.open) {
				onClickOpen();
			} else if (e.getSource() == this.close) {
				final ModelPanel modelPanel = currentModelPanel();
				final int oldIndex = this.modelPanels.indexOf(modelPanel);
				if (modelPanel != null) {
					if (modelPanel.close(this)) {
						this.modelPanels.remove(modelPanel);
						this.windowMenu.remove(modelPanel.getMenuItem());
						if (this.modelPanels.size() > 0) {
							final int newIndex = Math.min(this.modelPanels.size() - 1, oldIndex);
							setCurrentModel(this.modelPanels.get(newIndex));
						} else {
							// TODO remove from notifiers to fix leaks
							setCurrentModel(null);
						}
					}
				}
			} else if (e.getSource() == this.fetchUnit) {
				final GameObject unitFetched = fetchUnit();
				if (unitFetched != null) {
					final String filepath = convertPathToMDX(unitFetched.getField("file"));
					if (filepath != null) {
						loadStreamMdx(MpqCodebase.get().getResourceAsStream(filepath), true, true,
								unitFetched.getScaledIcon(0.25f));
						final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
								+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
						if (this.prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
							loadStreamMdx(MpqCodebase.get().getResourceAsStream(portrait), true, false,
									unitFetched.getScaledIcon(0.25f));
						}
						this.toolsMenu.getAccessibleContext().setAccessibleDescription(
								"Allows the user to control which parts of the model are displayed for editing.");
						this.toolsMenu.setEnabled(true);
					}
				}
			} else if (e.getSource() == this.fetchModel) {
				final ModelElement model = fetchModel();
				if (model != null) {
					final String filepath = convertPathToMDX(model.getFilepath());
					if (filepath != null) {

						final ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.get()
								.getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST))
								: MDLIcon;
						loadStreamMdx(MpqCodebase.get().getResourceAsStream(filepath), true, true, icon);
						final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
								+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
						if (this.prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
							loadStreamMdx(MpqCodebase.get().getResourceAsStream(portrait), true, false, icon);
						}
						this.toolsMenu.getAccessibleContext().setAccessibleDescription(
								"Allows the user to control which parts of the model are displayed for editing.");
						this.toolsMenu.setEnabled(true);
					}
				}
			} else if (e.getSource() == this.fetchObject) {
				final MutableGameObject objectFetched = fetchObject();
				if (objectFetched != null) {
					final String filepath = convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
					if (filepath != null) {
						loadStreamMdx(MpqCodebase.get().getResourceAsStream(filepath), true, true,
								new ImageIcon(BLPHandler.get()
										.getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
										.getScaledInstance(16, 16, Image.SCALE_FAST)));
						final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
								+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
						if (this.prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
							loadStreamMdx(MpqCodebase.get().getResourceAsStream(portrait), true, false,
									new ImageIcon(BLPHandler.get()
											.getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
											.getScaledInstance(16, 16, Image.SCALE_FAST)));
						}
						this.toolsMenu.getAccessibleContext().setAccessibleDescription(
								"Allows the user to control which parts of the model are displayed for editing.");
						this.toolsMenu.setEnabled(true);
					}
				}
			} else if (e.getSource() == this.importButton) {
				this.fc.setDialogTitle("Import");
				final EditableModel current = currentMDL();
				if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
					this.fc.setCurrentDirectory(current.getFile().getParentFile());
				} else if (this.profile.getPath() != null) {
					this.fc.setCurrentDirectory(new File(this.profile.getPath()));
				}
				final int returnValue = this.fc.showOpenDialog(this);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					this.currentFile = this.fc.getSelectedFile();
					this.profile.setPath(this.currentFile.getParent());
					this.toolsMenu.getAccessibleContext().setAccessibleDescription(
							"Allows the user to control which parts of the model are displayed for editing.");
					this.toolsMenu.setEnabled(true);
					importFile(this.currentFile);
				}

				this.fc.setSelectedFile(null);

				// //Special thanks to the JWSFileChooserDemo from oracle's Java
				// tutorials, from which many ideas were borrowed for the
				// following
				// FileOpenService fos = null;
				// FileContents fileContents = null;
				//
				// try
				// {
				// fos =
				// (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
				// }
				// catch (UnavailableServiceException exc )
				// {
				//
				// }
				//
				// if( fos != null )
				// {
				// try
				// {
				// fileContents = fos.openFileDialog(null, null);
				// }
				// catch (Exception exc )
				// {
				// JOptionPane.showMessageDialog(this,"Opening command failed:
				// "+exc.getLocalizedMessage());
				// }
				// }
				//
				// if( fileContents != null)
				// {
				// try
				// {
				// fileContents.getName();
				// }
				// catch (IOException exc)
				// {
				// JOptionPane.showMessageDialog(this,"Problem opening file:
				// "+exc.getLocalizedMessage());
				// }
				// }
				refreshController();
			} else if (e.getSource() == this.importUnit) {
				final GameObject fetchUnitResult = fetchUnit();
				if (fetchUnitResult == null) {
					return;
				}
				final String filepath = convertPathToMDX(fetchUnitResult.getField("file"));
				final EditableModel current = currentMDL();
				if (filepath != null) {
					final File animationSource = MpqCodebase.get().getFile(filepath);
					importFile(animationSource);
				}
				refreshController();
			} else if (e.getSource() == this.importGameModel) {
				final ModelElement fetchModelResult = fetchModel();
				if (fetchModelResult == null) {
					return;
				}
				final String filepath = convertPathToMDX(fetchModelResult.getFilepath());
				final EditableModel current = currentMDL();
				if (filepath != null) {
					final File animationSource = MpqCodebase.get().getFile(filepath);
					importFile(animationSource);
				}
				refreshController();
			} else if (e.getSource() == this.importGameObject) {
				final MutableGameObject fetchObjectResult = fetchObject();
				if (fetchObjectResult == null) {
					return;
				}
				final String filepath = convertPathToMDX(fetchObjectResult.getFieldAsString(UnitFields.MODEL_FILE, 0));
				final EditableModel current = currentMDL();
				if (filepath != null) {
					final File animationSource = MpqCodebase.get().getFile(filepath);
					importFile(animationSource);
				}
				refreshController();
			} else if (e.getSource() == this.importFromWorkspace) {
				final List<EditableModel> optionNames = new ArrayList<>();
				for (final ModelPanel modelPanel : this.modelPanels) {
					final EditableModel model = modelPanel.getModel();
					optionNames.add(model);
				}
				final EditableModel choice = (EditableModel) JOptionPane.showInputDialog(this,
						"Choose a workspace item to import data from:", "Import from Workspace",
						JOptionPane.OK_CANCEL_OPTION, null, optionNames.toArray(), optionNames.get(0));
				if (choice != null) {
					importFile(EditableModel.deepClone(choice, choice.getHeaderName()));
				}
				refreshController();
			} else if (e.getSource() == this.importButtonS) {
				final JFrame frame = new JFrame("Animation Transferer");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setContentPane(new AnimationTransfer(frame));
				frame.setIconImage(com.matrixeater.src.MainPanel.AnimIcon.getImage());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			} else if (e.getSource() == this.mergeGeoset) {
				this.fc.setDialogTitle("Merge Single Geoset (Oinker-based)");
				final EditableModel current = currentMDL();
				if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
					this.fc.setCurrentDirectory(current.getFile().getParentFile());
				} else if (this.profile.getPath() != null) {
					this.fc.setCurrentDirectory(new File(this.profile.getPath()));
				}
				final int returnValue = this.fc.showOpenDialog(this);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					this.currentFile = this.fc.getSelectedFile();
					final EditableModel geoSource = MdxUtils.loadEditable(this.currentFile);
					this.profile.setPath(this.currentFile.getParent());
					boolean going = true;
					Geoset host = null;
					while (going) {
						final String s = JOptionPane.showInputDialog(this,
								"Geoset into which to Import: (1 to " + current.getGeosetsSize() + ")");
						try {
							final int x = Integer.parseInt(s);
							if ((x >= 1) && (x <= current.getGeosetsSize())) {
								host = current.getGeoset(x - 1);
								going = false;
							}
						} catch (final NumberFormatException exc) {

						}
					}
					Geoset newGeoset = null;
					going = true;
					while (going) {
						final String s = JOptionPane.showInputDialog(this,
								"Geoset to Import: (1 to " + geoSource.getGeosetsSize() + ")");
						try {
							final int x = Integer.parseInt(s);
							if (x <= geoSource.getGeosetsSize()) {
								newGeoset = geoSource.getGeoset(x - 1);
								going = false;
							}
						} catch (final NumberFormatException exc) {

						}
					}
					newGeoset.updateToObjects(current);
					System.out.println("putting " + newGeoset.numUVLayers() + " into a nice " + host.numUVLayers());
					for (int i = 0; i < newGeoset.numVerteces(); i++) {
						final GeosetVertex ver = newGeoset.getVertex(i);
						host.add(ver);
						ver.setGeoset(host);// geoset = host;
						// for( int z = 0; z < host.n.numUVLayers(); z++ )
						// {
						// host.getUVLayer(z).addTVertex(newGeoset.getVertex(i).getTVertex(z));
						// }
					}
					for (int i = 0; i < newGeoset.numTriangles(); i++) {
						final Triangle tri = newGeoset.getTriangle(i);
						host.add(tri);
						tri.setGeoRef(host);
					}
				}

				this.fc.setSelectedFile(null);
			} else if (e.getSource() == this.clearRecent) {
				final int dialogResult = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to clear the Recent history?", "Confirm Clear",
						JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION) {
					SaveProfile.get().clearRecent();
					updateRecent();
				}
			} else if (e.getSource() == this.nullmodelButton) {
				nullmodelFile();
				refreshController();
			} else if ((e.getSource() == this.save) && (currentMDL() != null) && (currentMDL().getFile() != null)) {
				onClickSave();
			} else if (e.getSource() == this.saveAs) {
				if (!onClickSaveAs()) {
					return;
				}
				// } else if (e.getSource() == contextClose) {
				// if (((ModelPanel) tabbedPane.getComponentAt(contextClickedTab)).close()) {//
				// this);
				// tabbedPane.remove(contextClickedTab);
				// }
			} else if (e.getSource() == this.contextCloseAll) {
				closeAll();
			} else if (e.getSource() == this.contextCloseOthers) {
				closeOthers(this.currentModelPanel);
			} else if (e.getSource() == this.showVertexModifyControls) {
				final boolean selected = this.showVertexModifyControls.isSelected();
				this.prefs.setShowVertexModifierControls(selected);
				// SaveProfile.get().setShowViewportButtons(selected);
				for (final ModelPanel panel : this.modelPanels) {
					panel.getFrontArea().setControlsVisible(selected);
					panel.getBotArea().setControlsVisible(selected);
					panel.getSideArea().setControlsVisible(selected);
					final UVPanel uvPanel = panel.getEditUVPanel();
					if (uvPanel != null) {
						uvPanel.setControlsVisible(selected);
					}
				}
			} else if (e.getSource() == this.textureModels) {
				this.prefs.setTextureModels(this.textureModels.isSelected());
			} else if (e.getSource() == this.showNormals) {
				this.prefs.setShowNormals(this.showNormals.isSelected());
			} else if (e.getSource() == this.editUVs) {
				final ModelPanel disp = currentModelPanel();
				if (disp.getEditUVPanel() == null) {
					final UVPanel panel = new UVPanel(disp, this.prefs, this.modelStructureChangeListener);
					disp.setEditUVPanel(panel);

					panel.initViewport();
					final FloatingWindow floatingWindow = this.rootWindow.createFloatingWindow(
							new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2)), panel.getSize(),
							panel.getView());
					panel.init();
					floatingWindow.getTopLevelAncestor().setVisible(true);
				} else if (!disp.getEditUVPanel().frameVisible()) {
					final FloatingWindow floatingWindow = this.rootWindow.createFloatingWindow(
							new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2)),
							disp.getEditUVPanel().getSize(), disp.getEditUVPanel().getView());
					floatingWindow.getTopLevelAncestor().setVisible(true);
				}
			} else if (e.getSource() == this.exportTextures) {
				final DefaultListModel<Material> materials = new DefaultListModel<>();
				for (int i = 0; i < currentMDL().getMaterials().size(); i++) {
					final Material mat = currentMDL().getMaterials().get(i);
					materials.addElement(mat);
				}
				for (final ParticleEmitter2 emitter2 : currentMDL().sortedIdObjects(ParticleEmitter2.class)) {
					final Material dummyMaterial = new Material(
							new Layer("Blend", currentMDL().getTexture(emitter2.getTextureID())));
				}

				final JList<Material> materialsList = new JList<>(materials);
				materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				materialsList.setCellRenderer(new MaterialListRenderer(currentMDL()));
				JOptionPane.showMessageDialog(this, new JScrollPane(materialsList));

				if (this.exportTextureDialog.getCurrentDirectory() == null) {
					final EditableModel current = currentMDL();
					if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
						this.fc.setCurrentDirectory(current.getFile().getParentFile());
					} else if (this.profile.getPath() != null) {
						this.fc.setCurrentDirectory(new File(this.profile.getPath()));
					}
				}
				if (this.exportTextureDialog.getCurrentDirectory() == null) {
					this.exportTextureDialog.setSelectedFile(new File(this.exportTextureDialog.getCurrentDirectory()
							+ File.separator + materialsList.getSelectedValue().getName()));
				}

				final int x = this.exportTextureDialog.showSaveDialog(this);
				if (x == JFileChooser.APPROVE_OPTION) {
					final File file = this.exportTextureDialog.getSelectedFile();
					if (file != null) {
						try {
							if (file.getName().lastIndexOf('.') >= 0) {
								BufferedImage bufferedImage = materialsList.getSelectedValue()
										.getBufferedImage(currentMDL().getWrappedDataSource());
								String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
										.toUpperCase();
								if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
										|| fileExtension.equals("JPEG")) {
									JOptionPane.showMessageDialog(this,
											"Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
									bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
								}
								if (fileExtension.equals("BLP")) {
									fileExtension = "blp";
								}
								final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
								if (!write) {
									JOptionPane.showMessageDialog(this, "File type unknown or unavailable");
								}
							} else {
								JOptionPane.showMessageDialog(this, "No file type was specified");
							}
						} catch (final IOException e1) {
							ExceptionPopup.display(e1);
							e1.printStackTrace();
						} catch (final Exception e2) {
							ExceptionPopup.display(e2);
							e2.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(this, "No output file was specified");
					}
				}
			} else if (e.getSource() == this.scaleAnimations) {
				// if( disp.animpanel == null )
				// {
				// AnimationPanel panel = new UVPanel(disp);
				// disp.setUVPanel(panel);
				// panel.showFrame();
				// }
				// else if(!disp.uvpanel.frameVisible() )
				// {
				// disp.uvpanel.showFrame();
				// }
				final AnimationFrame aFrame = new AnimationFrame(currentModelPanel(), new Runnable() {
					@Override
					public void run() {
						MainPanel.this.timeSliderPanel.revalidateKeyframeDisplay();
					}
				});
				aFrame.setVisible(true);
			} else if (e.getSource() == this.linearizeAnimations) {
				final int x = JOptionPane.showConfirmDialog(this,
						"This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify animations?",
						"Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
				if (x == JOptionPane.OK_OPTION) {
					final List<AnimFlag> allAnimFlags = currentMDL().getAllAnimFlags();
					for (final AnimFlag flag : allAnimFlags) {
						flag.linearize();
					}
				}
			} else if (e.getSource() == this.duplicateSelection) {
				// final int x = JOptionPane.showConfirmDialog(this,
				// "This is an irreversible process that will split selected
				// vertices into many copies of themself, one for each face, so
				// you can wrap textures and normals in a different
				// way.\n\nContinue?",
				// "Warning"/* : Divide Vertices" */,
				// JOptionPane.OK_CANCEL_OPTION);
				// if (x == JOptionPane.OK_OPTION) {
				final ModelPanel currentModelPanel = currentModelPanel();
				if (currentModelPanel != null) {
					currentModelPanel.getUndoManager().pushAction(currentModelPanel.getModelEditorManager()
							.getModelEditor().cloneSelectedComponents(this.namePicker));
				}
				// }
			} else if (e.getSource() == this.simplifyKeyframes) {
				final int x = JOptionPane.showConfirmDialog(this,
						"This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify keyframes?",
						"Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
				if (x == JOptionPane.OK_OPTION) {
					simplifyKeyframes();
				}
			} else if (e.getSource() == this.riseFallBirth) {
				final ModelView disp = currentModelPanel().getModelViewManager();
				final EditableModel model = disp.getModel();
				final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);

				final Animation oldBirth = model.findAnimByName("birth");
				final Animation oldDeath = model.findAnimByName("death");

				Animation birth = new Animation("Birth", lastAnim.getEnd() + 300, lastAnim.getEnd() + 2300);
				Animation death = new Animation("Death", birth.getEnd() + 300, birth.getEnd() + 2300);
				final Animation stand = model.findAnimByName("stand");

				final int confirmed = JOptionPane.showConfirmDialog(this,
						"This will permanently alter model. Are you sure?", "Confirmation",
						JOptionPane.OK_CANCEL_OPTION);
				if (confirmed != JOptionPane.OK_OPTION) {
					return;
				}

				boolean wipeoutOldBirth = false;
				if (oldBirth != null) {
					final String[] choices = { "Ignore", "Delete", "Overwrite" };
					final Object x = JOptionPane.showInputDialog(this,
							"Existing birth detected. What should be done with it?", "Question",
							JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
					if (x == choices[1]) {
						wipeoutOldBirth = true;
					} else if (x == choices[2]) {
						birth = oldBirth;
					} else {
						return;
					}
				}
				boolean wipeoutOldDeath = false;
				if (oldDeath != null) {
					final String[] choices = { "Ignore", "Delete", "Overwrite" };
					final Object x = JOptionPane.showInputDialog(this,
							"Existing death detected. What should be done with it?", "Question",
							JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
					if (x == choices[1]) {
						wipeoutOldDeath = true;
					} else if (x == choices[2]) {
						death = oldDeath;
					} else {
						return;
					}
				}
				if (wipeoutOldBirth) {
					model.remove(oldBirth);
				}
				if (wipeoutOldDeath) {
					model.remove(oldDeath);
				}

				final List<IdObject> roots = new ArrayList<>();
				for (final IdObject obj : model.getIdObjects()) {
					if (obj.getParent() == null) {
						roots.add(obj);
					}
				}
				for (final AnimFlag af : model.getAllAnimFlags()) {
					af.deleteAnim(birth);
					af.deleteAnim(death);
				}
				for (final IdObject obj : roots) {
					if (obj instanceof Bone) {
						final Bone b = (Bone) obj;
						AnimFlag trans = null;
						boolean globalSeq = false;
						for (final AnimFlag af : b.getAnimFlags()) {
							if (af.getTypeId() == AnimFlag.TRANSLATION) {
								if (af.hasGlobalSeq()) {
									globalSeq = true;
								} else {
									trans = af;
								}
							}
						}
						if (globalSeq) {
							continue;
						}
						if (trans == null) {
							final List<Integer> times = new ArrayList<>();
							final List<Integer> values = new ArrayList<>();
							trans = new AnimFlag("Translation", times, values);
							trans.setInterpType(InterpolationType.LINEAR);
							b.getAnimFlags().add(trans);
						}
						trans.addEntry(birth.getStart(), new Vertex(0, 0, -300));
						trans.addEntry(birth.getEnd(), new Vertex(0, 0, 0));
						trans.addEntry(death.getStart(), new Vertex(0, 0, 0));
						trans.addEntry(death.getEnd(), new Vertex(0, 0, -300));
					}
				}

				// visibility
				for (final VisibilitySource source : model.getAllVisibilitySources()) {
					final AnimFlag dummy = new AnimFlag("dummy");
					final AnimFlag af = source.getVisibilityFlag();
					dummy.copyFrom(af);
					af.deleteAnim(birth);
					af.deleteAnim(death);
					af.copyFrom(dummy, stand.getStart(), stand.getEnd(), birth.getStart(), birth.getEnd());
					af.copyFrom(dummy, stand.getStart(), stand.getEnd(), death.getStart(), death.getEnd());
					af.setEntry(death.getEnd(), Integer.valueOf(0));
				}

				if (!birth.isNonLooping()) {
					birth.setNonLooping(true);
				}
				if (!death.isNonLooping()) {
					death.setNonLooping(true);
				}

				if (!model.contains(birth)) {
					model.add(birth);
				}
				if (!model.contains(death)) {
					model.add(death);
				}

				JOptionPane.showMessageDialog(this, "Done!");
			} else if (e.getSource() == this.animFromFile) {
				this.fc.setDialogTitle("Animation Source");
				final EditableModel current = currentMDL();
				if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
					this.fc.setCurrentDirectory(current.getFile().getParentFile());
				} else if (this.profile.getPath() != null) {
					this.fc.setCurrentDirectory(new File(this.profile.getPath()));
				}
				final int returnValue = this.fc.showOpenDialog(this);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					this.currentFile = this.fc.getSelectedFile();
					this.profile.setPath(this.currentFile.getParent());
					final EditableModel animationSourceModel = MdxUtils.loadEditable(this.currentFile);
					addSingleAnimation(current, animationSourceModel);
				}

				this.fc.setSelectedFile(null);

				refreshController();
			} else if (e.getSource() == this.animFromUnit) {
				this.fc.setDialogTitle("Animation Source");
				final GameObject fetchResult = fetchUnit();
				if (fetchResult == null) {
					return;
				}
				final String filepath = convertPathToMDX(fetchResult.getField("file"));
				final EditableModel current = currentMDL();
				if (filepath != null) {
					final EditableModel animationSource = MdxUtils.loadEditable(MpqCodebase.get().getFile(filepath));
					addSingleAnimation(current, animationSource);
				}
			} else if (e.getSource() == this.animFromModel) {
				this.fc.setDialogTitle("Animation Source");
				final ModelElement fetchResult = fetchModel();
				if (fetchResult == null) {
					return;
				}
				final String filepath = convertPathToMDX(fetchResult.getFilepath());
				final EditableModel current = currentMDL();
				if (filepath != null) {
					final EditableModel animationSource = MdxUtils.loadEditable(MpqCodebase.get().getFile(filepath));
					addSingleAnimation(current, animationSource);
				}
			} else if (e.getSource() == this.animFromObject) {
				this.fc.setDialogTitle("Animation Source");
				final MutableGameObject fetchResult = fetchObject();
				if (fetchResult == null) {
					return;
				}
				final String filepath = convertPathToMDX(fetchResult.getFieldAsString(UnitFields.MODEL_FILE, 0));
				final EditableModel current = currentMDL();
				if (filepath != null) {
					final EditableModel animationSource = MdxUtils.loadEditable(MpqCodebase.get().getFile(filepath));
					addSingleAnimation(current, animationSource);
				}
			} else if (e.getSource() == this.creditsButton) {
				final DefaultStyledDocument panel = new DefaultStyledDocument();
				final JTextPane epane = new JTextPane();
				epane.setForeground(Color.BLACK);
				epane.setBackground(Color.WHITE);
				final RTFEditorKit rtfk = new RTFEditorKit();
				try {
					rtfk.read(MainPanel.class.getResourceAsStream("credits.rtf"), panel, 0);
				} catch (final MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (final BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				epane.setDocument(panel);
				final JFrame frame = new JFrame("About");
				frame.setContentPane(new JScrollPane(epane));
				frame.setSize(650, 500);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				// JOptionPane.showMessageDialog(this,new JScrollPane(epane));
			} else if (e.getSource() == this.changelogButton) {
				final DefaultStyledDocument panel = new DefaultStyledDocument();
				final JTextPane epane = new JTextPane();
				epane.setForeground(Color.BLACK);
				epane.setBackground(Color.WHITE);
				final RTFEditorKit rtfk = new RTFEditorKit();
				try {
					rtfk.read(MainPanel.class.getResourceAsStream("changelist.rtf"), panel, 0);
				} catch (final MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (final BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				epane.setDocument(panel);
				final JFrame frame = new JFrame("Changelog");
				frame.setContentPane(new JScrollPane(epane));
				frame.setSize(650, 500);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				// JOptionPane.showMessageDialog(this,new JScrollPane(epane));
			}
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
		} catch (

		final Exception exc) {
			ExceptionPopup.display(exc);
		}
	}

	private void dataSourcesChanged() {
		for (final ModelPanel modelPanel : this.modelPanels) {
			final PerspDisplayPanel pdp = modelPanel.getPerspArea();
			pdp.reloadAllTextures();
			modelPanel.getAnimationViewer().reloadAllTextures();
		}
		this.directoryChangeNotifier.dataSourcesChanged();
	}

	private void simplifyKeyframes() {
		final EditableModel currentMDL = currentMDL();
		currentMDL.simplifyKeyframes();
	}

	private boolean onClickSaveAs() {
		final EditableModel current = currentMDL();
		return onClickSaveAs(current);
	}

	private boolean onClickSaveAs(final EditableModel current) {
		try {
			this.fc.setDialogTitle("Save as");
			if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
				this.fc.setCurrentDirectory(current.getFile().getParentFile());
				this.fc.setSelectedFile(current.getFile());
			} else if (this.profile.getPath() != null) {
				this.fc.setCurrentDirectory(new File(this.profile.getPath()));
			}
			final int returnValue = this.fc.showSaveDialog(this);
			File temp = this.fc.getSelectedFile();
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				if (temp != null) {
					final FileFilter ff = this.fc.getFileFilter();
					final String ext = ff.accept(new File("junk.mdl")) ? ".mdl" : ".mdx";
					if (ff.accept(new File("junk.obj"))) {
						throw new UnsupportedOperationException("OBJ saving has not been coded yet.");
					}
					final String name = temp.getName();
					if (name.lastIndexOf('.') != -1) {
						if (!name.substring(name.lastIndexOf('.'), name.length()).equals(ext)) {
							temp = new File(
									temp.getAbsolutePath().substring(0, temp.getAbsolutePath().lastIndexOf('.')) + ext);
						}
					} else {
						temp = new File(temp.getAbsolutePath() + ext);
					}
					this.currentFile = temp;
					if (temp.exists()) {
						final Object[] options = { "Overwrite", "Cancel" };
						final int n = JOptionPane.showOptionDialog(MainFrame.frame, "Selected file already exists.",
								"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
								options[1]);
						if (n == 1) {
							this.fc.setSelectedFile(null);
							return false;
						}
					}
					this.profile.setPath(this.currentFile.getParent());

					final MdlxModel mdlx = currentMDL().toMdlx();
					final FileOutputStream stream = new FileOutputStream(this.currentFile);

					if (ext.equals(".mdl")) {
						MdxUtils.saveMdl(mdlx, stream);
					} else {
						MdxUtils.saveMdx(mdlx, stream);
					}
					currentMDL().setFileRef(this.currentFile);
					// currentMDLDisp().resetBeenSaved();
					// TODO reset been saved
					currentModelPanel().getMenuItem().setName(this.currentFile.getName().split("\\.")[0]);
					currentModelPanel().getMenuItem().setToolTipText(this.currentFile.getPath());
				} else {
					JOptionPane.showMessageDialog(this,
							"You tried to save, but you somehow didn't select a file.\nThat is bad.");
				}
			}
			this.fc.setSelectedFile(null);
			return true;
		} catch (final Exception exc) {
			ExceptionPopup.display(exc);
		}
		refreshController();
		return false;
	}

	private void onClickSave() {
		try {
			if (currentMDL() != null) {
				MdxUtils.saveMdx(currentMDL(), currentMDL().getFile());
				this.profile.setPath(currentMDL().getFile().getParent());
				// currentMDLDisp().resetBeenSaved();
				// TODO reset been saved
			}
		} catch (final Exception exc) {
			ExceptionPopup.display(exc);
		}
		refreshController();
	}

	private void onClickOpen() {
		this.fc.setDialogTitle("Open");
		final EditableModel current = currentMDL();
		if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
			this.fc.setCurrentDirectory(current.getFile().getParentFile());
		} else if (this.profile.getPath() != null) {
			this.fc.setCurrentDirectory(new File(this.profile.getPath()));
		}

		final int returnValue = this.fc.showOpenDialog(this);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			openFile(this.fc.getSelectedFile());
		}

		this.fc.setSelectedFile(null);

		// //Special thanks to the JWSFileChooserDemo from oracle's Java
		// tutorials, from which many ideas were borrowed for the following
		// FileOpenService fos = null;
		// FileContents fileContents = null;
		//
		// try
		// {
		// fos =
		// (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
		// }
		// catch (UnavailableServiceException exc )
		// {
		//
		// }
		//
		// if( fos != null )
		// {
		// try
		// {
		// fileContents = fos.openFileDialog(null, null);
		// }
		// catch (Exception exc )
		// {
		// JOptionPane.showMessageDialog(this,"Opening command failed:
		// "+exc.getLocalizedMessage());
		// }
		// }
		//
		// if( fileContents != null)
		// {
		// try
		// {
		// fileContents.getName();
		// }
		// catch (IOException exc)
		// {
		// JOptionPane.showMessageDialog(this,"Problem opening file:
		// "+exc.getLocalizedMessage());
		// }
		// }
	}

	private void newModel() {
		final JPanel newModelPanel = new JPanel();
		newModelPanel.setLayout(new MigLayout());
		newModelPanel.add(new JLabel("Model Name: "), "cell 0 0");
		final JTextField newModelNameField = new JTextField("MrNew", 25);
		newModelPanel.add(newModelNameField, "cell 1 0");
		final JRadioButton createEmptyButton = new JRadioButton("Create Empty", true);
		newModelPanel.add(createEmptyButton, "cell 0 1");
		final JRadioButton createPlaneButton = new JRadioButton("Create Plane");
		newModelPanel.add(createPlaneButton, "cell 0 2");
		final JRadioButton createBoxButton = new JRadioButton("Create Box");
		newModelPanel.add(createBoxButton, "cell 0 3");
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(createBoxButton);
		buttonGroup.add(createPlaneButton);
		buttonGroup.add(createEmptyButton);

		final int userDialogResult = JOptionPane.showConfirmDialog(this, newModelPanel, "New Model",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (userDialogResult == JOptionPane.OK_OPTION) {
			final EditableModel mdl = new EditableModel(newModelNameField.getText());
			if (createBoxButton.isSelected()) {
				final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
				final JSpinner spinner = new JSpinner(sModel);
				final int userChoice = JOptionPane.showConfirmDialog(this, spinner, "Box: Choose Segments",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (userChoice != JOptionPane.OK_OPTION) {
					return;
				}
				ModelUtils.createBox(mdl, new Vertex(64, 64, 128), new Vertex(-64, -64, 0),
						((Number) spinner.getValue()).intValue());
			} else if (createPlaneButton.isSelected()) {
				final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
				final JSpinner spinner = new JSpinner(sModel);
				final int userChoice = JOptionPane.showConfirmDialog(this, spinner, "Plane: Choose Segments",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (userChoice != JOptionPane.OK_OPTION) {
					return;
				}
				ModelUtils.createGroundPlane(mdl, new Vertex(64, 64, 0), new Vertex(-64, -64, 0),
						((Number) spinner.getValue()).intValue());
			}
			final ModelPanel temp = new ModelPanel(this, mdl, this.prefs, MainPanel.this, this.selectionItemTypeGroup,
					this.selectionModeGroup, this.modelStructureChangeListener, this.coordDisplayListener,
					this.viewportTransferHandler, this.activeViewportWatcher, GlobalIcons.MDLIcon, false,
					this.textureExporter);
			loadModel(true, true, temp);
		}

	}

	private GameObject fetchUnit() {
		final GameObject choice = UnitOptionPane.show(this);
		if (choice != null) {

		} else {
			return null;
		}

		String filepath = choice.getField("file");

		try {
			filepath = convertPathToMDX(filepath);
			// modelDisp = new MDLDisplay(toLoad, null);
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return choice;
	}

	private String convertPathToMDX(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}

	private ModelOptionPane.ModelElement fetchModel() {
		final ModelOptionPane.ModelElement model = ModelOptionPane.showAndLogIcon(this);
		if (model == null) {
			return null;
		}
		String filepath = model.getFilepath();
		if (filepath != null) {

		} else {
			return null;
		}
		try {
			filepath = convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return model;
	}

	private MutableGameObject fetchObject() {
		final BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(getUnitData(),
				getUnitEditorSettings());
		final int x = JOptionPane.showConfirmDialog(this, selector, "Object Editor - Select Unit",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		final MutableGameObject choice = selector.getSelection();
		if ((choice == null) || (x != JOptionPane.OK_OPTION)) {
			return null;
		}

		String filepath = choice.getFieldAsString(UnitFields.MODEL_FILE, 0);

		try {
			filepath = convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return choice;
	}

	private void addSingleAnimation(final EditableModel current, final EditableModel animationSourceModel) {
		Animation choice = null;
		choice = (Animation) JOptionPane.showInputDialog(this, "Choose an animation!", "Add Animation",
				JOptionPane.QUESTION_MESSAGE, null, animationSourceModel.getAnims().toArray(),
				animationSourceModel.getAnims().get(0));
		if (choice == null) {
			JOptionPane.showMessageDialog(this, "Bad choice. No animation added.");
			return;
		}
		final Animation visibilitySource = (Animation) JOptionPane.showInputDialog(this,
				"Which animation from THIS model to copy visiblity from?", "Add Animation",
				JOptionPane.QUESTION_MESSAGE, null, current.getAnims().toArray(), current.getAnims().get(0));
		if (visibilitySource == null) {
			JOptionPane.showMessageDialog(this, "No visibility will be copied.");
		}
		final List<Animation> animationsAdded = current.addAnimationsFrom(animationSourceModel,
				Collections.singletonList(choice));
		for (final Animation anim : animationsAdded) {
			current.copyVisibility(visibilitySource, anim);
		}
		JOptionPane.showMessageDialog(this, "Added " + animationSourceModel.getName() + "'s " + choice.getName()
				+ " with " + visibilitySource.getName() + "'s visibility  OK!");
		this.modelStructureChangeListener.animationsAdded(animationsAdded);
	}

	private interface OpenViewGetter {
		View getView();
	}

	private final class OpenViewAction extends AbstractAction {
		private final OpenViewGetter openViewGetter;

		private OpenViewAction(final String name, final OpenViewGetter openViewGetter) {
			super(name);
			this.openViewGetter = openViewGetter;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final View view = this.openViewGetter.getView();
			if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
				final FloatingWindow createFloatingWindow = MainPanel.this.rootWindow
						.createFloatingWindow(MainPanel.this.rootWindow.getLocation(), new Dimension(640, 480), view);
				createFloatingWindow.getTopLevelAncestor().setVisible(true);
			}
		}
	}

	private interface ModelReference {
		EditableModel getModel();
	}

	private final class ModelStructureChangeListenerImplementation implements ModelStructureChangeListener {
		private final ModelReference modelReference;

		public ModelStructureChangeListenerImplementation(final ModelReference modelReference) {
			this.modelReference = modelReference;
		}

		public ModelStructureChangeListenerImplementation(final EditableModel model) {
			this.modelReference = new ModelReference() {
				@Override
				public EditableModel getModel() {
					return model;
				}
			};
		}

		@Override
		public void nodesRemoved(final List<IdObject> nodes) {
			// Tell program to set visibility after import
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				// display.setBeenSaved(false); // we edited the model
				// TODO notify been saved system, wherever that moves to
				for (final IdObject geoset : nodes) {
					display.getModelViewManager().makeIdObjectNotVisible(geoset);
				}
				reloadGeosetManagers(display);
			}
		}

		@Override
		public void nodesAdded(final List<IdObject> nodes) {
			// Tell program to set visibility after import
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				// display.setBeenSaved(false); // we edited the model
				// TODO notify been saved system, wherever that moves to
				for (final IdObject geoset : nodes) {
					display.getModelViewManager().makeIdObjectVisible(geoset);
				}
				reloadGeosetManagers(display);
				display.getEditorRenderModel().refreshFromEditor(MainPanel.this.animatedRenderEnvironment, IDENTITY,
						IDENTITY, IDENTITY, display.getPerspArea().getViewport());
				display.getAnimationViewer().reload();
			}
		}

		@Override
		public void geosetsRemoved(final List<Geoset> geosets) {
			// Tell program to set visibility after import
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				// display.setBeenSaved(false); // we edited the model
				// TODO notify been saved system, wherever that moves to
				for (final Geoset geoset : geosets) {
					display.getModelViewManager().makeGeosetNotEditable(geoset);
					display.getModelViewManager().makeGeosetNotVisible(geoset);
				}
				reloadGeosetManagers(display);
			}
		}

		@Override
		public void geosetsAdded(final List<Geoset> geosets) {
			// Tell program to set visibility after import
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				// display.setBeenSaved(false); // we edited the model
				// TODO notify been saved system, wherever that moves to
				for (final Geoset geoset : geosets) {
					display.getModelViewManager().makeGeosetEditable(geoset);
					// display.getModelViewManager().makeGeosetVisible(geoset);
				}
				reloadGeosetManagers(display);
			}
		}

		@Override
		public void camerasAdded(final List<Camera> cameras) {
			// Tell program to set visibility after import
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				// display.setBeenSaved(false); // we edited the model
				// TODO notify been saved system, wherever that moves to
				for (final Camera camera : cameras) {
					display.getModelViewManager().makeCameraVisible(camera);
					// display.getModelViewManager().makeGeosetVisible(geoset);
				}
				reloadGeosetManagers(display);
			}
		}

		@Override
		public void camerasRemoved(final List<Camera> cameras) {
			// Tell program to set visibility after import
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				// display.setBeenSaved(false); // we edited the model
				// TODO notify been saved system, wherever that moves to
				for (final Camera camera : cameras) {
					display.getModelViewManager().makeCameraNotVisible(camera);
					// display.getModelViewManager().makeGeosetVisible(geoset);
				}
				reloadGeosetManagers(display);
			}
		}

		@Override
		public void timelineAdded(final TimelineContainer node, final AnimFlag timeline) {

		}

		@Override
		public void keyframeAdded(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
			MainPanel.this.timeSliderPanel.revalidateKeyframeDisplay();
		}

		@Override
		public void timelineRemoved(final TimelineContainer node, final AnimFlag timeline) {

		}

		@Override
		public void keyframeRemoved(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
			MainPanel.this.timeSliderPanel.revalidateKeyframeDisplay();
		}

		@Override
		public void animationsAdded(final List<Animation> animation) {
			currentModelPanel().getAnimationViewer().reload();
			currentModelPanel().getAnimationController().reload();
			MainPanel.this.creatorPanel.reloadAnimationList();
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				reloadComponentBrowser(display);
			}
		}

		@Override
		public void animationsRemoved(final List<Animation> animation) {
			currentModelPanel().getAnimationViewer().reload();
			currentModelPanel().getAnimationController().reload();
			MainPanel.this.creatorPanel.reloadAnimationList();
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				reloadComponentBrowser(display);
			}
		}

		@Override
		public void texturesChanged() {
			final ModelPanel modelPanel = currentModelPanel();
			if (modelPanel != null) {
				modelPanel.getAnimationViewer().reloadAllTextures();
				modelPanel.getPerspArea().reloadAllTextures();
			}
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				reloadComponentBrowser(display);
			}
		}

		@Override
		public void headerChanged() {
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				reloadComponentBrowser(display);
			}
		}

		@Override
		public void animationParamsChanged(final Animation animation) {
			currentModelPanel().getAnimationViewer().reload();
			currentModelPanel().getAnimationController().reload();
			MainPanel.this.creatorPanel.reloadAnimationList();
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				reloadComponentBrowser(display);
			}
		}

		@Override
		public void globalSequenceLengthChanged(final int index, final Integer newLength) {
			currentModelPanel().getAnimationViewer().reload();
			currentModelPanel().getAnimationController().reload();
			MainPanel.this.creatorPanel.reloadAnimationList();
			final ModelPanel display = displayFor(this.modelReference.getModel());
			if (display != null) {
				reloadComponentBrowser(display);
			}
		}
	}

	private final class RepaintingModelStateListener implements ModelViewStateListener {
		private final JComponent component;

		public RepaintingModelStateListener(final JComponent component) {
			this.component = component;
		}

		@Override
		public void idObjectVisible(final IdObject bone) {
			this.component.repaint();
		}

		@Override
		public void idObjectNotVisible(final IdObject bone) {
			this.component.repaint();
		}

		@Override
		public void highlightGeoset(final Geoset geoset) {
			this.component.repaint();
		}

		@Override
		public void geosetVisible(final Geoset geoset) {
			this.component.repaint();
		}

		@Override
		public void geosetNotVisible(final Geoset geoset) {
			this.component.repaint();
		}

		@Override
		public void geosetNotEditable(final Geoset geoset) {
			this.component.repaint();
		}

		@Override
		public void geosetEditable(final Geoset geoset) {
			this.component.repaint();
		}

		@Override
		public void cameraVisible(final Camera camera) {
			this.component.repaint();
		}

		@Override
		public void cameraNotVisible(final Camera camera) {
			this.component.repaint();
		}

		@Override
		public void unhighlightGeoset(final Geoset geoset) {
			this.component.repaint();
		}

		@Override
		public void highlightNode(final IdObject node) {
			this.component.repaint();
		}

		@Override
		public void unhighlightNode(final IdObject node) {
			this.component.repaint();
		}
	}

	class RecentItem extends JMenuItem {
		public RecentItem(final String what) {
			super(what);
		}

		String filepath;
	}

	public void updateRecent() {
		final List<String> recent = SaveProfile.get().getRecent();
		for (final RecentItem recentItem : this.recentItems) {
			this.recentMenu.remove(recentItem);
		}
		this.recentItems.clear();
		for (int i = 0; i < recent.size(); i++) {
			final String fp = recent.get(recent.size() - i - 1);
			if ((this.recentItems.size() <= i) || (this.recentItems.get(i).filepath != fp)) {
				// String[] bits = recent.get(i).split("/");

				final RecentItem item = new RecentItem(new File(fp).getName());
				item.filepath = fp;
				this.recentItems.add(item);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {

						MainPanel.this.currentFile = new File(item.filepath);
						MainPanel.this.profile.setPath(MainPanel.this.currentFile.getParent());
						// frontArea.clearGeosets();
						// sideArea.clearGeosets();
						// botArea.clearGeosets();
						MainPanel.this.toolsMenu.getAccessibleContext().setAccessibleDescription(
								"Allows the user to control which parts of the model are displayed for editing.");
						MainPanel.this.toolsMenu.setEnabled(true);
						SaveProfile.get().addRecent(MainPanel.this.currentFile.getPath());
						updateRecent();
						loadFile(MainPanel.this.currentFile);
					}
				});
				this.recentMenu.add(item, this.recentMenu.getItemCount() - 2);
			}
		}
	}

	public EditableModel currentMDL() {
		if (this.currentModelPanel != null) {
			return this.currentModelPanel.getModel();
		} else {
			return null;
		}
	}

	public ModelEditorManager currentMDLDisp() {
		if (this.currentModelPanel != null) {
			return this.currentModelPanel.getModelEditorManager();
		} else {
			return null;
		}
	}

	public ModelPanel currentModelPanel() {
		return this.currentModelPanel;
	}

	/**
	 * Returns the MDLDisplay associated with a given MDL, or null if one cannot be
	 * found.
	 *
	 * @param model
	 * @return
	 */
	public ModelPanel displayFor(final EditableModel model) {
		ModelPanel output = null;
		ModelView tempDisplay;
		for (final ModelPanel modelPanel : this.modelPanels) {
			tempDisplay = modelPanel.getModelViewManager();
			if (tempDisplay.getModel() == model) {
				output = modelPanel;
				break;
			}
		}
		return output;
	}

	public void loadFile(final File f, final boolean temporary, final boolean selectNewTab, final ImageIcon icon) {
		if (f.getPath().toLowerCase().endsWith("blp")) {
			loadBLPPathAsModel(f.getName(), f.getParentFile());
			return;
		}
		if (f.getPath().toLowerCase().endsWith("png")) {
			loadBLPPathAsModel(f.getName(), f.getParentFile());
			return;
		}
		ModelPanel temp = null;
		if (f.getPath().toLowerCase().endsWith("mdx") || f.getPath().toLowerCase().endsWith("mdl")) {
			try {
				final EditableModel model = MdxUtils.loadEditable(f);
				model.setFileRef(f);
				temp = new ModelPanel(this, model, this.prefs, MainPanel.this, this.selectionItemTypeGroup,
						this.selectionModeGroup, this.modelStructureChangeListener, this.coordDisplayListener,
						this.viewportTransferHandler, this.activeViewportWatcher, icon, false, this.textureExporter);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				ExceptionPopup.display(e);
				throw new RuntimeException("Reading mdx failed");
			} catch (final IOException e) {
				e.printStackTrace();
				ExceptionPopup.display(e);
				throw new RuntimeException("Reading mdx failed");
			}
		} else if (f.getPath().toLowerCase().endsWith("obj")) {
			// final Build builder = new Build();
			// final MDLOBJBuilderInterface builder = new
			// MDLOBJBuilderInterface();
			final Build builder = new Build();
			try {
				final Parse obj = new Parse(builder, f.getPath());
				temp = new ModelPanel(this, builder.createMDL(), this.prefs, MainPanel.this,
						this.selectionItemTypeGroup, this.selectionModeGroup, this.modelStructureChangeListener,
						this.coordDisplayListener, this.viewportTransferHandler, this.activeViewportWatcher, icon,
						false, this.textureExporter);
			} catch (final FileNotFoundException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			} catch (final IOException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			}
		}
		loadModel(temporary, selectNewTab, temp);
	}

	public void loadStreamMdx(final InputStream f, final boolean temporary, final boolean selectNewTab,
			final ImageIcon icon) {
		ModelPanel temp = null;
		try {
			final EditableModel model = MdxUtils.loadEditable(f);
			model.setFileRef(null);
			temp = new ModelPanel(this, model, this.prefs, MainPanel.this, this.selectionItemTypeGroup,
					this.selectionModeGroup, this.modelStructureChangeListener, this.coordDisplayListener,
					this.viewportTransferHandler, this.activeViewportWatcher, icon, false, this.textureExporter);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
			throw new RuntimeException("Reading mdx failed");
		} catch (final IOException e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
			throw new RuntimeException("Reading mdx failed");
		}
		loadModel(temporary, selectNewTab, temp);
	}

	public void loadBLPPathAsModel(final String filepath) {
		loadBLPPathAsModel(filepath, null);
	}

	public void loadBLPPathAsModel(final String filepath, final File workingDirectory) {
		loadBLPPathAsModel(filepath, workingDirectory, 800);
	}

	public void loadBLPPathAsModel(final String filepath, final File workingDirectory, final int version) {
		final EditableModel blankTextureModel = new EditableModel(filepath.substring(filepath.lastIndexOf('\\') + 1));
		blankTextureModel.setFormatVersion(version);
		if (workingDirectory != null) {
			blankTextureModel.setFileRef(new File(workingDirectory.getPath() + "/" + filepath + ".mdl"));
		}
		final Geoset newGeoset = new Geoset();
		final Layer layer = new Layer("Blend", new Bitmap(filepath));
		layer.setUnshaded(true);
		final Material material = new Material(layer);
		newGeoset.setMaterial(material);
		final BufferedImage bufferedImage = material.getBufferedImage(blankTextureModel.getWrappedDataSource());
		final int textureWidth = bufferedImage.getWidth();
		final int textureHeight = bufferedImage.getHeight();
		final float aspectRatio = textureWidth / (float) textureHeight;

		final int displayWidth = (int) (aspectRatio > 1 ? 128 : 128 * aspectRatio);
		final int displayHeight = (int) (aspectRatio < 1 ? 128 : 128 / aspectRatio);

		final int groundOffset = aspectRatio > 1 ? (128 - displayHeight) / 2 : 0;
		final GeosetVertex upperLeft = new GeosetVertex(0, displayWidth / 2, displayHeight + groundOffset,
				new Normal(0, 0, 1));
		final TVertex upperLeftTVert = new TVertex(1, 0);
		upperLeft.addTVertex(upperLeftTVert);
		newGeoset.add(upperLeft);
		upperLeft.setGeoset(newGeoset);

		final GeosetVertex upperRight = new GeosetVertex(0, -displayWidth / 2, displayHeight + groundOffset,
				new Normal(0, 0, 1));
		newGeoset.add(upperRight);
		final TVertex upperRightTVert = new TVertex(0, 0);
		upperRight.addTVertex(upperRightTVert);
		upperRight.setGeoset(newGeoset);

		final GeosetVertex lowerLeft = new GeosetVertex(0, displayWidth / 2, groundOffset, new Normal(0, 0, 1));
		newGeoset.add(lowerLeft);
		final TVertex lowerLeftTVert = new TVertex(1, 1);
		lowerLeft.addTVertex(lowerLeftTVert);
		lowerLeft.setGeoset(newGeoset);

		final GeosetVertex lowerRight = new GeosetVertex(0, -displayWidth / 2, groundOffset, new Normal(0, 0, 1));
		newGeoset.add(lowerRight);
		final TVertex lowerRightTVert = new TVertex(0, 1);
		lowerRight.addTVertex(lowerRightTVert);
		lowerRight.setGeoset(newGeoset);

		newGeoset.add(new Triangle(upperLeft, upperRight, lowerLeft));
		newGeoset.add(new Triangle(upperRight, lowerRight, lowerLeft));
		blankTextureModel.add(newGeoset);
		blankTextureModel.add(new Animation("Stand", 0, 1000));
		blankTextureModel.doSavePreps();

		loadModel(workingDirectory == null, true,
				new ModelPanel(MainPanel.this, blankTextureModel, this.prefs, MainPanel.this,
						this.selectionItemTypeGroup, this.selectionModeGroup, this.modelStructureChangeListener,
						this.coordDisplayListener, this.viewportTransferHandler, this.activeViewportWatcher,
						GlobalIcons.orangeIcon, true, this.textureExporter));
	}

	public void loadModel(final boolean temporary, final boolean selectNewTab, final ModelPanel temp) {
		if (temporary) {
			temp.getModelViewManager().getModel().setTemp(true);
		}
		final ModelPanel modelPanel = temp;
		// temp.getRootWindow().addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseEntered(final MouseEvent e) {
		// currentModelPanel = ModelPanel;
		// geoControl.setViewportView(currentModelPanel.getModelViewManagingTree());
		// geoControl.repaint();
		// }
		// });
		final JMenuItem menuItem = new JMenuItem(temp.getModel().getName());
		menuItem.setIcon(temp.getIcon());
		this.windowMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setCurrentModel(modelPanel);
			}
		});
		temp.setJMenuItem(menuItem);
		temp.getModelViewManager().addStateListener(new RepaintingModelStateListener(MainPanel.this));
		temp.changeActivity(this.currentActivity);

		if (this.geoControl == null) {
			this.geoControl = new JScrollPane(temp.getModelViewManagingTree());
			this.viewportControllerWindowView.setComponent(this.geoControl);
			this.viewportControllerWindowView.repaint();
			this.geoControlModelData = new JScrollPane(temp.getModelComponentBrowserTree());
			this.modelDataView.setComponent(this.geoControlModelData);
			this.modelComponentView.setComponent(temp.getComponentsPanel());
			this.modelDataView.repaint();
		}
		addTabForView(temp, selectNewTab);
		this.modelPanels.add(temp);

		// tabbedPane.addTab(f.getName().split("\\.")[0], icon, temp, f.getPath());
		// if (selectNewTab) {
		// tabbedPane.setSelectedComponent(temp);
		// }
		if (temporary) {
			temp.getModelViewManager().getModel().setFileRef(null);
		}
		// }
		// }).start();
		this.toolsMenu.setEnabled(true);

		if (selectNewTab && (this.prefs.getQuickBrowse() != null) && this.prefs.getQuickBrowse()) {
			for (int i = (this.modelPanels.size() - 2); i >= 0; i--) {
				final ModelPanel openModelPanel = this.modelPanels.get(i);
				if (openModelPanel.getUndoManager().isRedoListEmpty()
						&& openModelPanel.getUndoManager().isUndoListEmpty()) {
					if (openModelPanel.close(this)) {
						this.modelPanels.remove(openModelPanel);
						this.windowMenu.remove(openModelPanel.getMenuItem());
					}
				}
			}
		}
	}

	public void addTabForView(final ModelPanel view, final boolean selectNewTab) {
		// modelTabStringViewMap.addView(view);
		// final DockingWindow previousWindow = modelTabWindow.getWindow();
		// final TabWindow tabWindow = previousWindow instanceof TabWindow ? (TabWindow)
		// previousWindow : new
		// TabWindow();
		// DockingWindow selectedWindow = null;
		// if (previousWindow == tabWindow) {
		// selectedWindow = tabWindow.getSelectedWindow();
		// }
		// if (previousWindow != null && tabWindow != previousWindow) {
		// tabWindow.addTab(previousWindow);
		// }
		// tabWindow.addTab(view);
		// if (selectedWindow != null) {
		// tabWindow.setSelectedTab(tabWindow.getChildWindowIndex(selectNewTab ? view :
		// selectedWindow));
		// }
		// modelTabWindow.setWindow(tabWindow);
		if (selectNewTab) {
			view.getMenuItem().doClick();
		}
	}

	public void setCurrentModel(final ModelPanel modelContextManager) {
		this.currentModelPanel = modelContextManager;
		if (this.currentModelPanel == null) {
			final JPanel jPanel = new JPanel();
			jPanel.add(new JLabel("..."));
			this.viewportControllerWindowView.setComponent(jPanel);
			this.geoControl = null;
			this.frontView.setComponent(new JPanel());
			this.bottomView.setComponent(new JPanel());
			this.leftView.setComponent(new JPanel());
			this.perspectiveView.setComponent(new JPanel());
			this.previewView.setComponent(new JPanel());
			this.animationControllerView.setComponent(new JPanel());
			refreshAnimationModeState();
			this.timeSliderPanel.setUndoManager(null, this.animatedRenderEnvironment);
			this.timeSliderPanel.setModelView(null);
			this.creatorPanel.setModelEditorManager(null);
			this.creatorPanel.setCurrentModel(null);
			this.creatorPanel.setUndoManager(null);
			this.modelComponentView.setComponent(new JPanel());
			this.geoControlModelData = null;
		} else {
			this.geoControl.setViewportView(this.currentModelPanel.getModelViewManagingTree());
			this.geoControl.repaint();

			this.frontView.setComponent(modelContextManager.getFrontArea());
			this.bottomView.setComponent(modelContextManager.getBotArea());
			this.leftView.setComponent(modelContextManager.getSideArea());
			this.perspectiveView.setComponent(modelContextManager.getPerspArea());
			this.previewView.setComponent(modelContextManager.getAnimationViewer());
			this.animationControllerView.setComponent(modelContextManager.getAnimationController());
			refreshAnimationModeState();
			this.timeSliderPanel.setUndoManager(this.currentModelPanel.getUndoManager(),
					this.animatedRenderEnvironment);
			this.timeSliderPanel.setModelView(this.currentModelPanel.getModelViewManager());
			this.creatorPanel.setModelEditorManager(this.currentModelPanel.getModelEditorManager());
			this.creatorPanel.setCurrentModel(this.currentModelPanel.getModelViewManager());
			this.creatorPanel.setUndoManager(this.currentModelPanel.getUndoManager());

			this.geoControlModelData.setViewportView(this.currentModelPanel.getModelComponentBrowserTree());

			this.modelComponentView.setComponent(this.currentModelPanel.getComponentsPanel());
			this.geoControlModelData.repaint();
			this.currentModelPanel.getModelComponentBrowserTree().reloadFromModelView();
		}
		this.activeViewportWatcher.viewportChanged(null);
		this.timeSliderPanel.revalidateKeyframeDisplay();
	}

	public void loadFile(final File f, final boolean temporary) {
		loadFile(f, temporary, true, MDLIcon);
	}

	public void loadFile(final File f) {
		loadFile(f, false);
	}

	public void openFile(final File f) {
		this.currentFile = f;
		this.profile.setPath(this.currentFile.getParent());
		// frontArea.clearGeosets();
		// sideArea.clearGeosets();
		// botArea.clearGeosets();
		this.toolsMenu.getAccessibleContext().setAccessibleDescription(
				"Allows the user to control which parts of the model are displayed for editing.");
		this.toolsMenu.setEnabled(true);
		SaveProfile.get().addRecent(this.currentFile.getPath());
		updateRecent();
		loadFile(this.currentFile);
	}

	public void importFile(final File f) throws IOException {
		final EditableModel currentModel = currentMDL();
		if (currentModel != null) {
			importFile(MdxUtils.loadEditable(f));
		}
	}

	public void importFile(final EditableModel model) {
		final EditableModel currentModel = currentMDL();
		if (currentModel != null) {
			this.importPanel = new ImportPanel(currentModel, model);
			this.importPanel.setCallback(new ModelStructureChangeListenerImplementation(new ModelReference() {
				private final EditableModel model = currentMDL();

				@Override
				public EditableModel getModel() {
					return this.model;
				}
			}));

		}
	}

	public String incName(final String name) {
		String output = name;

		int depth = 1;
		boolean continueLoop = true;
		while (continueLoop) {
			char c = '0';
			try {
				c = output.charAt(output.length() - depth);
			} catch (final IndexOutOfBoundsException e) {
				// c remains '0'
				continueLoop = false;
			}
			for (char n = '0'; (n < '9') && continueLoop; n++) {
				// JOptionPane.showMessageDialog(null,"checking "+c+" against
				// "+n);
				if (c == n) {
					char x = c;
					x++;
					output = output.substring(0, output.length() - depth) + x
							+ output.substring((output.length() - depth) + 1);
					continueLoop = false;
				}
			}
			if (c == '9') {
				output = output.substring(0, output.length() - depth) + 0
						+ output.substring((output.length() - depth) + 1);
			} else if (continueLoop) {
				output = output.substring(0, (output.length() - depth) + 1) + 1
						+ output.substring((output.length() - depth) + 1);
				continueLoop = false;
			}
			depth++;
		}
		if (output == null) {
			output = "name error";
		} else if (output.equals(name)) {
			output = output + "_edit";
		}

		return output;
	}

	public void nullmodelFile() {
		final EditableModel currentMDL = currentMDL();
		if (currentMDL != null) {
			final EditableModel newModel = new EditableModel();
			newModel.copyHeaders(currentMDL);
			if (newModel.getFileRef() == null) {
				newModel.setFileRef(
						new File(System.getProperty("java.io.tmpdir") + "MatrixEaterExtract/matrixeater_anonymousMDL",
								"" + (int) (Math.random() * Integer.MAX_VALUE) + ".mdl"));
			}
			while (newModel.getFile().exists()) {
				newModel.setFileRef(
						new File(currentMDL.getFile().getParent() + "/" + incName(newModel.getName()) + ".mdl"));
			}
			this.importPanel = new ImportPanel(newModel, EditableModel.deepClone(currentMDL, "CurrentModel"));

			final Thread watcher = new Thread(new Runnable() {
				@Override
				public void run() {
					while (MainPanel.this.importPanel.getParentFrame().isVisible()
							&& (!MainPanel.this.importPanel.importStarted()
									|| MainPanel.this.importPanel.importEnded())) {
						try {
							Thread.sleep(1);
						} catch (final Exception e) {
							ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
						}
					}
					// if( !importPanel.getParentFrame().isVisible() &&
					// !importPanel.importEnded() )
					// JOptionPane.showMessageDialog(null,"bad voodoo
					// "+importPanel.importSuccessful());
					// else
					// JOptionPane.showMessageDialog(null,"good voodoo
					// "+importPanel.importSuccessful());
					// if( importPanel.importSuccessful() )
					// {
					// newModel.saveFile();
					// loadFile(newModel.getFile());
					// }

					if (MainPanel.this.importPanel.importStarted()) {
						while (!MainPanel.this.importPanel.importEnded()) {
							try {
								Thread.sleep(1);
							} catch (final Exception e) {
								ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
							}
						}

						if (MainPanel.this.importPanel.importSuccessful()) {
							try {
								MdxUtils.saveMdx(newModel, newModel.getFile());
							} catch (final IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							loadFile(newModel.getFile());
						}
					}
				}
			});
			watcher.start();
		}
	}

	@Override
	public void refreshUndo() {
		this.undo.setEnabled(this.undo.funcEnabled());
		this.redo.setEnabled(this.redo.funcEnabled());
	}

	public void refreshController() {
		if (this.geoControl != null) {
			this.geoControl.repaint();
		}
		if (this.geoControlModelData != null) {
			this.geoControlModelData.repaint();
		}
	}

	// @Override
	// public void mouseEntered(final MouseEvent e) {
	// refreshUndo();
	// }

	// @Override
	// public void mouseExited(final MouseEvent e) {
	// refreshUndo();
	// }

	// @Override
	// public void mousePressed(final MouseEvent e) {
	// refreshUndo();
	// }

	// @Override
	// public void mouseReleased(final MouseEvent e) {
	// refreshUndo();
	//
	// }

	// @Override
	// public void mouseClicked(final MouseEvent e) {
	// if (e.getSource() == tabbedPane && e.getButton() == MouseEvent.BUTTON3) {
	// for (int i = 0; i < tabbedPane.getTabCount(); i++) {
	// if (tabbedPane.getBoundsAt(i).contains(e.getX(), e.getY())) {
	// contextClickedTab = i;
	// contextMenu.show(tabbedPane, e.getX(), e.getY());
	// }
	// }
	// }
	// }

	// @Override
	// public void stateChanged(final ChangeEvent e) {
	// if (((ModelPanel) tabbedPane.getSelectedComponent()) != null) {
	// geoControl.setMDLDisplay(((ModelPanel)
	// tabbedPane.getSelectedComponent()).getModelViewManagingTree());
	// } else {
	// geoControl.setMDLDisplay(null);
	// }
	// }

	public void setMouseCoordDisplay(final byte dim1, final byte dim2, final double value1, final double value2) {
		for (int i = 0; i < this.mouseCoordDisplay.length; i++) {
			this.mouseCoordDisplay[i].setText("");
		}
		this.mouseCoordDisplay[dim1].setText((float) value1 + "");
		this.mouseCoordDisplay[dim2].setText((float) value2 + "");
	}

	private class UndoMenuItem extends JMenuItem {
		public UndoMenuItem(final String text) {
			super(text);
		}

		@Override
		public String getText() {
			if (funcEnabled()) {
				return "Undo " + currentModelPanel().getUndoManager().getUndoText();// +"
																					// Ctrl+Z";
			} else {
				return "Can't undo";// +" Ctrl+Z";
			}
		}

		public boolean funcEnabled() {
			try {
				return !currentModelPanel().getUndoManager().isUndoListEmpty();
			} catch (final NullPointerException e) {
				return false;
			}
		}
	}

	private class RedoMenuItem extends JMenuItem {
		public RedoMenuItem(final String text) {
			super(text);
		}

		@Override
		public String getText() {
			if (funcEnabled()) {
				return "Redo " + currentModelPanel().getUndoManager().getRedoText();// +"
																					// Ctrl+Y";
			} else {
				return "Can't redo";// +" Ctrl+Y";
			}
		}

		public boolean funcEnabled() {
			try {
				return !currentModelPanel().getUndoManager().isRedoListEmpty();
			} catch (final NullPointerException e) {
				return false;
			}
		}
	}

	public boolean closeAll() {
		boolean success = true;
		final Iterator<ModelPanel> iterator = this.modelPanels.iterator();
		boolean closedCurrentPanel = false;
		ModelPanel lastUnclosedModelPanel = null;
		while (iterator.hasNext()) {
			final ModelPanel panel = iterator.next();
			if (success = panel.close(this)) {
				this.windowMenu.remove(panel.getMenuItem());
				iterator.remove();
				if (panel == this.currentModelPanel) {
					closedCurrentPanel = true;
				}
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (closedCurrentPanel) {
			setCurrentModel(lastUnclosedModelPanel);
		}
		return success;
	}

	public boolean closeOthers(final ModelPanel panelToKeepOpen) {
		boolean success = true;
		final Iterator<ModelPanel> iterator = this.modelPanels.iterator();
		boolean closedCurrentPanel = false;
		ModelPanel lastUnclosedModelPanel = null;
		while (iterator.hasNext()) {
			final ModelPanel panel = iterator.next();
			if (panel == panelToKeepOpen) {
				lastUnclosedModelPanel = panel;
				continue;
			}
			if (success = panel.close(this)) {
				this.windowMenu.remove(panel.getMenuItem());
				iterator.remove();
				if (panel == this.currentModelPanel) {
					closedCurrentPanel = true;
				}
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (closedCurrentPanel) {
			setCurrentModel(lastUnclosedModelPanel);
		}
		return success;
	}

	protected void repaintSelfAndChildren(final ModelPanel mpanel) {
		repaint();
		this.geoControl.repaint();
		this.geoControlModelData.repaint();
		mpanel.repaintSelfAndRelatedChildren();
	}

	private final TextureExporterImpl textureExporter = new TextureExporterImpl();

	public final class TextureExporterImpl implements TextureExporter {
		public JFileChooser getFileChooser() {
			return MainPanel.this.exportTextureDialog;
		}

		@Override
		public void showOpenDialog(final String suggestedName, final TextureExporterClickListener fileHandler,
				final Component parent) {
			if (MainPanel.this.exportTextureDialog.getCurrentDirectory() == null) {
				final EditableModel current = currentMDL();
				if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
					MainPanel.this.fc.setCurrentDirectory(current.getFile().getParentFile());
				} else if (MainPanel.this.profile.getPath() != null) {
					MainPanel.this.fc.setCurrentDirectory(new File(MainPanel.this.profile.getPath()));
				}
			}
			if (MainPanel.this.exportTextureDialog.getCurrentDirectory() == null) {
				MainPanel.this.exportTextureDialog.setSelectedFile(new File(
						MainPanel.this.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
			}
			final int showOpenDialog = MainPanel.this.exportTextureDialog.showOpenDialog(parent);
			if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
				final File file = MainPanel.this.exportTextureDialog.getSelectedFile();
				if (file != null) {
					fileHandler.onClickOK(file, MainPanel.this.exportTextureDialog.getFileFilter());
				} else {
					JOptionPane.showMessageDialog(parent, "No import file was specified");
				}
			}
		}

		@Override
		public void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
				final Component parent) {

			if (MainPanel.this.exportTextureDialog.getCurrentDirectory() == null) {
				final EditableModel current = currentMDL();
				if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
					MainPanel.this.fc.setCurrentDirectory(current.getFile().getParentFile());
				} else if (MainPanel.this.profile.getPath() != null) {
					MainPanel.this.fc.setCurrentDirectory(new File(MainPanel.this.profile.getPath()));
				}
			}
			if (MainPanel.this.exportTextureDialog.getCurrentDirectory() == null) {
				MainPanel.this.exportTextureDialog.setSelectedFile(new File(
						MainPanel.this.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
			}

			final int x = MainPanel.this.exportTextureDialog.showSaveDialog(parent);
			if (x == JFileChooser.APPROVE_OPTION) {
				final File file = MainPanel.this.exportTextureDialog.getSelectedFile();
				if (file != null) {
					try {
						if (file.getName().lastIndexOf('.') >= 0) {
							fileHandler.onClickOK(file, MainPanel.this.exportTextureDialog.getFileFilter());
						} else {
							JOptionPane.showMessageDialog(parent, "No file type was specified");
						}
					} catch (final Exception e2) {
						ExceptionPopup.display(e2);
						e2.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(parent, "No output file was specified");
				}
			}
		}

	}

	@Override
	public void save(final EditableModel model) {
		if (model.getFile() != null) {
			try {
				MdxUtils.saveMdx(model, model.getFile());
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			onClickSaveAs(model);
		}
	}

	private Component getFocusedComponent() {
		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		final Component focusedComponent = kfm.getFocusOwner();
		return focusedComponent;
	}

	private boolean focusedComponentNeedsTyping(final Component focusedComponent) {
		return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField);
	}
}
