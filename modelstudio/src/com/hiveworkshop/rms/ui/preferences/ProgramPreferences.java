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
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

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
	private Boolean loadPortraits = true;

	private Boolean optimizeOnSave = true;

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

	private transient KeyBindingPrefs keyBindingPrefs;
	private transient EditorColorPrefs editorColorPrefs;
	private transient CameraControlPrefs cameraShortcutPrefs;
	private transient Nav3DMousePrefs nav3DMousePrefs;

	private String uiElementColors = new UiElementColorPrefs().toString();
	private transient UiElementColorPrefs uiElementColorPrefs;


	//	private ViewMap viewMap = new ViewMap();
	byte[] viewMap = new byte[] {};
	private List<View> viewList = new ArrayList<>();

	public ProgramPreferences deepCopy() {
		ProgramPreferences preferences = new ProgramPreferences();
		preferences.setFromOther(this);
		return preferences;
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

	public boolean isOptimizeOnSave() {
		return optimizeOnSave;
	}
	public ProgramPreferences setOptimizeOnSave(final boolean optimizeOnSave) {
		this.optimizeOnSave = optimizeOnSave;
		return this;
	}

	public int getTeamColor() {
		return teamColor;
	}
	public ProgramPreferences setTeamColor(final int teamColor) {
		this.teamColor = teamColor;
		return this;
	}

	public int getVertexSize() {
		return vertexSize;
	}
	public void setVertexSize(final int vertexSize) {
		this.vertexSize = vertexSize;
	}

	public int getNodeBoxSize() {
		return nodeBoxSize;
	}
	public ProgramPreferences setNodeBoxSize(final int vertexSize) {
		this.nodeBoxSize = vertexSize;
		return this;
	}

	public boolean loadBrowsersOnStartup() {
		return loadBrowsersOnStartup;
	}
	public ProgramPreferences setLoadBrowsersOnStartup(boolean loadBrowsersOnStartup) {
		this.loadBrowsersOnStartup = loadBrowsersOnStartup;
		return this;
	}

	public boolean showNodeForward() {
		return showNodeForward;
	}
	public ProgramPreferences setShowNodeForward(final boolean showNodeForward) {
		this.showNodeForward = showNodeForward;
		return this;
	}

	public boolean showPerspectiveGrid() {
		return showPerspectiveGrid;
	}
	public ProgramPreferences setShowPerspectiveGrid(final boolean showPerspectiveGrid) {
		this.showPerspectiveGrid = showPerspectiveGrid;
		return this;
	}

	public boolean showVMControls() {
		return showVertexModifierControls;
	}
	public ProgramPreferences setShowVertexModifierControls(final boolean showVertexModifierControls) {
		this.showVertexModifierControls = showVertexModifierControls;
		return this;
	}


	public boolean isLoadPortraits() {
		return loadPortraits;
	}
	public ProgramPreferences setLoadPortraits(final boolean loadPortraits) {
		this.loadPortraits = loadPortraits;
		return this;
	}


	public boolean isUseBoxesForPivotPoints() {
		return useBoxesForPivotPoints == null || useBoxesForPivotPoints;
	}
	public void setUseBoxesForPivotPoints(final Boolean useBoxesForPivotPoints) {
		this.useBoxesForPivotPoints = useBoxesForPivotPoints;
	}

	public Boolean show2dGrid() {
		return show2dGrid != null && show2dGrid;
	}
	public void setShow2dGrid(final Boolean show2dGrid) {
		this.show2dGrid = show2dGrid;
	}

	public Boolean getAllowLoadingNonBlpTextures() {
		return allowLoadingNonBlpTextures != null && allowLoadingNonBlpTextures;
	}
	public void setAllowLoadingNonBlpTextures(final Boolean allowLoadingNonBlpTextures) {
		this.allowLoadingNonBlpTextures = allowLoadingNonBlpTextures;
	}

	public Boolean getRenderParticles() {
		return renderParticles == null || renderParticles;
	}
	public ProgramPreferences setRenderParticles(final Boolean renderParticles) {
		this.renderParticles = renderParticles;
		return this;
	}

	public String getOpenFileFilter() {
		return openFileFilter;
	}
	public ProgramPreferences setOpenFileFilter(String openFileFilter) {
		this.openFileFilter = openFileFilter;
		return this;
	}


	public KeyBindingPrefs getKeyBindingPrefs() {
		if (keyBindingPrefs == null) {
			keyBindingPrefs = new KeyBindingPrefs().parseString(keyBindings);
		}
		return keyBindingPrefs;
	}
	public ProgramPreferences setKeyBindings(String keyBindings) {
		this.keyBindings = keyBindings;
		if (keyBindingPrefs == null) {
			keyBindingPrefs = new KeyBindingPrefs();
		}
		keyBindingPrefs.parseString(keyBindings);
		return this;
	}
	public ProgramPreferences setKeyBindings(KeyBindingPrefs keyBindingPrefs) {
		return setKeyBindings(keyBindingPrefs.toString());
	}

	public Color getSelectColor() {
		return getEditorColorPrefs().getColor(ColorThing.SELECT_BOX_COLOR);
	}

	public EditorColorPrefs getEditorColorPrefs() {
		if (editorColorPrefs == null) {
			editorColorPrefs = new EditorColorPrefs().parseString(editorColors);
		}
		return editorColorPrefs;
	}
	public ProgramPreferences setEditorColors(String editorColors) {
		this.editorColors = editorColors;
		if (editorColorPrefs == null) {
			editorColorPrefs = new EditorColorPrefs();
		}
		editorColorPrefs.parseString(editorColors);
		return this;
	}
	public ProgramPreferences setEditorColors(EditorColorPrefs editorColors) {
		return setEditorColors(editorColors.toString());
	}

	public UiElementColorPrefs getUiElementColorPrefs() {
		if (uiElementColorPrefs == null) {
			uiElementColorPrefs = new UiElementColorPrefs().parseString(uiElementColors);
		}
		return uiElementColorPrefs;
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
		return setUiElementColors(uiElementColors.toString());
	}

	public CameraControlPrefs getCameraControlPrefs() {
		if (cameraShortcutPrefs == null) {
			cameraShortcutPrefs = new CameraControlPrefs().parseString(cameraShortcuts);
		}
		return cameraShortcutPrefs;
	}

	public ProgramPreferences setCameraControlPrefs(String cameraShortcuts) {
		this.cameraShortcuts = cameraShortcuts;
		if (cameraShortcutPrefs == null) {
			cameraShortcutPrefs = new CameraControlPrefs();
		}
		cameraShortcutPrefs.parseString(cameraShortcuts);
		return this;
	}

	public ProgramPreferences setCameraControlPrefs(CameraControlPrefs cameraShortcutsPrefs) {
		return setCameraControlPrefs(cameraShortcutsPrefs.toString());
	}
	public Nav3DMousePrefs getNav3DMousePrefs() {
		if (nav3DMousePrefs == null) {
			nav3DMousePrefs = new Nav3DMousePrefs().parseString(nav3DMouseActions);
		}
		return nav3DMousePrefs;
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
		return setNav3DMousePrefs(nav3DMousePrefs.toString());
	}

	public Boolean getQuickBrowse() {
		return quickBrowse != null && quickBrowse;
	}
	public void setQuickBrowse(final Boolean quickBrowse) {
		this.quickBrowse = quickBrowse;
	}

	public GUITheme getTheme() {
		return theme;
	}
	public void setTheme(final GUITheme theme) {
		this.theme = theme;
	}

	private transient ProgramPreferencesChangeListener notifier = new ProgramPreferencesChangeListener();
	public void addChangeListener(final Runnable listener) {
		if (notifier == null) {
			notifier = new ProgramPreferencesChangeListener();
		}
		notifier.subscribe(listener);
	}
	private void firePrefsChanged() {
		if (notifier != null) {
			notifier.runListeners();
		}
	}

	public void setNullToDefaults() {
		ProgramPreferences defaultPrefs = new ProgramPreferences();
		Field[] declaredFields = this.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			try {
				if (notTransientNorStatic(field) && field.get(this) == null) {
//					System.out.println("filling field: \"" + field.getName() + "\"");
					field.set(this, field.get(defaultPrefs));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private boolean notTransientNorStatic(Field field) {
		int modifiers = field.getModifiers();
//		!Modifier.isStatic(modifiers) || !Modifier.isTransient(modifiers);
		return (modifiers & 128) == 0 && (modifiers & 8) == 0;
	}

	public ProgramPreferences saveToFile() {
		SaveProfileNew.save();
		firePrefsChanged();
		return this;
	}

	public String toString() {
		Field[] declaredFields = ProgramPreferences.class.getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		for (Field field : declaredFields) {
			if (notTransientNorStatic(field)) {
				try {
					String name = field.getName();
					Object o = field.get(this);

					if (o instanceof Collection<?> collection) {
						sb.append(name).append(" = ").append("[\n");
						for (Object e : collection) {
							if (e instanceof String) {
								sb.append("\t\"").append(e).append("\",\n");
							} else {
								sb.append("\t").append(e).append(",\n");
							}
						}
						sb.append("];\n");
					} else {
						if (o instanceof String) {
							sb.append(name).append(" = \"").append(o).append("\";\n");
						} else {
							sb.append(name).append(" = ").append(o).append(";\n");
						}
					}

				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return sb.toString();
	}
	public void fromString(String s) {
		Field[] declaredFields = this.getClass().getDeclaredFields();
		TreeMap<Integer, Field> offsetToField = new TreeMap<>();
		for (Field field : declaredFields) {
			if (notTransientNorStatic(field)) {
				String name = field.getName();
				int indexOf = s.indexOf(name + " = ");
				if (indexOf != -1) {
					offsetToField.put(indexOf, field);
				}
			}
		}

		for (Integer offs : offsetToField.keySet()) {
			Field field = offsetToField.get(offs);
			String name = field.getName();
			String fieldString = s.strip().split(name + " = ")[1];
			Integer nOffs = offsetToField.higherKey(offs);
			if (nOffs != null) {
				String nextName = offsetToField.get(nOffs).getName();
				fieldString = fieldString.strip().split(nextName + " = ")[0];
			}
//			System.out.println(field.getName() + " - " + field.getType() + " [" + fieldString +"]");
			parseField(field, fieldString);
		}

//		Map<String, String> fieldStringsMap = new LinkedHashMap<>();
//		String stump = s;
//		Field lastField = null;
//		for (Field field : declaredFields) {
//			if (notTransientNorStatic(field)) {
//				String name = field.getName();
//				String[] split = stump.split(name + " = ");
//
//				if (lastField != null) {
//					String strip = split[0].strip();
//					fieldStringsMap.put(lastField.getName(), strip);
//					if (!strip.isBlank()) {
//						String s1 = strip.replaceAll("(^\")|(\"?;$)", "");
//						System.out.println(lastField.getName() + " - " + lastField.getType() + " [" + s1 +"]");
//
//						parseField(lastField, s1);
//					}
//				}
//				lastField = field;
//				if (1 < split.length) {
//					stump = split[1];
//				}
//			}
//		}
	}

	private void parseField(Field field, String strip) {
		String s = strip.replaceAll("(^\")|(\"?;\\s*$)", "");
		try {
			if (field.getType().getSuperclass() == Enum.class) {
				field.set(this, Enum.valueOf((Class<Enum>) field.getType(), s));
			} else if (field.getType() == Integer.class) {
				field.set(this, Integer.parseInt(s));
			} else if (field.getType() == Boolean.class) {
				field.set(this, Boolean.parseBoolean(s));
			} else if (field.getType() == String.class) {
				field.set(this, s);
			} else if (field.getType() == Float.class) {
				field.set(this, Float.parseFloat(s));
			} else if (field.getType() == Color.class) {
//				field.set(this, Color.getColor(s));
//				field.set(this, Color.decode(s));
			} else {
				System.out.println("\tUNKNOWN TYPE");
			}

		} catch (Exception e) {
			System.out.println("Failed to parse [" + field.getName() + ", " + field.getType() + "] from \"" + s + "\"");
//			e.printStackTrace();
		}
	}

	public ProgramPreferences saveViewMap() {
////		ViewMap viewMap = new ViewMap();
//		viewList.clear();
//		for (JComponent component : ProgramGlobals.getRootWindowUgg().getDockingWindows()) {
//			if (component instanceof View) {
//				WindowHandler2.traverseAndStuff((View) component, viewMap, viewList);
//				viewMap.addView(viewList.size(), (View) component);
//				viewList.add((View) component);
//			}
//		}
		RootWindowUgg rootWindowUgg = ProgramGlobals.getRootWindowUgg();
		rootWindowUgg.compileViewMap();
		viewMap = rootWindowUgg.ugg();
		return this;
	}

	public byte[] getViewMap() {
		return viewMap;
	}

	public List<View> getViewList() {
		return viewList;
	}
}
