package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.NoSuchElementException;

public final class RedoActionImplementation extends AbstractAction {
    private final MainPanel mainPanel;

    public RedoActionImplementation(final String name, final MainPanel mainPanel) {
        super(name);
        this.mainPanel = mainPanel;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final ModelPanel mpanel = mainPanel.currentModelPanel();
        if (mpanel != null) {
            try {
                mpanel.getUndoManager().redo();
            } catch (final NoSuchElementException exc) {
                JOptionPane.showMessageDialog(mainPanel, "Nothing to redo!");
            } catch (final Exception exc) {
                ExceptionPopup.display(exc);
            }
        }
        mainPanel.getUndoHandler().refreshUndo();
        MainPanel.repaintSelfAndChildren(mainPanel);
        mpanel.repaintSelfAndRelatedChildren();
    }
}