package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.preferences.listeners.ProgramPreferencesChangeListener;

import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

// this only exists for backwards compatibility to let users load old preference files
// and should be removed when deemed to be unnecessary either do to enough of the user base
// on the new format or do to prioritizing clean code over a slight inconvenience for the users
// twilac 21-04-03
public class ProgramPreference2 implements Serializable {
	private static final long serialVersionUID = 1L;
	Color activeRColor1 = new Color(200, 255, 200);
	Color activeRColor2 = new Color(60, 170, 0);
	Color activeColor1 = new Color(255, 200, 200);
	Color activeColor2 = new Color(170, 60, 0);
	Color activeBColor1 = new Color(200, 200, 255);
	Color activeBColor2 = new Color(0, 60, 170);
	transient int selectionType = 0;
	transient int actionType = 3;
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
	int teamColor = 6;
	private int viewMode = 1;
	private boolean showNormals;
	private boolean showPerspectiveGrid;
	private boolean showVertexModifierControls = false;
	private boolean textureModels = true;
	private boolean useNativeMDXParser = true; // left for compat
	private boolean loadPortraits = true;
	private transient boolean cloneOn = false;
	private transient boolean[] dimLocks = new boolean[3];
	private Boolean show2dGrid = true;
	private Boolean useBoxesForPivotPoints = true;
	private Boolean allowLoadingNonBlpTextures = true;
	private Boolean renderParticles = true;
	private Boolean renderStaticPoseParticles = true;
	private int vertexSize = 3;
	private Boolean quickBrowse = true;
	private boolean smallIcons = false;

	private MouseButtonPreference threeDCameraSpinButton = MouseButtonPreference.LEFT;
	private MouseButtonPreference threeDCameraPanButton = MouseButtonPreference.MIDDLE;
	private transient ProgramPreferencesChangeListener.ProgramPreferencesChangeNotifier notifier = new ProgramPreferencesChangeListener.ProgramPreferencesChangeNotifier();

