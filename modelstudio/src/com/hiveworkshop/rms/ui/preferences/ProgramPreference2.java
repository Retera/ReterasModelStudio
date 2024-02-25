package com.hiveworkshop.rms.ui.preferences;

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
	private transient ProgramPreferencesChangeListener notifier = new ProgramPreferencesChangeListener();

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
					prefMethMap.get(fieldName.toLowerCase()).invoke(newPrefs, pref2NameMap.get(fieldName).get(this));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return newPrefs;
	}
}
