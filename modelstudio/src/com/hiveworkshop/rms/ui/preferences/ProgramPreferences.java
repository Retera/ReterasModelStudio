package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.RootWindowUgg;
import com.hiveworkshop.rms.ui.preferences.listeners.ProgramPreferencesChangeListener;
import net.infonode.docking.View;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ProgramPreferences implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private Boolean showPerspectiveGrid = true;
	private Integer teamColor = 6;
	private Boolean show2dGrid = true;
	private Boolean showNodeForward = false;
	private Boolean showVertexModifierControls = false;
	private Boolean loadBrowsersOnStartup = true;
	private Boolean useBoxesForPivotPoints = true;
	private Boolean allowLoadingNonBlpTextures = true;
	private Boolean renderParticles = true;
	private Color activeColor1 = new Color(255, 200, 200);
	private Color activeColor2 = new Color(170, 60, 0);
	private Boolean loadPortraits = true;

	private GUITheme theme = GUITheme.ALUMINIUM;
	private Integer vertexSize = 3;
	private Integer nodeBoxSize = 5;
	private Boolean quickBrowse = true;
	private String keyBindings = new KeyBindingPrefs().toString();
	private String editorColors = new EditorColorPrefs().toString();
	private String cameraShortcuts = new CameraControlPrefs().toString();
	private String nav3DMouseActions = new Nav3DMousePrefs().toString();
	private String openFileFilter = "";
	private Integer maxNumbersOfUndo = 200;

	private transient KeyBindingPrefs keyBindingsPrefs;
	private transient EditorColorPrefs editorColorsPrefs;
	private transient CameraControlPrefs cameraShortcutsPrefs;
	private transient Nav3DMousePrefs nav3DMousePrefs;

	private String uiElementColors = new UiElementColorPrefs().toString();
	private transient UiElementColorPrefs uiElementColorPrefs;


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
		return getEditorColorPrefs().getColor(ColorThing.SELECT_BOX_COLOR);
	}

	public boolean loadBrowsersOnStartup() {
		return loadBrowsersOnStartup;
	}
	public ProgramPreferences setLoadBrowsersOnStartup(boolean loadBrowsersOnStartup) {
		this.loadBrowsersOnStartup = loadBrowsersOnStartup;
		saveAndFireListeners();
		return this;
	}

	public boolean showNodeForward() {
		return showNodeForward;
	}
	public void setShowNodeForward(final boolean showNodeForward) {
		this.showNodeForward = showNodeForward;
		saveAndFireListeners();
	}

	public boolean showPerspectiveGrid() {
		return showPerspectiveGrid;
	}
	public void setShowPerspectiveGrid(final boolean showPerspectiveGrid) {
		this.showPerspectiveGrid = showPerspectiveGrid;
		saveAndFireListeners();
	}

	public boolean showVMControls() {
		return showVertexModifierControls;
	}
	public void setShowVertexModifierControls(final boolean showVertexModifierControls) {
		this.showVertexModifierControls = showVertexModifierControls;
		saveAndFireListeners();
	}

	public Color getActiveColor1() {
		return activeColor1;
	}
	public void setActiveColor1(final Color activeColor1) {
		this.activeColor1 = activeColor1;
		saveAndFireListeners();
	}

	public Color getActiveColor2() {
		return activeColor2;
	}
	public void setActiveColor2(final Color activeColor2) {
		this.activeColor2 = activeColor2;
		saveAndFireListeners();
	}



	public boolean isLoadPortraits() {
		return loadPortraits;
	}
	public void setLoadPortraits(final boolean loadPortraits) {
		this.loadPortraits = loadPortraits;
		saveAndFireListeners();
	}


	public boolean isUseBoxesForPivotPoints() {
		return useBoxesForPivotPoints == null || useBoxesForPivotPoints;
	}
	public void setUseBoxesForPivotPoints(final Boolean useBoxesForPivotPoints) {
		this.useBoxesForPivotPoints = useBoxesForPivotPoints;
		saveAndFireListeners();
	}

	public Boolean show2dGrid() {
		return show2dGrid != null && show2dGrid;
	}

	public void setShow2dGrid(final Boolean show2dGrid) {
		this.show2dGrid = show2dGrid;
		saveAndFireListeners();
	}

	public Boolean getAllowLoadingNonBlpTextures() {
		return allowLoadingNonBlpTextures != null && allowLoadingNonBlpTextures;
	}
	public void setAllowLoadingNonBlpTextures(final Boolean allowLoadingNonBlpTextures) {
		this.allowLoadingNonBlpTextures = allowLoadingNonBlpTextures;
		saveAndFireListeners();
	}

	public Boolean getRenderParticles() {
		return renderParticles == null || renderParticles;
	}
	public void setRenderParticles(final Boolean renderParticles) {
		this.renderParticles = renderParticles;
		saveAndFireListeners();
	}

	public String getKeyBindings() {
		return keyBindings;
	}

	public KeyBindingPrefs getKeyBindingPrefs() {
		if (keyBindingsPrefs == null) {
			keyBindingsPrefs = new KeyBindingPrefs().parseString(keyBindings);
		}
		return keyBindingsPrefs;
	}
	public KeyBindingPrefs getKeyBindingPrefsCopy() {
		return new KeyBindingPrefs().parseString(keyBindings);
	}

	public ProgramPreferences setKeyBindings(String keyBindings) {
		this.keyBindings = keyBindings;
		if (keyBindingsPrefs == null) {
			keyBindingsPrefs = new KeyBindingPrefs();
		}
		keyBindingsPrefs.parseString(keyBindings);
		return this;
	}

	public ProgramPreferences setKeyBindings(KeyBindingPrefs keyBindingPrefs) {
		this.keyBindings = keyBindingPrefs.toString();
		if (keyBindingsPrefs == null) {
			keyBindingsPrefs = new KeyBindingPrefs();
		}
		keyBindingsPrefs.parseString(keyBindings);
		System.out.println("Saved keybindings!");
		System.out.println(keyBindings);
		saveAndFireListeners();
		return this;
	}



	public String getOpenFileFilter() {
		return openFileFilter;
	}

	public ProgramPreferences setOpenFileFilter(String openFileFilter) {
		this.openFileFilter = openFileFilter;
		return this;
	}


	public String getEditorColors() {
		return editorColors;
	}

	public EditorColorPrefs getEditorColorPrefs() {
		if (editorColorsPrefs == null) {
			editorColorsPrefs = new EditorColorPrefs().parseString(editorColors);
		}
		return editorColorsPrefs;
	}
	public EditorColorPrefs getEditorColorPrefsCopy() {
		return new EditorColorPrefs().parseString(editorColors);
	}

	public ProgramPreferences setEditorColors(String editorColors) {
		this.editorColors = editorColors;
		if (editorColorsPrefs == null) {
			editorColorsPrefs = new EditorColorPrefs();
		}
		editorColorsPrefs.parseString(editorColors);
		return this;
	}

	public ProgramPreferences setEditorColors(EditorColorPrefs editorColors) {
		this.editorColors = editorColors.toString();
		System.out.println("Saved keybindings!");
		System.out.println(editorColors);
		saveAndFireListeners();
		return this;
	}


	public String getUiElementColors() {
		return uiElementColors;
	}

	public UiElementColorPrefs getUiElementColorPrefs() {
		if (uiElementColorPrefs == null) {
			uiElementColorPrefs = new UiElementColorPrefs().parseString(uiElementColors);
		}
		return uiElementColorPrefs;
	}
	public UiElementColorPrefs getUiElementColorPrefsCopy() {
		return new UiElementColorPrefs().parseString(uiElementColors);
	}

	public ProgramPreferences setUiElementColors(String uiElementColors) {
		this.uiElementColors = uiElementColors;
		if (uiElementColorPrefs == null) {
			uiElementColorPrefs = new UiElementColorPrefs();
		}
		uiElementColorPrefs.parseString(uiElementColors);
		return this;
	}

	public ProgramPreferences setUiElementColors(UiElementColorPrefs uiElementColors) {
		this.uiElementColors = uiElementColors.toString();
		System.out.println("Saved UiElementColors!");
		System.out.println(uiElementColors);
		saveAndFireListeners();
		return this;
	}

	public CameraControlPrefs getCameraControlPrefs() {
		if (cameraShortcutsPrefs == null) {
			cameraShortcutsPrefs = new CameraControlPrefs().parseString(cameraShortcuts);
		}
		return cameraShortcutsPrefs;
	}
	public CameraControlPrefs getCameraControlPrefsCopy() {
		return new CameraControlPrefs().parseString(cameraShortcuts);
	}

	public ProgramPreferences setCameraControlPrefs(String cameraShortcuts) {
		this.cameraShortcuts = cameraShortcuts;
		if (cameraShortcutsPrefs == null) {
			cameraShortcutsPrefs = new CameraControlPrefs();
		}
		cameraShortcutsPrefs.parseString(cameraShortcuts);
		return this;
	}

	public ProgramPreferences setCameraControlPrefs(CameraControlPrefs cameraShortcutsPrefs) {
		this.cameraShortcuts = cameraShortcutsPrefs.toString();
		if (this.cameraShortcutsPrefs == null) {
			this.cameraShortcutsPrefs = new CameraControlPrefs();
		}
		this.cameraShortcutsPrefs.parseString(this.cameraShortcuts);
		System.out.println("Saved keybindings!");
		System.out.println(cameraShortcutsPrefs);
		saveAndFireListeners();
		return this;
	}

	public Nav3DMousePrefs getNav3DMousePrefs() {
		if (nav3DMousePrefs == null) {
			nav3DMousePrefs = new Nav3DMousePrefs().parseString(nav3DMouseActions);
		}
		return nav3DMousePrefs;
	}
	public Nav3DMousePrefs getNav3DMousePrefsCopy() {
		return new Nav3DMousePrefs().parseString(nav3DMouseActions);
	}

	public ProgramPreferences setNav3DMousePrefs(String nav3DMouseActions) {
		this.nav3DMouseActions = nav3DMouseActions;
		if (nav3DMousePrefs == null) {
			nav3DMousePrefs = new Nav3DMousePrefs();
		}
		nav3DMousePrefs.parseString(nav3DMouseActions);
		return this;
	}

	public ProgramPreferences setNav3DMousePrefs(Nav3DMousePrefs nav3DMousePrefs) {
		this.nav3DMouseActions = nav3DMousePrefs.toString();
		if (this.nav3DMousePrefs == null) {
			this.nav3DMousePrefs = new Nav3DMousePrefs();
		}
		this.nav3DMousePrefs.parseString(this.nav3DMouseActions);
		System.out.println("Saved keybindings!");
		System.out.println(cameraShortcuts);
		saveAndFireListeners();
		return this;
	}

	public Integer getSelectMouseButton() {
		return getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.SELECT);
	}
	public Integer getThreeDCameraSpinMouseEx() {
		return getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.CAMERA_SPIN);
	}
	public Integer getThreeDCameraPanMouseEx() {
		return getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.CAMERA_PAN);
	}
	public Integer getSnapTransformModifier() {
		return getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.SNAP_TRANSFORM_MODIFIER);
	}
	public Integer getModifyMouseButton() {
		return getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.MODIFY);
	}
	public Integer getAddSelectModifier() {
		return getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.ADD_SELECT_MODIFIER);
	}
	public Integer getRemoveSelectModifier() {
		return getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.REMOVE_SELECT_MODIFIER);
	}


	public Boolean getQuickBrowse() {
		return quickBrowse != null && quickBrowse;
	}
	public void setQuickBrowse(final Boolean quickBrowse) {
		this.quickBrowse = quickBrowse;
		saveAndFireListeners();
	}

	public GUITheme getTheme() {
		return theme;
	}

	public void setTheme(final GUITheme theme) {
		this.theme = theme;
//		SwingUtilities.updateComponentTreeUI(rootComponent);
		saveAndFireListeners();
	}

	public void resetToDefaults() {
		loadFrom(new ProgramPreferences());
	}

	private void firePrefsChanged() {
		if (notifier != null) {
			notifier.runListeners();
		}
	}

	private transient ProgramPreferencesChangeListener notifier = new ProgramPreferencesChangeListener();

	public void addChangeListener(final Runnable listener) {
		if (notifier == null) {
			notifier = new ProgramPreferencesChangeListener();
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

	private void saveAndFireListeners() {
		SaveProfile.save();
		firePrefsChanged();
	}

	public ProgramPreferences saveViewMap() {
////		ViewMap viewMap = new ViewMap();
//		viewList.clear();
//		for(JComponent component : ProgramGlobals.getRootWindowUgg().getDockingWindows()) {
//			if (component instanceof View) {
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

	public byte[] getViewMap() {
		return viewMap;
	}

	public List<View> getViewList() {
		return viewList;
	}
}
