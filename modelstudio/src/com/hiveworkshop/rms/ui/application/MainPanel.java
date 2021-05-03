package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ClonedNodeNamePickerImplementation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.RedoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.UndoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.CreatorModelingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.StringViewMap;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabBorderSizePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainPanel extends JPanel implements UndoHandler, ModelEditorChangeActivityListener {
    MenuBar.UndoMenuItem undo;
    MenuBar.RedoMenuItem redo;

    List<ModelPanel> modelPanels;
    ModelPanel currentModelPanel;
    View frontView, leftView, bottomView, perspectiveView;
    final View timeSliderView;
    final View previewView;
    final View creatorView;
    final View animationControllerView;

    JScrollPane geoControl;
    JScrollPane geoControlModelData;
    JTextField[] mouseCoordDisplay = new JTextField[3];
    boolean cheatShift = false;
    boolean cheatAlt = false;
    SaveProfile profile = SaveProfile.get();
    ProgramPreferences prefs = profile.getPreferences();

    final CreatorModelingPanel creatorPanel;
    final TimeEnvironmentImpl animatedRenderEnvironment;
    final CoordDisplayListener coordDisplayListener;
    final ModelStructureChangeListener modelStructureChangeListener;
    final ViewportTransferHandler viewportTransferHandler;
    final StringViewMap viewMap;
    final RootWindow rootWindow;

    public ModelEditorActionType actionType;
//    JMenu teamColorMenu;
    JButton snapButton;
    ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeGroup;
    ToolbarButtonGroup<SelectionMode> selectionModeGroup;
    ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup;
    View viewportControllerWindowView;
    View toolView;
    View modelDataView;
    View modelComponentView;
    ActivityDescriptor currentActivity;

    public AbstractAction undoAction = new UndoActionImplementation("Undo", this);
    public AbstractAction redoAction = new RedoActionImplementation("Redo", this);
    AbstractAction expandSelectionAction = MainPanelLinkActions.getExpandSelectionAction(this);
    AbstractAction selectAllAction = MainPanelLinkActions.getSelectAllAction(this);
    AbstractAction invertSelectAction = MainPanelLinkActions.getInvertSelectAction(this);
    AbstractAction rigAction = MainPanelLinkActions.getRigAction(this);
    AbstractAction cloneAction = MainPanelLinkActions.getCloneAction(this);
    AbstractAction deleteAction = MainPanelLinkActions.getDeleteAction(this);

    TimeSliderPanel timeSliderPanel;

    boolean animationModeState = false;
    final ZoomableImagePreviewPanel blpPanel;

    final ViewportListener viewportListener = new ViewportListener();

    ClonedNodeNamePicker namePicker = new ClonedNodeNamePickerImplementation(this);

    public MainPanel() {
        super();
        setLayout(new MigLayout("fill, ins 0, gap 0, novisualpadding, wrap 1", "[fill, grow]", "[][fill, grow]"));
        add(ToolBar.createJToolBar(this));

        final JLabel[] divider = new JLabel[3];
        for (int i = 0; i < divider.length; i++) {
            divider[i] = new JLabel("----------");
        }

        TimeSliderView.createMouseCoordDisp(mouseCoordDisplay);

        modelStructureChangeListener = ModelStructureChangeListener.getModelStructureChangeListener(this);
        animatedRenderEnvironment = new TimeEnvironmentImpl(0, 1);
        blpPanel = new ZoomableImagePreviewPanel(null);

        TimeSliderView.createTimeSliderPanel(this);

        animatedRenderEnvironment.addChangeListener((start, end) -> MainPanelLinkActions.animatedRenderEnvChangeResult(MainPanel.this, start, end));


        ClosePopup.createContextMenuPopup(this);

        modelPanels = new ArrayList<>();

        viewMap = new StringViewMap();

        rootWindow = new RootWindow(viewMap);
        final Runnable fixit = () -> {
            WindowHandler.traverseAndReset(rootWindow);
            WindowHandler.traverseAndFix(rootWindow);
        };
        rootWindow.addListener(WindowHandler.getDockingWindowListener(this));
        setRootProps(rootWindow);
        rootWindow.addListener(WindowHandler.getDockingWindowListener2(fixit));


        JPanel contentsDummy = new JPanel();
        contentsDummy.add(new JLabel("..."));
        modelDataView = new View("Contents", null, contentsDummy);
        modelComponentView = new View("Component", null, new JPanel());


        previewView = new View("Preview", null, new JPanel());

        timeSliderView = TimeSliderView.createTimeSliderView(timeSliderPanel);

        creatorPanel = new CreatorModelingPanel(newType -> {
            actionTypeGroup.maybeSetButtonType(newType);
            changeActivity(newType);
        }, prefs, actionTypeGroup, viewportListener, animatedRenderEnvironment);

        creatorView = new View("Modeling", null, creatorPanel);


        animationControllerView = new View("Animation Controller", null, new JPanel());

        final TabWindow startupTabWindow = MainLayoutCreator.createMainLayout(this);
        rootWindow.setWindow(startupTabWindow);
        startupTabWindow.setSelectedTab(0);

        add(rootWindow);

        selectionItemTypeGroup.addToolbarButtonListener(this::selectionItemTypeGroupActionRes);

        actionTypeGroup.addToolbarButtonListener(newType -> MainPanelLinkActions.actionTypeGroupActionRes(MainPanel.this, newType));
        actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);

        viewportTransferHandler = new ViewportTransferHandler();
        coordDisplayListener = (dim1, dim2, value1, value2) -> TimeSliderView.setMouseCoordDisplay(mouseCoordDisplay, dim1, dim2, value1, value2);
    }

    private static void setRootProps(RootWindow rootWindow) {
        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setSizePolicy(TitledTabSizePolicy.EQUAL_SIZE);
        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setBorderSizePolicy(TitledTabBorderSizePolicy.EQUAL_SIZE);

        rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties().setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
        rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().setShadowEnabled(false);
        rootWindow.getRootWindowProperties().getWindowAreaProperties().getInsets().set(0, 0, 0, 0);
        rootWindow.getRootWindowProperties().getSplitWindowProperties().setDividerSize(1);
        rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
        rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(true);

        rootWindow.setBackground(Color.GREEN);
        rootWindow.setForeground(Color.GREEN);
    }

    public CreatorModelingPanel getCreatorPanel() {
        return creatorPanel;
    }

    public List<ModelPanel> getModelPanels() {
        return modelPanels;
    }

    public JScrollPane getGeoControl() {
        return geoControl;
    }

    public JScrollPane getGeoControlModelData() {
        return geoControlModelData;
    }

    public TimeEnvironmentImpl getAnimatedRenderEnvironment() {
        return animatedRenderEnvironment;
    }

    public TimeSliderPanel getTimeSliderPanel() {
        return timeSliderPanel;
    }

    public ModelStructureChangeListener getModelStructureChangeListener() {
        return modelStructureChangeListener;
    }

    public ProgramPreferences getPrefs() {
        return prefs;
    }

    public RootWindow getRootWindow() {
        return rootWindow;
    }

    public ModelPanel currentModelPanel() {
        return currentModelPanel;
    }

    public EditableModel currentMDL() {
        if (currentModelPanel != null) {
            return currentModelPanel.getModel();
        } else {
            return null;
        }
    }

    @Override
    public JRootPane getRootPane() {
        return super.getRootPane();
    }

    private void selectionItemTypeGroupActionRes(SelectionItemTypes newType) {
        animationModeState = newType == SelectionItemTypes.ANIMATE;
        // we need to refresh the state of stuff AFTER the ModelPanels, this is a pretty signficant design flaw,
        // so we're just going to post to the EDT to get behind them (they're called on the same notifier as this method)
        SwingUtilities.invokeLater(() -> ModelLoader.refreshAnimationModeState(MainPanel.this));

        if (newType == SelectionItemTypes.TPOSE) {

            final Object[] settings = {"Move Linked", "Move Single"};
            final Object dialogResult = JOptionPane.showInputDialog(null, "Choose settings:", "T-Pose Settings",
                    JOptionPane.PLAIN_MESSAGE, null, settings, settings[0]);
            ModelEditorManager.MOVE_LINKED = dialogResult == settings[0];
        }
        repaint();
    }

    @Override
    public void changeActivity(ActivityDescriptor newType) {
        currentActivity = newType;
        for (ModelPanel modelPanel : modelPanels) {
            modelPanel.changeActivity(newType);
        }
        creatorPanel.changeActivity(newType);
    }

    public void init() {
        final JRootPane root = getRootPane();
        MainPanelLinkActions.linkActions(this, root);

    }

    @Override
    public void refreshUndo() {
        undo.setEnabled(undo.funcEnabled());
        redo.setEnabled(redo.funcEnabled());
    }


    public static void repaintSelfAndChildren(MainPanel mainPanel) {
        mainPanel.repaint();
        mainPanel.geoControl.repaint();
        mainPanel.geoControlModelData.repaint();
    }
}