	public void reload() {
		dimLocks = new boolean[3];
		actionType = 3;
		if (show2dGrid == null) {
			show2dGrid = true;
		}
		if (useBoxesForPivotPoints == null) {
			useBoxesForPivotPoints = true;
		}
		if (allowLoadingNonBlpTextures == null) {
			allowLoadingNonBlpTextures = true;
		}
		if (renderParticles == null) {
			renderParticles = true;
		}
		if (renderStaticPoseParticles == null) {
			renderStaticPoseParticles = true;
		}
		if ((vertexColor == null) || (normalsColor == null) || (pivotPointsColor == null)) {
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
		if (animatedBoneSelectedColor == null) {
			animatedBoneSelectedColor = Color.red;
			animatedBoneUnselectedColor = Color.green;
			animatedBoneSelectedUpstreamColor = Color.yellow;
		}
		if (pivotPointsSelectedColor == null) {
			pivotPointsSelectedColor = Color.RED.darker();
		}
		if (selectColor == null) {
			selectColor = Color.RED;
		}
		if (perspectiveBackgroundColor == null) {
			perspectiveBackgroundColor = new Color(80, 80, 80);
		}
		if (backgroundColor == null) {
			backgroundColor = Color.DARK_GRAY.darker();
		}
		if (vertexSize == 0) {
			vertexSize = 3;
		}
		Material.teamColor = teamColor;
		if (threeDCameraSpinButton == null) {
			threeDCameraSpinButton = MouseButtonPreference.LEFT;
			threeDCameraPanButton = MouseButtonPreference.MIDDLE;
		}
		if (theme == null) {
			theme = GUITheme.WINDOWS;
		}
		if (quickBrowse == null) {
			quickBrowse = Boolean.TRUE;
		}
	}

	public void loadFrom(final ProgramPreference2 other) {
		viewMode = other.viewMode;
		showNormals = other.showNormals;
		showPerspectiveGrid = other.showPerspectiveGrid;
		showVertexModifierControls = other.showVertexModifierControls;
		textureModels = other.textureModels;
		useNativeMDXParser = other.useNativeMDXParser;
		loadPortraits = other.loadPortraits;
		show2dGrid = other.show2dGrid;
		smallIcons = other.smallIcons;
		useBoxesForPivotPoints = other.useBoxesForPivotPoints;
		activeRColor1 = other.activeRColor1;
		activeRColor2 = other.activeRColor2;
		activeColor1 = other.activeColor1;
		activeColor2 = other.activeBColor2;
		activeBColor1 = other.activeBColor1;
		activeBColor2 = other.activeBColor2;
		vertexColor = other.vertexColor;
		triangleColor = other.triangleColor;
		visibleUneditableColor = other.visibleUneditableColor;
		highlighTriangleColor = other.highlighTriangleColor;
		highlighVertexColor = other.highlighVertexColor;
		normalsColor = other.normalsColor;
		pivotPointsSelectedColor = other.pivotPointsSelectedColor;
		pivotPointsColor = other.pivotPointsColor;
		animatedBoneSelectedColor = other.animatedBoneSelectedColor;
		animatedBoneUnselectedColor = other.animatedBoneUnselectedColor;
		animatedBoneSelectedUpstreamColor = other.animatedBoneSelectedUpstreamColor;
		lightsColor = other.lightsColor;
		ambientLightColor = other.ambientLightColor;
		selectColor = other.selectColor;
		vertexSize = other.vertexSize;
		teamColor = other.teamColor;
		backgroundColor = other.backgroundColor;
		perspectiveBackgroundColor = other.perspectiveBackgroundColor;
		threeDCameraPanButton = other.threeDCameraPanButton;
		threeDCameraSpinButton = other.threeDCameraSpinButton;
		theme = other.theme;
		quickBrowse = other.quickBrowse;
		allowLoadingNonBlpTextures = other.allowLoadingNonBlpTextures;
		renderParticles = other.renderParticles;
		renderStaticPoseParticles = other.renderStaticPoseParticles;
		SaveProfile.save();
		firePrefsChanged();

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

	public void setDimLocks(final boolean[] dimLocks) {
		this.dimLocks = dimLocks;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getActiveRColor1() {
		return activeRColor1;
	}

	public void setActiveRColor1(final Color activeRColor1) {
		this.activeRColor1 = activeRColor1;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getActiveRColor2() {
		return activeRColor2;
	}

	public void setActiveRColor2(final Color activeRColor2) {
		this.activeRColor2 = activeRColor2;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getActiveColor1() {
		return activeColor1;
	}

	public void setActiveColor1(final Color activeColor1) {
		this.activeColor1 = activeColor1;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getActiveColor2() {
		return activeColor2;
	}

	public void setActiveColor2(final Color activeColor2) {
		this.activeColor2 = activeColor2;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getActiveBColor1() {
		return activeBColor1;
	}

	public void setActiveBColor1(final Color activeBColor1) {
		this.activeBColor1 = activeBColor1;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getActiveBColor2() {
		return activeBColor2;
	}

	public void setActiveBColor2(final Color activeBColor2) {
		this.activeBColor2 = activeBColor2;
		SaveProfile.save();
		firePrefsChanged();
	}

	public int getActionType() {
		return actionType;
	}

	public void setActionType(final int actionType) {
		this.actionType = actionType;
		SaveProfile.save();
		firePrefsChanged();
	}

	public int getViewMode() {
		return viewMode;
	}

	public void setViewMode(final int viewMode) {
		this.viewMode = viewMode;
		SaveProfile.save();
		firePrefsChanged();
	}

	public boolean isShowVertexModifierControls() {
		return showVertexModifierControls;
	}

	public void setShowVertexModifierControls(final boolean showVertexModifierControls) {
		this.showVertexModifierControls = showVertexModifierControls;
		SaveProfile.save();
		firePrefsChanged();
	}

	public boolean isTextureModels() {
		return textureModels;
	}

	public void setTextureModels(final boolean textureModels) {
		this.textureModels = textureModels;
		SaveProfile.save();
		firePrefsChanged();
	}

	public int getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(final int selectionType) {
		this.selectionType = selectionType;
		SaveProfile.save();
		firePrefsChanged();
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

	public boolean isSmallIcons() {
		return smallIcons;
	}

	public void setSmallIcons(boolean smallIcons) {
		this.smallIcons = smallIcons;
		SaveProfile.save();
		firePrefsChanged();
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

	public void setAnimatedBoneSelectedColor(final Color animatedBoneSelectedColor) {
		this.animatedBoneSelectedColor = animatedBoneSelectedColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getAnimatedBoneSelectedUpstreamColor() {
		return animatedBoneSelectedUpstreamColor;
	}

	public void setAnimatedBoneSelectedUpstreamColor(final Color animatedBoneSelectedUpstreamColor) {
		this.animatedBoneSelectedUpstreamColor = animatedBoneSelectedUpstreamColor;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Color getAnimatedBoneUnselectedColor() {
		return animatedBoneUnselectedColor;
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

	public void setAllowLoadingNonBlpTextures(final Boolean allowLoadingNonBlpTextures) {
		this.allowLoadingNonBlpTextures = allowLoadingNonBlpTextures;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Boolean getRenderParticles() {
		return renderParticles == null || renderParticles;
	}

	public void setRenderParticles(final Boolean renderParticles) {
		this.renderParticles = renderParticles;
		SaveProfile.save();
		firePrefsChanged();
	}

	public Boolean getRenderStaticPoseParticles() {
		return renderStaticPoseParticles == null || renderStaticPoseParticles;
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
		loadFrom(new ProgramPreference2());
	}

	private void firePrefsChanged() {
		if (notifier != null) {
			notifier.preferencesChanged();
		}
	}

	public GUITheme getTheme() {
		return theme;
	}

	public void setTheme(final GUITheme theme) {
		this.theme = theme;
		SaveProfile.save();
		firePrefsChanged();
	}

	public void addChangeListener(final ProgramPreferencesChangeListener listener) {
		if (notifier == null) {
			notifier = new ProgramPreferencesChangeListener.ProgramPreferencesChangeNotifier();
		}
		notifier.subscribe(listener);
	}

	public ProgramPreferences getAsNewPrefs() {
		ProgramPreferences newPrefs = new ProgramPreferences();
		Field[] declaredFields = this.getClass().getDeclaredFields();
		Map<String, Method> prefMethMap = new HashMap<>();
		Map<String, Field> pref2NameMap = new HashMap<>();
		for (Field field : declaredFields) {
			pref2NameMap.put(field.getName(), field);
		}
		for (Method method : newPrefs.getClass().getDeclaredMethods()) {
			String name = method.getName();
			if (name.startsWith("set")) {
				prefMethMap.put(name.replaceFirst("set", "").toLowerCase(), method);
			}
		}
		;
		for (String fieldName : pref2NameMap.keySet()) {
			try {
				if (prefMethMap.get(fieldName.toLowerCase()) != null) {
//					System.out.println(fieldName);
					prefMethMap.get(fieldName.toLowerCase()).invoke(newPrefs, pref2NameMap.get(fieldName).get(this));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return newPrefs;
	}
}
