package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.RootWindowUgg;
import com.hiveworkshop.rms.ui.preferences.listeners.ProgramPreferencesChangeListener;
import net.infonode.docking.View;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ProgramPreferences implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	transient Integer selectionType = 0;
	transient Integer actionType = 3;
	private Integer teamColor = 6;
	private Integer viewMode = 1;
	private Boolean showNormals = false;
	private Boolean show3dVerts = false;
	private Boolean showPerspectiveGrid = true;
	private Boolean showVertexModifierControls = false;
	private Boolean textureModels = true;
	private Boolean useNativeMDXParser = true; // left for compat
	private Boolean show2dGrid = true;
	private Boolean useBoxesForPivotPoints = true;
	private Boolean allowLoadingNonBlpTextures = true;
	private Boolean renderParticles = true;
	private Boolean renderStaticPoseParticles = true;
	private Color activeRColor1 = new Color(200, 255, 200);
	private Color activeRColor2 = new Color(60, 170, 0);
	private Color activeColor1 = new Color(255, 200, 200);
	private Color activeColor2 = new Color(170, 60, 0);
	private Color activeBColor1 = new Color(200, 200, 255);
	private Color activeBColor2 = new Color(0, 60, 170);
	private Boolean loadPortraits = true;
	private transient Boolean cloneOn = false;

	private Color vertexColor = new Color(0, 0, 255);// new Color(0, 0, 0)
	private Color highlighVertexColor = new Color(0, 255, 0);
	private Color triangleColor = new Color(255, 255, 255);// new Color(190, 190, 190)
	private Color highlighTriangleColor = new Color(255, 255, 0);
	private Color visibleUneditableColor = new Color(150, 150, 255);
	private Color normalsColor = new Color(128, 128, 255);
	private Color pivotPointsSelectedColor = Color.RED.darker();
	private Color pivotPointsColor = Color.MAGENTA;
	private Color animatedBoneUnselectedColor = Color.GREEN;
	private Color animatedBoneSelectedColor = Color.RED;
	private Color animatedBoneSelectedUpstreamColor = Color.YELLOW;
	private Color lightsColor = Color.YELLOW.brighter();
	private Color ambientLightColor = Color.CYAN.brighter();
	private Color backgroundColor = new Color(45, 45, 45);// new Color(190, 190, 190)
	private Color perspectiveBackgroundColor = new Color(80, 80, 80);// new Color(190, 190, 190)
	private Color selectColor = Color.RED;
	private GUITheme theme = GUITheme.ALUMINIUM;
	private transient Boolean[] dimLocks = new Boolean[3];
	private Integer vertexSize = 3;
	private Integer nodeBoxSize = 5;
	private Boolean quickBrowse = true;

	//	private String keyBindings = new KeyBindingPrefs().makeMap().toString();
	private String keyBindings = new KeyBindingPrefs().toString();
	private String editorColors = new EditorColorPrefs().toString();


	private MouseButtonPreference threeDCameraSpinButton = MouseButtonPreference.LEFT;
	private MouseButtonPreference threeDCameraPanButton = MouseButtonPreference.MIDDLE;

	private Integer threeDCameraSpinMouseEx = MouseEvent.BUTTON2_DOWN_MASK;
	private Integer threeDCameraPanMouseEx = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK;
	private Integer selectMouseButton = MouseEvent.BUTTON1_DOWN_MASK;
	private Integer modifyMouseButton = MouseEvent.BUTTON3_DOWN_MASK;
	private Integer addSelectModifier = MouseEvent.SHIFT_DOWN_MASK;
	private Integer removeSelectModifier = MouseEvent.CTRL_DOWN_MASK;

	private Integer maxNumbersOfUndo = 100;


	//	private ViewMap viewMap = new ViewMap();
	byte[] viewMap = new byte[] {};
	private List<View> viewList = new ArrayList<>();

	public void loadFrom(ProgramPreferences other) {
		setFromOther(other);
		saveAndFireListeners();

	}

	public void setFromOther(ProgramPreferences other) {
		Field[] declaredFields = this.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			try {
				if (!Modifier.isFinal(field.getModifiers()) && field.get(other) != null) {
					field.set(this, field.get(other));
				}

			} catch (Exception e) {
				System.out.println("failed on field: " + field.getName());
				e.printStackTrace();
			}
		}
	}

	public Integer getMaxNumbersOfUndo() {
		return maxNumbersOfUndo;
	}

	public ProgramPreferences setMaxNumbersOfUndo(Integer maxNumbersOfUndo) {
		this.maxNumbersOfUndo = maxNumbersOfUndo;
		return this;
	}

	public int getTeamColor() {
		return teamColor;
	}

	public void setTeamColor(final int teamColor) {
		this.teamColor = teamColor;
		saveAndFireListeners();
	}

	public int getVertexSize() {
		return vertexSize;
	}
	public void setVertexSize(final int vertexSize) {
		this.vertexSize = vertexSize;
		saveAndFireListeners();
	}

	public int getNodeBoxSize() {
		return nodeBoxSize;
	}
	public void setNodeBoxSize(final int vertexSize) {
		this.nodeBoxSize = vertexSize;
		saveAndFireListeners();
	}

	public Color getSelectColor() {
		return selectColor;
	}

	public void setSelectColor(final Color selectColor) {
		this.selectColor = selectColor;
		saveAndFireListeners();
	}

	public int viewMode() {
		return viewMode;
	}

	public boolean showNormals() {
		return showNormals;
	}
	public boolean show3dVerts() {
		return show3dVerts;
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
		saveAndFireListeners();
	}

	public void setShowNormals(final boolean showNormals) {
		this.showNormals = showNormals;
		saveAndFireListeners();
	}

	public void setShow3dVerts(final boolean show3dVerts) {
		this.show3dVerts = show3dVerts;
		saveAndFireListeners();
	}

	public void setShowPerspectiveGrid(final boolean showPerspectiveGrid) {
		this.showPerspectiveGrid = showPerspectiveGrid;
		saveAndFireListeners();
	}

	public void setShowVertexModifierControls(final boolean showVertexModifierControls) {
		this.showVertexModifierControls = showVertexModifierControls;
		saveAndFireListeners();
	}

	public void setTextureModels(final boolean textureModels) {
		this.textureModels = textureModels;
		saveAndFireListeners();
	}

	public void setDimLocks(final Boolean[] dimLocks) {
		this.dimLocks = dimLocks;
		saveAndFireListeners();
	}

	public void setActiveRColor1(final Color activeRColor1) {
		this.activeRColor1 = activeRColor1;
		saveAndFireListeners();
	}

	public void setActiveRColor2(final Color activeRColor2) {
		this.activeRColor2 = activeRColor2;
		saveAndFireListeners();
	}

	public void setActiveColor1(final Color activeColor1) {
		this.activeColor1 = activeColor1;
		saveAndFireListeners();
	}

	public void setActiveColor2(final Color activeColor2) {
		this.activeColor2 = activeColor2;
		saveAndFireListeners();
	}

	public void setActiveBColor1(final Color activeBColor1) {
		this.activeBColor1 = activeBColor1;
		saveAndFireListeners();
	}

	public void setActiveBColor2(final Color activeBColor2) {
		this.activeBColor2 = activeBColor2;
		saveAndFireListeners();
	}

	public void setSelectionType(final int selectionType) {
		this.selectionType = selectionType;
		saveAndFireListeners();
	}

	public void setActionType(final int actionType) {
		this.actionType = actionType;
		saveAndFireListeners();
	}

	public void setTheme(final GUITheme theme) {
		this.theme = theme;
		saveAndFireListeners();
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
		saveAndFireListeners();
	}

	public Color getVertexColor() {
		return vertexColor;
	}

	public void setVertexColor(final Color vertexColor) {
		this.vertexColor = vertexColor;
		saveAndFireListeners();
	}

	public Color getTriangleColor() {
		return triangleColor;
	}

	public void setTriangleColor(final Color triangleColor) {
		this.triangleColor = triangleColor;
		saveAndFireListeners();
	}

	public Color getVisibleUneditableColor() {
		return visibleUneditableColor;
	}

	public void setVisibleUneditableColor(final Color visibleUneditableColor) {
		this.visibleUneditableColor = visibleUneditableColor;
		saveAndFireListeners();
	}

	public Color getHighlighTriangleColor() {
		return highlighTriangleColor;
	}

	public void setHighlighTriangleColor(final Color highlighTriangleColor) {
		this.highlighTriangleColor = highlighTriangleColor;
		saveAndFireListeners();
	}

	public Color getHighlighVertexColor() {
		return highlighVertexColor;
	}

	public void setHighlighVertexColor(final Color highlighVertexColor) {
		this.highlighVertexColor = highlighVertexColor;
		saveAndFireListeners();
	}

	public Color getNormalsColor() {
		return normalsColor;
	}

	public void setNormalsColor(final Color normalsColor) {
		this.normalsColor = normalsColor;
		saveAndFireListeners();
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
		saveAndFireListeners();
	}

	public void setShow2dGrid(final Boolean show2dGrid) {
		this.show2dGrid = show2dGrid;
		saveAndFireListeners();
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
		saveAndFireListeners();
	}

	public void setAnimatedBoneSelectedUpstreamColor(final Color animatedBoneSelectedUpstreamColor) {
		this.animatedBoneSelectedUpstreamColor = animatedBoneSelectedUpstreamColor;
		saveAndFireListeners();
	}

	public void setAnimatedBoneUnselectedColor(final Color animatedBoneUnselectedColor) {
		this.animatedBoneUnselectedColor = animatedBoneUnselectedColor;
		saveAndFireListeners();
	}

	public Color getPivotPointsColor() {
		return pivotPointsColor;
	}

	public void setPivotPointsColor(final Color pivotPointsColor) {
		this.pivotPointsColor = pivotPointsColor;
		saveAndFireListeners();
	}

	public Color getPivotPointsSelectedColor() {
		return pivotPointsSelectedColor;
	}

	public void setPivotPointsSelectedColor(final Color pivotPointsSelectedColor) {
		this.pivotPointsSelectedColor = pivotPointsSelectedColor;
		saveAndFireListeners();
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
		saveAndFireListeners();
	}

	public void setRenderParticles(final Boolean renderParticles) {
		this.renderParticles = renderParticles;
		saveAndFireListeners();
	}

	public void setRenderStaticPoseParticles(final Boolean renderStaticPoseParticles) {
		this.renderStaticPoseParticles = renderStaticPoseParticles;
		saveAndFireListeners();
	}

	public Color getLightsColor() {
		return lightsColor;
	}

	public void setLightsColor(final Color lightsColor) {
		this.lightsColor = lightsColor;
		saveAndFireListeners();
	}

	public Color getAmbientLightColor() {
		return ambientLightColor;
	}

	public void setAmbientLightColor(final Color ambientLightColor) {
		this.ambientLightColor = ambientLightColor;
		saveAndFireListeners();
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		saveAndFireListeners();
	}

	public Color getPerspectiveBackgroundColor() {
		if (perspectiveBackgroundColor == null) {
			return new Color(80, 80, 80);
		}
		return perspectiveBackgroundColor;
	}

	public void setPerspectiveBackgroundColor(final Color perspectiveBackgroundColor) {
		this.perspectiveBackgroundColor = perspectiveBackgroundColor;
		saveAndFireListeners();
	}

	public MouseButtonPreference getThreeDCameraSpinButton() {
		return threeDCameraSpinButton;
	}

	public void setThreeDCameraSpinButton(final MouseButtonPreference threeDCameraSpinButton) {
		this.threeDCameraSpinButton = threeDCameraSpinButton;
		saveAndFireListeners();
	}

	public MouseButtonPreference getThreeDCameraPanButton() {
		return threeDCameraPanButton;
	}

	public void setThreeDCameraPanButton(final MouseButtonPreference threeDCameraPanButton) {
		this.threeDCameraPanButton = threeDCameraPanButton;
		saveAndFireListeners();
	}


	public Integer getThreeDCameraSpinMouseEx() {
		return threeDCameraSpinMouseEx;
	}

	public void setThreeDCameraSpinMouseEx(int threeDCameraSpinMouseEx) {
		this.threeDCameraSpinMouseEx = threeDCameraSpinMouseEx;
		saveAndFireListeners();
	}

	public Integer getThreeDCameraPanMouseEx() {
		return threeDCameraPanMouseEx;
	}

	public void setThreeDCameraPanMouseEx(int threeDCameraPanMouseEx) {
		this.threeDCameraPanMouseEx = threeDCameraPanMouseEx;

		saveAndFireListeners();
	}

	public Integer getSelectMouseButton() {
		return selectMouseButton;
	}

	public void setSelectMouseButton(int selectMouseButton) {
		this.selectMouseButton = selectMouseButton;
		saveAndFireListeners();
	}

	public Integer getModifyMouseButton() {
		return modifyMouseButton;
	}

	public void setModifyMouseButton(int modifyMouseButton) {
		this.modifyMouseButton = modifyMouseButton;
		saveAndFireListeners();
	}

	public Integer getAddSelectModifier() {
		return addSelectModifier;
	}

	public void setAddSelectModifier(int addSelectModifier) {
		this.addSelectModifier = addSelectModifier;
		saveAndFireListeners();
	}

	public Integer getRemoveSelectModifier() {
		return removeSelectModifier;
	}

	public void setRemoveSelectModifier(int removeSelectModifier) {
		this.removeSelectModifier = removeSelectModifier;
		saveAndFireListeners();
	}

	public Boolean getQuickBrowse() {
		return quickBrowse != null && quickBrowse;
	}

	public void setQuickBrowse(final Boolean quickBrowse) {
		this.quickBrowse = quickBrowse;
		saveAndFireListeners();
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

	public String getKeyBindings() {
		return keyBindings;
	}

	public KeyBindingPrefs getKeyBindingPrefs() {
		return new KeyBindingPrefs().parseString(keyBindings);
	}

	public ProgramPreferences setKeyBindings(String keyBindings) {
		this.keyBindings = keyBindings;
		return this;
	}

	public ProgramPreferences setKeyBindings(KeyBindingPrefs keyBindingPrefs) {
		this.keyBindings = keyBindingPrefs.toString();
		System.out.println("Saved keybindings!");
		System.out.println(keyBindings);
		saveAndFireListeners();
		return this;
	}


	public String getEditorColors() {
		return editorColors;
	}

	public EditorColorPrefs getEditorColorPrefs() {
		return new EditorColorPrefs().parseString(editorColors);
	}

	public ProgramPreferences setEditorColors(String editorColors) {
		this.editorColors = editorColors;
		return this;
	}

	public ProgramPreferences setEditorColors(EditorColorPrefs editorColors) {
		this.editorColors = editorColors.toString();
		System.out.println("Saved keybindings!");
		System.out.println(editorColors);
		saveAndFireListeners();
		return this;
	}

	public ProgramPreferences saveViewMap() {
////		ViewMap viewMap = new ViewMap();
//		viewList.clear();
//		for(JComponent component : ProgramGlobals.getRootWindowUgg().getDockingWindows()){
//			if (component instanceof View){
//				WindowHandler2.traverseAndStuff((View) component, viewMap, viewList);
//				viewMap.addView(viewList.size(), (View) component);
//				viewList.add((View) component);
//			}
//		}
		RootWindowUgg rootWindowUgg = ProgramGlobals.getRootWindowUgg();
		rootWindowUgg.compileViewMap();
		viewMap = rootWindowUgg.ugg();

		saveAndFireListeners();
		return this;
	}

	private void saveAndFireListeners() {
		SaveProfile.save();
		firePrefsChanged();
	}

	public byte[] getViewMap() {
		return viewMap;
	}

	public List<View> getViewList() {
		return viewList;
	}
}
