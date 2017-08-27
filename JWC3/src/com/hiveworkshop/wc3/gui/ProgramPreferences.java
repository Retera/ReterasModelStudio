package com.hiveworkshop.wc3.gui;

import java.awt.Color;
import java.io.Serializable;

import com.hiveworkshop.wc3.user.SaveProfile;

public class ProgramPreferences implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private int viewMode = 1;
	private boolean showNormals;
	private boolean showVertexModifierControls = true;
	private boolean textureModels = true;
	private boolean useNativeMDXParser = true;
	private boolean loadPortraits = true;
	private transient boolean cloneOn = false;
	private transient boolean[] dimLocks = new boolean[3];
	private Boolean invertedDisplay = true;
	Color activeRColor1 = new Color(200, 255, 200);
	Color activeRColor2 = new Color(60, 170, 0);
	Color activeColor1 = new Color(255, 200, 200);
	Color activeColor2 = new Color(170, 60, 0);
	Color activeBColor1 = new Color(200, 200, 255);
	Color activeBColor2 = new Color(0, 60, 170);
	transient int selectionType = 0;
	transient int actionType = 3;

	Color vertexColor = new Color(0, 0, 255);// new Color(0, 0, 0)
	Color triangleColor = new Color(255, 255, 255);// new Color(190, 190, 190)
	Color visibleUneditableColor = new Color(150, 150, 255);
	Color highlighTriangleColor = new Color(255, 255, 0);
	Color highlighVertexColor = new Color(0, 255, 0);
	Color normalsColor = new Color(128, 128, 255);
	Color pivotPointsSelectedColor = Color.RED.darker();
	Color pivotPointsColor = Color.MAGENTA;
	Color lightsColor = Color.YELLOW.brighter();
	Color ambientLightColor = Color.CYAN.brighter();
	Color selectColor = Color.RED;
	private int vertexSize = 3;

	public void reload() {
		dimLocks = new boolean[3];
		actionType = 3;
		if (invertedDisplay == null) {
			invertedDisplay = true;
		}
		if (vertexColor == null || normalsColor == null || pivotPointsColor == null) {
			vertexColor = new Color(0, 0, 255);// new Color(0, 0, 0)
			triangleColor = new Color(255, 255, 255);// new Color(190, 190, 190)
			visibleUneditableColor = new Color(150, 150, 255);
			highlighTriangleColor = new Color(255, 255, 0);
			highlighVertexColor = new Color(0, 255, 0);
			normalsColor = new Color(128, 128, 255);
			pivotPointsColor = Color.magenta;
			lightsColor = Color.YELLOW.brighter();
			ambientLightColor = Color.CYAN.brighter();
		}
		if (pivotPointsSelectedColor == null) {
			pivotPointsSelectedColor = Color.RED.darker();
		}
		if (selectColor == null) {
			selectColor = Color.RED;
		}
		if (vertexSize == 0) {
			vertexSize = 3;
		}
	}

	public int getVertexSize() {
		return vertexSize;
	}

	public void setVertexSize(final int vertexSize) {
		this.vertexSize = vertexSize;
	}

	public Color getSelectColor() {
		return selectColor;
	}

	public void setSelectColor(final Color selectColor) {
		this.selectColor = selectColor;
	}

	public int viewMode() {
		return viewMode;
	}

	public boolean showNormals() {
		return showNormals;
	}

	public boolean isCloneOn() {
		return cloneOn;
	}

	public void setCloneOn(final boolean cloneOn) {
		this.cloneOn = cloneOn;
	}

	public void setDimLock(final int x, final boolean flag) {
		dimLocks[x] = flag;
		// if( dimLocks[x] )
		// {
		// getDLockButton(x).setColors(activeBColor1,activeBColor2);
		// }
		// else
		// {
		// getDLockButton(x).resetColors();
		// }
	}

	public boolean getDimLock(final int dim) {
		return dimLocks[dim];
	}

	public boolean showVMControls() {
		return showVertexModifierControls;
	}

	public boolean textureModels() {
		return textureModels;
	}

	public int currentSelectionType() {
		return selectionType;
	}

	public int currentActionType() {
		return actionType;
	}

	public void setViewMode(final int viewMode) {
		this.viewMode = viewMode;
		SaveProfile.save();
	}

	public void setShowNormals(final boolean showNormals) {
		this.showNormals = showNormals;
		SaveProfile.save();
	}

	public void setShowVertexModifierControls(final boolean showVertexModifierControls) {
		this.showVertexModifierControls = showVertexModifierControls;
		SaveProfile.save();
	}

	public void setTextureModels(final boolean textureModels) {
		this.textureModels = textureModels;
		SaveProfile.save();
	}

	public void setDimLocks(final boolean[] dimLocks) {
		this.dimLocks = dimLocks;
		SaveProfile.save();
	}

	public void setActiveRColor1(final Color activeRColor1) {
		this.activeRColor1 = activeRColor1;
		SaveProfile.save();
	}

	public void setActiveRColor2(final Color activeRColor2) {
		this.activeRColor2 = activeRColor2;
		SaveProfile.save();
	}

	public void setActiveColor1(final Color activeColor1) {
		this.activeColor1 = activeColor1;
		SaveProfile.save();
	}

	public void setActiveColor2(final Color activeColor2) {
		this.activeColor2 = activeColor2;
		SaveProfile.save();
	}

	public void setActiveBColor1(final Color activeBColor1) {
		this.activeBColor1 = activeBColor1;
		SaveProfile.save();
	}

	public void setActiveBColor2(final Color activeBColor2) {
		this.activeBColor2 = activeBColor2;
		SaveProfile.save();
	}

	public void setSelectionType(final int selectionType) {
		this.selectionType = selectionType;
		SaveProfile.save();
	}

	public void setActionType(final int actionType) {
		this.actionType = actionType;
		SaveProfile.save();
	}

	public Color getActiveRColor1() {
		return activeRColor1;
	}

	public Color getActiveRColor2() {
		return activeRColor2;
	}

	public Color getActiveColor1() {
		return activeColor1;
	}

	public Color getActiveColor2() {
		return activeColor2;
	}

	public Color getActiveBColor1() {
		return activeBColor1;
	}

	public Color getActiveBColor2() {
		return activeBColor2;
	}

	public int getActionType() {
		return actionType;
	}

	public int getViewMode() {
		return viewMode;
	}

	public boolean isShowNormals() {
		return showNormals;
	}

	public boolean isShowVertexModifierControls() {
		return showVertexModifierControls;
	}

	public boolean isTextureModels() {
		return textureModels;
	}

	public int getSelectionType() {
		return selectionType;
	}

	public boolean isUseNativeMDXParser() {
		return useNativeMDXParser;
	}

	public void setUseNativeMDXParser(final boolean useNativeMDXParser) {
		this.useNativeMDXParser = useNativeMDXParser;
		SaveProfile.save();
	}

	public boolean isLoadPortraits() {
		return loadPortraits;
	}

	public void setLoadPortraits(final boolean loadPortraits) {
		this.loadPortraits = loadPortraits;
		SaveProfile.save();
	}

	public Color getVertexColor() {
		return vertexColor;
	}

	public void setVertexColor(final Color vertexColor) {
		this.vertexColor = vertexColor;
	}

	public Color getTriangleColor() {
		return triangleColor;
	}

	public void setTriangleColor(final Color triangleColor) {
		this.triangleColor = triangleColor;
	}

	public Color getVisibleUneditableColor() {
		return visibleUneditableColor;
	}

	public void setVisibleUneditableColor(final Color visibleUneditableColor) {
		this.visibleUneditableColor = visibleUneditableColor;
	}

	public Color getHighlighTriangleColor() {
		return highlighTriangleColor;
	}

	public void setHighlighTriangleColor(final Color highlighTriangleColor) {
		this.highlighTriangleColor = highlighTriangleColor;
	}

	public Color getHighlighVertexColor() {
		return highlighVertexColor;
	}

	public void setHighlighVertexColor(final Color highlighVertexColor) {
		this.highlighVertexColor = highlighVertexColor;
	}

	public Color getNormalsColor() {
		return normalsColor;
	}

	public void setNormalsColor(final Color normalsColor) {
		this.normalsColor = normalsColor;
	}

	public Boolean isInvertedDisplay() {
		return invertedDisplay;
	}

	public void setInvertedDisplay(final Boolean invertedDisplay) {
		this.invertedDisplay = invertedDisplay;
	}

	public Color getPivotPointsColor() {
		return pivotPointsColor;
	}

	public void setPivotPointsColor(final Color pivotPointsColor) {
		this.pivotPointsColor = pivotPointsColor;
	}

	public Color getPivotPointsSelectedColor() {
		return pivotPointsSelectedColor;
	}

	public void setPivotPointsSelectedColor(final Color pivotPointsSelectedColor) {
		this.pivotPointsSelectedColor = pivotPointsSelectedColor;
	}

	public Color getLightsColor() {
		return lightsColor;
	}

	public void setLightsColor(final Color lightsColor) {
		this.lightsColor = lightsColor;
	}

	public Color getAmbientLightColor() {
		return ambientLightColor;
	}

	public void setAmbientLightColor(final Color ambientLightColor) {
		this.ambientLightColor = ambientLightColor;
	}
}
