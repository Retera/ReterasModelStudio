package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.ui.preferences.listeners.ProgramPreferencesChangeListener;

import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;

public class ProgramPreferences implements Serializable {
	private static final long serialVersionUID = 1L;
	transient Integer selectionType = 0;
	transient Integer actionType = 3;
	Integer teamColor = 6;
	private Integer viewMode = 1;
	private Boolean showNormals = false;
	private Boolean showPerspectiveGrid = true;
	private Boolean showVertexModifierControls = false;
	private Boolean textureModels = true;
	private Boolean useNativeMDXParser = true; // left for compat
	private Boolean show2dGrid = true;
	private Boolean useBoxesForPivotPoints = true;
	private Boolean allowLoadingNonBlpTextures = true;
	private Boolean renderParticles = true;
	private Boolean renderStaticPoseParticles = true;
	Color activeRColor1 = new Color(200, 255, 200);
	Color activeRColor2 = new Color(60, 170, 0);
	Color activeColor1 = new Color(255, 200, 200);
	Color activeColor2 = new Color(170, 60, 0);
	Color activeBColor1 = new Color(200, 200, 255);
	Color activeBColor2 = new Color(0, 60, 170);
	private Boolean loadPortraits = true;
	private transient Boolean cloneOn = false;

	Color vertexColor = new Color(0, 0, 255);// new Color(0, 0, 0)
	Color highlighVertexColor = new Color(0, 255, 0);
	Color triangleColor = new Color(255, 255, 255);// new Color(190, 190, 190)
	Color highlighTriangleColor = new Color(255, 255, 0);
	Color visibleUneditableColor = new Color(150, 150, 255);
	Color normalsColor = new Color(128, 128, 255);
	Color pivotPointsSelectedColor = Color.RED.darker();
	Color pivotPointsColor = Color.MAGENTA;
	Color animatedBoneUnselectedColor = Color.GREEN;
	Color animatedBoneSelectedColor = Color.RED;
	Color animatedBoneSelectedUpstreamColor = Color.YELLOW;
	Color lightsColor = Color.YELLOW.brighter();
	Color ambientLightColor = Color.CYAN.brighter();
	Color backgroundColor = new Color(45, 45, 45);// new Color(190, 190, 190)
	Color perspectiveBackgroundColor = new Color(80, 80, 80);// new Color(190, 190, 190)
	Color selectColor = Color.RED;
	GUITheme theme = GUITheme.ALUMINIUM;
	private transient Boolean[] dimLocks = new Boolean[3];
	private Integer vertexSize = 3;
	private Boolean quickBrowse = true;


	private MouseButtonPreference threeDCameraSpinButton = MouseButtonPreference.LEFT;
	private MouseButtonPreference threeDCameraPanButton = MouseButtonPreference.MIDDLE;

	public void loadFrom(ProgramPreferences other) {
		setFromOther(other);
		SaveProfile.save();
		firePrefsChanged();

	}

