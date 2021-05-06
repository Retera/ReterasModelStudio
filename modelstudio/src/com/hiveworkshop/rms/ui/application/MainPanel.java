package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
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
import java.util.Enumeration;
import java.util.List;

public class MainPanel extends JPanel implements ModelEditorChangeActivityListener {
    UndoHandler undoHandler;

    List<ModelPanel> modelPanels;
    ModelPanel currentModelPanel;
    View frontView, leftView, bottomView, perspectiveView;
    final View timeSliderView;
    final View previewView;
    final View creatorView;
    final View animationControllerView;

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

    AbstractAction expandSelectionAction = MainPanelLinkActions.getExpandSelectionAction(this);
    AbstractAction selectAllAction = MainPanelLinkActions.getSelectAllAction(this);
    AbstractAction invertSelectAction = MainPanelLinkActions.getInvertSelectAction(this);
    AbstractAction rigAction = MainPanelLinkActions.getRigAction(this);
    AbstractAction cloneAction = MainPanelLinkActions.getCloneAction(this);
    AbstractAction deleteAction = MainPanelLinkActions.getDeleteAction(this);

    TimeSliderPanel timeSliderPanel;

    boolean animationModeState = false;

    final ViewportListener viewportListener = new ViewportListener();

    ClonedNodeNamePicker namePicker = new ClonedNodeNamePicker(this);

    public MainPanel() {
        super();
        undoHandler = new UndoHandler(this);
        setLayout(new MigLayout("fill, ins 0, gap 0, novisualpadding, wrap 1", "[fill, grow]", "[][fill, grow]"));
        add(ToolBar.createJToolBar(this));

        final JLabel[] divider = new JLabel[3];
        for (int i = 0; i < divider.length; i++) {
            divider[i] = new JLabel("----------");
        }


        TimeSliderView.createMouseCoordDisp(mouseCoordDisplay);

        modelStructureChangeListener = ModelStructureChangeListener.getModelStructureChangeListener(this);
        animatedRenderEnvironment = new TimeEnvironmentImpl(0, 1);

        TimeSliderView.createTimeSliderPanel(this);

        animatedRenderEnvironment.addChangeListener((start, end) -> MainPanelLinkActions.animatedRenderEnvChangeResult(MainPanel.this, start, end));


        ClosePopup.createContextMenuPopup(this);

        modelPanels = new ArrayList<>();

        viewMap = new StringViewMap();

        rootWindow = new RootWindow(viewMap);
        final Runnable fixit = () -> {
            WindowHandler.traverseAndReset(rootWindow);
//            WindowHandler.traverseAndReset(rootWindow, new Vec3(.3,.3,.3));
            WindowHandler.traverseAndFix(rootWindow);
        };
        rootWindow.addListener(WindowHandler.getDockingWindowListener(this));
        rootWindow.addListener(WindowHandler.getDockingWindowListener2(fixit));
        setRootProps(rootWindow);


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

//        UIManager.put("TabbedPane.contentBorderInsets", new Insets(-2, -2, -1, -1));
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(-2, -2, -1, -1));
//        //			UIManager.put("TabbedPane.border", null);
////			UIManager.put("TabbedPane.borderWidth", 0);
////			UIManager.put("TabbedPane.labelShift", 20);
////			UIManager.put("InternalFrame.borderWidth", 0);
////			UIManager.put("TabbedPane.tabRunOverlay", 4);
////			UIManager.put("InternalFrame.border", null);
////			UIManager.put("Desktop.minOnScreenInsets", new Insets(0, 0, 0, 0));
////			UIManager.put("TabbedPane.tabInsets", new Insets(0, 0, 20, 0));
////			UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 20, 0));
////			UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(8, 0, 5, 0));
//////			UIManager.put("Table.highlight", Color.magenta);
//        UIManager.put("controlDkShadow", Color.magenta.brighter());
//
//			UIManager.put("SplitPane.highlight", Color.magenta);
//			UIManager.put("TabbedPane.tabAreaBackground", Color.magenta);
//			UIManager.put("Button.highlight", Color.magenta);
//			UIManager.put("TabbedPane.highlight", Color.green);
//			UIManager.put("TabbedPane.border", Color.magenta);
//			UIManager.put("SplitPane.darkShadow", Color.magenta);
//			UIManager.put("ToolBar.darkShadow", Color.magenta);
//			UIManager.put("InternalFrame.borderDarkShadow", Color.magenta);
//			UIManager.put("Separator.highlight", Color.blue.brighter().brighter());
//			UIManager.put("Slider.focus", Color.yellow);
//			UIManager.put("controlLtHighlight", Color.yellow);
//			UIManager.put("controlHighlight", Color.yellow);
//			UIManager.put("ToolBar.highlight", Color.yellow);
//			UIManager.put("Separator.shadow", Color.yellow.darker());
//			UIManager.put("SplitPaneDivider.draggingColor", Color.yellow.darker());
//			UIManager.put("SplitPane.dividerFocusColor", Color.ORANGE.darker());
//			UIManager.put("Slider.shadow", Color.ORANGE.darker());
//			UIManager.put("MenuBar.shadow", Color.magenta.brighter());
//        UIManager.put("SplitPane.shadow", Color.blue.brighter());
//        UIManager.put("Panel.background", Color.pink);
//        UIManager.put("EditorPane.inactiveBackground", Color.blue.brighter());
//        UIManager.put("ToolBar.shadow", Color.pink);
//        UIManager.put("controlShadow", Color.yellow);
//        UIManager.put("InternalFrame.borderShadow", Color.yellow);
//        UIManager.put("InternalFrame.borderColor ", Color.ORANGE.darker());
//        UIManager.put("InternalFrame.activeTitleBackground", Color.yellow);
//			UIManager.put("Separator.background", Color.red);
//			UIManager.put("ScrollBar.thumbHighlight", Color.red);
//			UIManager.put("MenuBar.highlight", Color.cyan.brighter().brighter());
//			UIManager.put("InternalFrame.borderHighlight", Color.cyan);
//			UIManager.put("EditorPane.disabledBackground", Color.cyan);
        UIManager.put("TabbedPane.darkShadow", Color.green);
//			UIManager.put("TabbedPane.background", Color.cyan.brighter());
//			UIManager.put("SplitPane.background", Color.green);
//			UIManager.put("TabbedPane.shadow", Color.green.darker());
//			UIManager.put("Panel.alterBackground", Color.green.darker());
//
//			UIManager.put("window", Color.red.darker().darker());
//			UIManager.put("RootPane.background", Color.red.darker().darker());
//			UIManager.put("Panel.lightBackground", Color.red.darker().darker());
//			UIManager.put("control", Color.red.darker().darker());
////			UIManager.put("TabbedPane.tabsOverlapBorder", false);
////			UIManager.put("TabbedPane.border", null);
////			UIManager.put("TabbedPane.shadow", false);
////			UIManager.put("TabbedPane.contentBorder", null);


        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setSizePolicy(TitledTabSizePolicy.EQUAL_SIZE);
        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setBorderSizePolicy(TitledTabBorderSizePolicy.EQUAL_SIZE);

        rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties().setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
        rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().setShadowEnabled(false);
        rootWindow.getRootWindowProperties().getWindowAreaProperties().getInsets().set(0, 0, 0, 0);
        rootWindow.getRootWindowProperties().getSplitWindowProperties().setDividerSize(2);
//        rootWindow.getRootWindowProperties().getDockingWindowProperties().getTabProperties().getTitledTabProperties().setHighlightedRaised(0);
        rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
        rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(true);

        rootWindow.setBackground(Color.GREEN.darker());
        rootWindow.setForeground(Color.GREEN.darker());
//    getjTable(UIManager.getDefaults());
    }

    private static JTable getjTable(UIDefaults defaults) {
        System.out.println(defaults.size() + " properties defined !");
        String[] colName = {"Key", "Value"};
        String[][] rowData = new String[defaults.size()][2];
        int i = 0;
        for (Enumeration e = defaults.keys(); e.hasMoreElements(); i++) {
            Object key = e.nextElement();
            rowData[i][0] = key.toString();
            rowData[i][1] = "" + defaults.get(key);
            System.out.println(rowData[i][0] + " ,, " + rowData[i][1]);
        }
        JTable t = new JTable(rowData, colName);
        return t;
    }

    public UndoHandler getUndoHandler() {
        return undoHandler;
    }

    public CreatorModelingPanel getCreatorPanel() {
        return creatorPanel;
    }

    public List<ModelPanel> getModelPanels() {
        return modelPanels;
    }

//    public JScrollPane getmEditingTP() {
//        return mEditingTP;
//    }
//
//    public JScrollPane getCompBrowserTP() {
//        return compBrowserTP;
//    }

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


    public static void repaintSelfAndChildren(MainPanel mainPanel) {
        mainPanel.repaint();
//        mainPanel.mEditingTP.repaint();
//        mainPanel.compBrowserTP.repaint();
    }
}
