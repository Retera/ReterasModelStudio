package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewStateListener;

import javax.swing.*;

class RepaintingModelStateListener implements ModelViewStateListener {
    private final JComponent component;

    public RepaintingModelStateListener(final JComponent component) {
        this.component = component;
    }

    @Override
    public void idObjectVisible(final IdObject bone) {
        component.repaint();
    }

    @Override
    public void idObjectNotVisible(final IdObject bone) {
        component.repaint();
    }

    @Override
    public void highlightGeoset(final Geoset geoset) {
        component.repaint();
    }

    @Override
    public void geosetVisible(final Geoset geoset) {
        component.repaint();
    }

    @Override
    public void geosetNotVisible(final Geoset geoset) {
        component.repaint();
    }

    @Override
    public void geosetNotEditable(final Geoset geoset) {
        component.repaint();
    }

    @Override
    public void geosetEditable(final Geoset geoset) {
        component.repaint();
    }

    @Override
    public void cameraVisible(final Camera camera) {
        component.repaint();
    }

    @Override
    public void cameraNotVisible(final Camera camera) {
        component.repaint();
    }

    @Override
    public void unhighlightGeoset(final Geoset geoset) {
        component.repaint();
    }

    @Override
    public void highlightNode(final IdObject node) {
        component.repaint();
    }

    @Override
    public void unhighlightNode(final IdObject node) {
        component.repaint();
    }
}
