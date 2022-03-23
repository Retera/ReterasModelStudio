package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextField[] mouseCoordDisplay = new JTextField[3];
    private final CoordDisplayListener coordDisplayListener;
    private final RootWindowUgg rootWindowUgg;

    public MainPanel(JToolBar toolBar, RootWindowUgg rootWindowUgg) {
	    super(new MigLayout("fill, ins 0, gap 0, novisualpadding, wrap 1", "[fill, grow]", "[][fill, grow]"));
	    add(toolBar);

	    TimeSliderView.createMouseCoordDisp(mouseCoordDisplay);

	    ClosePopup.createContextMenuPopup();

	    this.rootWindowUgg = rootWindowUgg;
	    add(rootWindowUgg);

	    coordDisplayListener = (coordSys, value1, value2) -> TimeSliderView.setMouseCoordDisplay(mouseCoordDisplay, coordSys, value1, value2);
    }

    public void init() {
        linkActions(getRootPane());
    }

    public void linkActions(JRootPane rootPane) {
	    KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
	    rootPane.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
	    rootPane.setActionMap(keyBindingPrefs.getActionMap());
    }

    public void repaintSelfAndChildren() {
        repaint();
    }

    public WindowHandler2 getWindowHandler2() {
        return rootWindowUgg.getWindowHandler2();
    }

}
