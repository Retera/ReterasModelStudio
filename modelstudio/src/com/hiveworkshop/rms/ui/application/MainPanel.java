package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

//public class MainPanel extends JPanel implements ModelEditorChangeActivityListener {
public class MainPanel extends JPanel {
    MainPanelLinkActions mainPanelLinkActions;

    JTextField[] mouseCoordDisplay = new JTextField[3];

    final CoordDisplayListener coordDisplayListener;
    final ViewportTransferHandler viewportTransferHandler;
    final RootWindowUgg rootWindowUgg;

    public MainPanel(JToolBar toolBar, RootWindowUgg rootWindowUgg) {
        super(new MigLayout("fill, ins 0, gap 0, novisualpadding, wrap 1", "[fill, grow]", "[][fill, grow]"));
        add(toolBar);

        mainPanelLinkActions = new MainPanelLinkActions();


        TimeSliderView.createMouseCoordDisp(mouseCoordDisplay);

        ClosePopup.createContextMenuPopup();

        this.rootWindowUgg = rootWindowUgg;
        add(rootWindowUgg);

        viewportTransferHandler = new ViewportTransferHandler();
        coordDisplayListener = (coordSys, value1, value2) -> TimeSliderView.setMouseCoordDisplay(mouseCoordDisplay, coordSys, value1, value2);
    }

    public ViewportListener getViewportListener() {
        return rootWindowUgg.getWindowHandler2().getViewportListener();
    }

    public void selectionItemTypeGroupActionRes(SelectionItemTypes newType) {
//        animationModeState = newType == SelectionItemTypes.ANIMATE;
        // we need to refresh the state of stuff AFTER the ModelPanels, this is a pretty signficant design flaw,
        // so we're just going to post to the EDT to get behind them (they're called on the same notifier as this method)
        SwingUtilities.invokeLater(() -> ModelLoader.refreshAnimationModeState());

        if (newType == SelectionItemTypes.TPOSE) {

            final Object[] settings = {"Move Linked", "Move Single"};
            final Object dialogResult = JOptionPane.showInputDialog(null, "Choose settings:", "T-Pose Settings",
                    JOptionPane.PLAIN_MESSAGE, null, settings, settings[0]);
            ModelEditorManager.MOVE_LINKED = dialogResult == settings[0];
        }
        repaint();
    }

//    @Override
//    public void changeActivity(ModelEditorActionType3 newType) {
////        actionTypeGroup.setActiveButton(newType);
//        for (ModelPanel modelPanel : ProgramGlobals.getModelPanels()) {
//            modelPanel.changeActivity(newType);
//        }
//    }

    public void init() {
        linkActions(getRootPane());
    }

    public void linkActions(JRootPane rootPane) {
//        mainPanelLinkActions.linkActions(rootPane);
        mainPanelLinkActions.linkActions2(rootPane);
    }

    public MainPanelLinkActions getMainPanelLinkActions() {
        return mainPanelLinkActions;
    }

    public void repaintSelfAndChildren() {
        repaint();
    }

    public WindowHandler2 getWindowHandler2() {
        return rootWindowUgg.getWindowHandler2();
    }

    public CoordDisplayListener getCoordDisplayListener() {
        return coordDisplayListener;
    }

    public ViewportTransferHandler getViewportTransferHandler() {
        return viewportTransferHandler;
    }
}