	public void setFromOther(ProgramPreferences other) {
		Field[] declaredFields = this.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			try {
				if (field.get(other) != null) {
					field.set(this, field.get(other));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int getTeamColor() {
		return teamColor;
	}

	public void setTeamColor(final int teamColor) {
		this.teamColor = teamColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public int getVertexSize() {
		return vertexSize;
	}

	public void setVertexSize(final int vertexSize) {
		this.vertexSize = vertexSize;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getSelectColor() {
		return selectColor;
	}

	public void setSelectColor(final Color selectColor) {
		this.selectColor = selectColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public int viewMode() {
		return viewMode;
	}

	public boolean showNormals() {
		return showNormals;
	}

	public boolean showPerspectiveGrid() {
		return showPerspectiveGrid;
	}

	public boolean isCloneOn() {
		return cloneOn;
	}

	public void setCloneOn(final boolean cloneOn) {
		this.cloneOn = cloneOn;
		firePrefsChanged();
	}

	public void setDimLock(final int x, final boolean flag) {
		dimLocks[x] = flag;
		firePrefsChanged();
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
		firePrefsChanged();
	}

	public void setShowNormals(final boolean showNormals) {
		this.showNormals = showNormals;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setShowPerspectiveGrid(final boolean showPerspectiveGrid) {
		this.showPerspectiveGrid = showPerspectiveGrid;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setShowVertexModifierControls(final boolean showVertexModifierControls) {
		this.showVertexModifierControls = showVertexModifierControls;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setTextureModels(final boolean textureModels) {
		this.textureModels = textureModels;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setDimLocks(final Boolean[] dimLocks) {
		this.dimLocks = dimLocks;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setActiveRColor1(final Color activeRColor1) {
		this.activeRColor1 = activeRColor1;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setActiveRColor2(final Color activeRColor2) {
		this.activeRColor2 = activeRColor2;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setActiveColor1(final Color activeColor1) {
		this.activeColor1 = activeColor1;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setActiveColor2(final Color activeColor2) {
		this.activeColor2 = activeColor2;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setActiveBColor1(final Color activeBColor1) {
		this.activeBColor1 = activeBColor1;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setActiveBColor2(final Color activeBColor2) {
		this.activeBColor2 = activeBColor2;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setSelectionType(final int selectionType) {
		this.selectionType = selectionType;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setActionType(final int actionType) {
		this.actionType = actionType;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setTheme(final GUITheme theme) {
		this.theme = theme;
		SaveProfile.save();
		firePrefsChanged();
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

	public boolean isShowVertexModifierControls() {
		return showVertexModifierControls;
	}

	public boolean isTextureModels() {
		return textureModels;
	}

	public int getSelectionType() {
		return selectionType;
	}

	public boolean isLoadPortraits() {
		return loadPortraits;
	}

	public void setLoadPortraits(final boolean loadPortraits) {
		this.loadPortraits = loadPortraits;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getVertexColor() {
		return vertexColor;
	}

	public void setVertexColor(final Color vertexColor) {
		this.vertexColor = vertexColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getTriangleColor() {
		return triangleColor;
	}

	public void setTriangleColor(final Color triangleColor) {
		this.triangleColor = triangleColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getVisibleUneditableColor() {
		return visibleUneditableColor;
	}

	public void setVisibleUneditableColor(final Color visibleUneditableColor) {
		this.visibleUneditableColor = visibleUneditableColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getHighlighTriangleColor() {
		return highlighTriangleColor;
	}

	public void setHighlighTriangleColor(final Color highlighTriangleColor) {
		this.highlighTriangleColor = highlighTriangleColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getHighlighVertexColor() {
		return highlighVertexColor;
	}

	public void setHighlighVertexColor(final Color highlighVertexColor) {
		this.highlighVertexColor = highlighVertexColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getNormalsColor() {
		return normalsColor;
	}

	public void setNormalsColor(final Color normalsColor) {
		this.normalsColor = normalsColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Boolean show2dGrid() {
		return show2dGrid != null && show2dGrid;
	}


	public Boolean getUseBoxesForPivotPoints() {
		return useBoxesForPivotPoints != null && useBoxesForPivotPoints;
	}

	public boolean isUseBoxesForPivotPoints() {
		if (useBoxesForPivotPoints == null) {
			return true;
		}
		return useBoxesForPivotPoints;
	}

	public void setUseBoxesForPivotPoints(final Boolean useBoxesForPivotPoints) {
		this.useBoxesForPivotPoints = useBoxesForPivotPoints;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setShow2dGrid(final Boolean show2dGrid) {
		this.show2dGrid = show2dGrid;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getAnimatedBoneSelectedColor() {
		return animatedBoneSelectedColor;
	}

	public Color getAnimatedBoneSelectedUpstreamColor() {
		return animatedBoneSelectedUpstreamColor;
	}

	public Color getAnimatedBoneUnselectedColor() {
		return animatedBoneUnselectedColor;
	}

	public void setAnimatedBoneSelectedColor(final Color animatedBoneSelectedColor) {
		this.animatedBoneSelectedColor = animatedBoneSelectedColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setAnimatedBoneSelectedUpstreamColor(final Color animatedBoneSelectedUpstreamColor) {
		this.animatedBoneSelectedUpstreamColor = animatedBoneSelectedUpstreamColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setAnimatedBoneUnselectedColor(final Color animatedBoneUnselectedColor) {
		this.animatedBoneUnselectedColor = animatedBoneUnselectedColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getPivotPointsColor() {
		return pivotPointsColor;
	}

	public void setPivotPointsColor(final Color pivotPointsColor) {
		this.pivotPointsColor = pivotPointsColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getPivotPointsSelectedColor() {
		return pivotPointsSelectedColor;
	}

	public void setPivotPointsSelectedColor(final Color pivotPointsSelectedColor) {
		this.pivotPointsSelectedColor = pivotPointsSelectedColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Boolean getAllowLoadingNonBlpTextures() {
		return allowLoadingNonBlpTextures != null && allowLoadingNonBlpTextures;
	}

	public Boolean getRenderParticles() {
		return renderParticles == null || renderParticles;
	}

	public Boolean getRenderStaticPoseParticles() {
		return renderStaticPoseParticles == null || renderStaticPoseParticles;
	}

	public void setAllowLoadingNonBlpTextures(final Boolean allowLoadingNonBlpTextures) {
		this.allowLoadingNonBlpTextures = allowLoadingNonBlpTextures;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setRenderParticles(final Boolean renderParticles) {
		this.renderParticles = renderParticles;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void setRenderStaticPoseParticles(final Boolean renderStaticPoseParticles) {
		this.renderStaticPoseParticles = renderStaticPoseParticles;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getLightsColor() {
		return lightsColor;
	}

	public void setLightsColor(final Color lightsColor) {
		this.lightsColor = lightsColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getAmbientLightColor() {
		return ambientLightColor;
	}

	public void setAmbientLightColor(final Color ambientLightColor) {
		this.ambientLightColor = ambientLightColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getPerspectiveBackgroundColor() {
		if (perspectiveBackgroundColor == null) {
			return new Color(80, 80, 80);
		}
		return perspectiveBackgroundColor;
	}

	public void setPerspectiveBackgroundColor(final Color perspectiveBackgroundColor) {
		this.perspectiveBackgroundColor = perspectiveBackgroundColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public MouseButtonPreference getThreeDCameraSpinButton() {
		return threeDCameraSpinButton;
	}

	public void setThreeDCameraSpinButton(final MouseButtonPreference threeDCameraSpinButton) {
		this.threeDCameraSpinButton = threeDCameraSpinButton;
		SaveProfile.save();
		firePrefsChanged();
	}

	public MouseButtonPreference getThreeDCameraPanButton() {
		return threeDCameraPanButton;
	}

	public void setThreeDCameraPanButton(final MouseButtonPreference threeDCameraPanButton) {
		this.threeDCameraPanButton = threeDCameraPanButton;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Boolean getQuickBrowse() {
		return quickBrowse != null && quickBrowse;
	}

	public void setQuickBrowse(final Boolean quickBrowse) {
		this.quickBrowse = quickBrowse;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void resetToDefaults() {
		loadFrom(new ProgramPreferences());
	}

	private void firePrefsChanged() {
		if (notifier != null) {
			notifier.preferencesChanged();
		}
	}

	public GUITheme getTheme() {
		return theme;
	}

	private transient ProgramPreferencesChangeListener.ProgramPreferencesChangeNotifier notifier = new ProgramPreferencesChangeListener.ProgramPreferencesChangeNotifier();

	public void addChangeListener(final ProgramPreferencesChangeListener listener) {
		if (notifier == null) {
			notifier = new ProgramPreferencesChangeListener.ProgramPreferencesChangeNotifier();
		}
		notifier.subscribe(listener);
	}

	public void setNullToDefaults() {
		ProgramPreferences defaultPrefs = new ProgramPreferences();
		Field[] declaredFields = this.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			try {
				if (field.get(this) == null) {
					field.set(this, field.get(defaultPrefs));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
