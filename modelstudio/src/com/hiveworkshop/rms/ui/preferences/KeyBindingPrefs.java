package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.util.ActionMapActions;

import javax.swing.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class KeyBindingPrefs implements Serializable {
	private final Map<ActionMapActions, KSMapping> keyStrokeMap = new HashMap<>();
	private final KSMapping UNDO_KSM                      = new KSMapping(KeyStroke.getKeyStroke("control Z"), ActionMapActions.UNDO);
	private final KSMapping REDO_KSM                      = new KSMapping(KeyStroke.getKeyStroke("control Y"), ActionMapActions.REDO);
	private final KSMapping DELETE_KSM                    = new KSMapping(KeyStroke.getKeyStroke("DELETE"), ActionMapActions.DELETE);
	private final KSMapping CLONE_SELECTION_KSM           = new KSMapping(KeyStroke.getKeyStroke(""), ActionMapActions.CLONE_SELECTION);
	private final KSMapping CUT_KSM                       = new KSMapping(KeyStroke.getKeyStroke("control C"), ActionMapActions.CUT);
	private final KSMapping COPY_KSM                      = new KSMapping(KeyStroke.getKeyStroke("control V"), ActionMapActions.COPY);
	private final KSMapping PASTE_KSM                     = new KSMapping(KeyStroke.getKeyStroke("control X"), ActionMapActions.PASTE);
	private final KSMapping DUPLICATE_KSM                 = new KSMapping(KeyStroke.getKeyStroke("control D"), ActionMapActions.DUPLICATE);

	private final KSMapping MAXIMIZE_FOCUSED_WINDOW_KSM   = new KSMapping(KeyStroke.getKeyStroke("SPACE"), ActionMapActions.MAXIMIZE_FOCUSED_WINDOW);
	private final KSMapping NEXT_KEYFRAME_KSM             = new KSMapping(KeyStroke.getKeyStroke("RIGHT"), ActionMapActions.NEXT_KEYFRAME);
	private final KSMapping PREV_KEYFRAME_KSM             = new KSMapping(KeyStroke.getKeyStroke("LEFT"), ActionMapActions.PREV_KEYFRAME);
	private final KSMapping JUMP_ONE_FRAME_FW_KSM         = new KSMapping(KeyStroke.getKeyStroke("UP"), ActionMapActions.JUMP_ONE_FRAME_FW);
	private final KSMapping JUMP_TEN_FRAMES_FW_KSM        = new KSMapping(KeyStroke.getKeyStroke("shift UP"), ActionMapActions.JUMP_TEN_FRAMES_FW);
	private final KSMapping JUMP_ONE_FRAME_BW_KSM         = new KSMapping(KeyStroke.getKeyStroke("DOWN"), ActionMapActions.JUMP_ONE_FRAME_BW);
	private final KSMapping JUMP_TEN_FRAMES_BW_KSM        = new KSMapping(KeyStroke.getKeyStroke("shift DOWN"), ActionMapActions.JUMP_TEN_FRAMES_BW);
	private final KSMapping PLAY_ACTION_KSM               = new KSMapping(KeyStroke.getKeyStroke("control SPACE"), ActionMapActions.PLAY_ACTION);

	private final KSMapping SET_TRANSL_MODE_ANIM_KSM      = new KSMapping(KeyStroke.getKeyStroke("W"), ActionMapActions.SET_TRANSL_MODE_ANIM);
	private final KSMapping SET_SCALING_MODE_ANIM_KSM     = new KSMapping(KeyStroke.getKeyStroke("E"), ActionMapActions.SET_SCALING_MODE_ANIM);
	private final KSMapping SET_ROTATE_MODE_ANIM_KSM      = new KSMapping(KeyStroke.getKeyStroke("R"), ActionMapActions.SET_ROTATE_MODE_ANIM);
	private final KSMapping SET_EXTRUDE_MODE_ANIM_KSM     = new KSMapping(KeyStroke.getKeyStroke("T"), ActionMapActions.SET_EXTRUDE_MODE_ANIM);
	private final KSMapping SET_EXTEND_MODE_ANIM_KSM      = new KSMapping(KeyStroke.getKeyStroke("Y"), ActionMapActions.SET_EXTEND_MODE_ANIM);

	private final KSMapping SELECTION_TYPE_ANIMATE_KSM    = new KSMapping(KeyStroke.getKeyStroke("A"), ActionMapActions.SELECTION_TYPE_ANIMATE);
	private final KSMapping SELECTION_TYPE_VERTEX_KSM     = new KSMapping(KeyStroke.getKeyStroke("S"), ActionMapActions.SELECTION_TYPE_VERTEX);
	private final KSMapping SELECTION_TYPE_CLUSTER_KSM    = new KSMapping(KeyStroke.getKeyStroke("D"), ActionMapActions.SELECTION_TYPE_CLUSTER);
	private final KSMapping SELECTION_TYPE_FACE_KSM       = new KSMapping(KeyStroke.getKeyStroke("F"), ActionMapActions.SELECTION_TYPE_FACE);
	private final KSMapping SELECTION_TYPE_GROUP_KSM      = new KSMapping(KeyStroke.getKeyStroke("G"), ActionMapActions.SELECTION_TYPE_GROUP);

	private final KSMapping TOGGLE_WIREFRAME_KSM          = new KSMapping(KeyStroke.getKeyStroke("Z"), ActionMapActions.TOGGLE_WIREFRAME);
	private final KSMapping CREATE_FACE_KSM               = new KSMapping(KeyStroke.getKeyStroke("control F"), ActionMapActions.CREATE_FACE);
	private final KSMapping SHIFT_SELECT_KSM              = new KSMapping(KeyStroke.getKeyStroke("shift pressed SHIFT"), ActionMapActions.SHIFT_SELECT);
	private final KSMapping ALT_SELECT_KSM                = new KSMapping(KeyStroke.getKeyStroke("alt pressed ALT"), ActionMapActions.ALT_SELECT);
	private final KSMapping UNSHIFT_SELECT_KSM            = new KSMapping(KeyStroke.getKeyStroke("released SHIFT"), ActionMapActions.UNSHIFT_SELECT);
	private final KSMapping UNALT_SELECT_KSM              = new KSMapping(KeyStroke.getKeyStroke("released ALT"), ActionMapActions.UNALT_SELECT);
	private final KSMapping SELECT_ALL_KSM                = new KSMapping(KeyStroke.getKeyStroke("control A"), ActionMapActions.SELECT_ALL);
	private final KSMapping INVERT_SELECTION_KSM          = new KSMapping(KeyStroke.getKeyStroke("control I"), ActionMapActions.INVERT_SELECTION);
	private final KSMapping EXPAND_SELECTION_KSM          = new KSMapping(KeyStroke.getKeyStroke("control E"), ActionMapActions.EXPAND_SELECTION);
	private final KSMapping RIG_ACTION_KSM                = new KSMapping(KeyStroke.getKeyStroke("control W"), ActionMapActions.RIG_ACTION);
	private final KSMapping MERGE_GEOSETS_KSM             = new KSMapping(KeyStroke.getKeyStroke("control T"), ActionMapActions.MERGE_GEOSETS);

	private final KSMapping EXPORT_STATIC_MESH_KSM      = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.EXPORT_STATIC_MESH);
	private final KSMapping EXPORT_ANIMATED_PNG_KSM     = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.EXPORT_ANIMATED_PNG);
	private final KSMapping COPY_KFS_BETWEEN_ANIMS_KSM  = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.COPY_KFS_BETWEEN_ANIMS);
	private final KSMapping BACK2BACK_ANIMATION_KSM     = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.BACK2BACK_ANIMATION);
	private final KSMapping SCALING_ANIM_LENGTHS_KSM    = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.SCALING_ANIM_LENGTHS);
	private final KSMapping ASSIGN_800_KSM              = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.ASSIGN_800);
	private final KSMapping ASSIGN_1000_KSM             = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.ASSIGN_1000);
	private final KSMapping SD_TO_HD_KSM                = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.SD_TO_HD);
	private final KSMapping HD_TO_SD_KSM                = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.HD_TO_SD);
	private final KSMapping REMOVE_LODS_KSM             = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.REMOVE_LODS);
	private final KSMapping RECALC_TANGENTS_KSM         = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.RECALC_TANGENTS);
	private final KSMapping IMP_GEOSET_INTO_GEOSET_KSM  = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.IMP_GEOSET_INTO_GEOSET);
	private final KSMapping SMOOTH_VERTS_KSM            = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.SMOOTH_VERTS);
	private final KSMapping EDIT_MODEL_COMPS_KSM        = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.EDIT_MODEL_COMPS);
	private final KSMapping IMPORT_ANIM_KSM             = new KSMapping(KeyStroke.getKeyStroke("null"), ActionMapActions.IMPORT_ANIM);

	public KeyBindingPrefs makeMap() {
		keyStrokeMap.put(UNDO_KSM.getAction()                      , UNDO_KSM);
		keyStrokeMap.put(REDO_KSM.getAction()                      , REDO_KSM);
		keyStrokeMap.put(DELETE_KSM.getAction()                    , DELETE_KSM);
		keyStrokeMap.put(CLONE_SELECTION_KSM.getAction()           , CLONE_SELECTION_KSM);

		keyStrokeMap.put(CUT_KSM.getAction()                       , CUT_KSM);
		keyStrokeMap.put(COPY_KSM.getAction()                      , COPY_KSM);
		keyStrokeMap.put(PASTE_KSM.getAction()                     , PASTE_KSM);
		keyStrokeMap.put(DUPLICATE_KSM.getAction()                 , DUPLICATE_KSM);


		keyStrokeMap.put(MAXIMIZE_FOCUSED_WINDOW_KSM.getAction()   , MAXIMIZE_FOCUSED_WINDOW_KSM);
		keyStrokeMap.put(NEXT_KEYFRAME_KSM.getAction()             , NEXT_KEYFRAME_KSM);
		keyStrokeMap.put(PREV_KEYFRAME_KSM.getAction()             , PREV_KEYFRAME_KSM);
		keyStrokeMap.put(JUMP_ONE_FRAME_FW_KSM.getAction()         , JUMP_ONE_FRAME_FW_KSM);
		keyStrokeMap.put(JUMP_TEN_FRAMES_FW_KSM.getAction()        , JUMP_TEN_FRAMES_FW_KSM);
		keyStrokeMap.put(JUMP_ONE_FRAME_BW_KSM.getAction()         , JUMP_ONE_FRAME_BW_KSM);
		keyStrokeMap.put(JUMP_TEN_FRAMES_BW_KSM.getAction()        , JUMP_TEN_FRAMES_BW_KSM);
		keyStrokeMap.put(PLAY_ACTION_KSM.getAction()               , PLAY_ACTION_KSM);
		keyStrokeMap.put(SET_TRANSL_MODE_ANIM_KSM.getAction()      , SET_TRANSL_MODE_ANIM_KSM);
		keyStrokeMap.put(SET_SCALING_MODE_ANIM_KSM.getAction()     , SET_SCALING_MODE_ANIM_KSM);
		keyStrokeMap.put(SET_ROTATE_MODE_ANIM_KSM.getAction()      , SET_ROTATE_MODE_ANIM_KSM);
		keyStrokeMap.put(SET_EXTRUDE_MODE_ANIM_KSM.getAction()     , SET_EXTRUDE_MODE_ANIM_KSM);
		keyStrokeMap.put(SET_EXTEND_MODE_ANIM_KSM.getAction()      , SET_EXTEND_MODE_ANIM_KSM);
		keyStrokeMap.put(SELECTION_TYPE_ANIMATE_KSM.getAction()    , SELECTION_TYPE_ANIMATE_KSM);
		keyStrokeMap.put(SELECTION_TYPE_VERTEX_KSM.getAction()     , SELECTION_TYPE_VERTEX_KSM);
		keyStrokeMap.put(SELECTION_TYPE_CLUSTER_KSM.getAction()    , SELECTION_TYPE_CLUSTER_KSM);
		keyStrokeMap.put(SELECTION_TYPE_FACE_KSM.getAction()       , SELECTION_TYPE_FACE_KSM);
		keyStrokeMap.put(SELECTION_TYPE_GROUP_KSM.getAction()      , SELECTION_TYPE_GROUP_KSM);
		keyStrokeMap.put(TOGGLE_WIREFRAME_KSM.getAction()          , TOGGLE_WIREFRAME_KSM);
		keyStrokeMap.put(CREATE_FACE_KSM.getAction()               , CREATE_FACE_KSM);
		keyStrokeMap.put(SHIFT_SELECT_KSM.getAction()              , SHIFT_SELECT_KSM);
		keyStrokeMap.put(ALT_SELECT_KSM.getAction()                , ALT_SELECT_KSM);
		keyStrokeMap.put(UNSHIFT_SELECT_KSM.getAction()            , UNSHIFT_SELECT_KSM);
		keyStrokeMap.put(UNALT_SELECT_KSM.getAction()              , UNALT_SELECT_KSM);
		keyStrokeMap.put(SELECT_ALL_KSM.getAction()                , SELECT_ALL_KSM);
		keyStrokeMap.put(INVERT_SELECTION_KSM.getAction()          , INVERT_SELECTION_KSM);
		keyStrokeMap.put(EXPAND_SELECTION_KSM.getAction()          , EXPAND_SELECTION_KSM);
		keyStrokeMap.put(RIG_ACTION_KSM.getAction()                , RIG_ACTION_KSM);
		keyStrokeMap.put(MERGE_GEOSETS_KSM.getAction()             , MERGE_GEOSETS_KSM);

		keyStrokeMap.put(EXPORT_STATIC_MESH_KSM.getAction()         , EXPORT_STATIC_MESH_KSM);
		keyStrokeMap.put(EXPORT_ANIMATED_PNG_KSM.getAction()        , EXPORT_ANIMATED_PNG_KSM);
		keyStrokeMap.put(COPY_KFS_BETWEEN_ANIMS_KSM.getAction()     , COPY_KFS_BETWEEN_ANIMS_KSM);
		keyStrokeMap.put(BACK2BACK_ANIMATION_KSM.getAction()        , BACK2BACK_ANIMATION_KSM);
		keyStrokeMap.put(SCALING_ANIM_LENGTHS_KSM.getAction()       , SCALING_ANIM_LENGTHS_KSM);
		keyStrokeMap.put(ASSIGN_800_KSM.getAction()                 , ASSIGN_800_KSM);
		keyStrokeMap.put(ASSIGN_1000_KSM.getAction()                , ASSIGN_1000_KSM);
		keyStrokeMap.put(SD_TO_HD_KSM.getAction()                   , SD_TO_HD_KSM);
		keyStrokeMap.put(HD_TO_SD_KSM.getAction()                   , HD_TO_SD_KSM);
		keyStrokeMap.put(REMOVE_LODS_KSM.getAction()                , REMOVE_LODS_KSM);
		keyStrokeMap.put(RECALC_TANGENTS_KSM.getAction()            , RECALC_TANGENTS_KSM);
		keyStrokeMap.put(IMP_GEOSET_INTO_GEOSET_KSM.getAction()     , IMP_GEOSET_INTO_GEOSET_KSM);
		keyStrokeMap.put(SMOOTH_VERTS_KSM.getAction()               , SMOOTH_VERTS_KSM);
		keyStrokeMap.put(EDIT_MODEL_COMPS_KSM.getAction()           , EDIT_MODEL_COMPS_KSM);
		keyStrokeMap.put(IMPORT_ANIM_KSM.getAction()                , IMPORT_ANIM_KSM);

		keyStrokeMap.put(ActionMapActions.KEYBOARDKEY              , new KSMapping(KeyStroke.getKeyStroke(""), ActionMapActions.KEYBOARDKEY));
		return this;
	}

	public KeyStroke getKeyStroke(ActionMapActions action){
		return keyStrokeMap.get(action).getKeyStroke();
	}


	public void setKeyStroke(ActionMapActions action, KeyStroke keyStroke){
		keyStrokeMap.get(action).setKeyStroke(keyStroke);
	}

	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		for (ActionMapActions action : ActionMapActions.values()){
			if(keyStrokeMap.get(action) != null){
				stringBuilder.append(action.name()).append("=").append(keyStrokeMap.get(action).keyStroke).append("\n");
			}
		}
		return stringBuilder.toString();
	}

	public KeyBindingPrefs parseString(String string){
		String[] lines = string.split("\n");
		for(String line : lines){
			String[] s = line.split("=");
			if(s.length>1){
				ActionMapActions mapAction = ActionMapActions.valueOf(s[0].strip());
				KeyStroke keyStroke = KeyStroke.getKeyStroke(s[1]);
				keyStrokeMap.computeIfAbsent(mapAction, k -> new KSMapping(keyStroke, mapAction)).setKeyStroke(keyStroke);
			}
		}
		return this;
	}


	public InputMap getInputMap(){
		InputMap inputMap = new InputMap();
		for (ActionMapActions action : ActionMapActions.values()){
			if(keyStrokeMap.get(action) != null){
				inputMap.put(keyStrokeMap.get(action).getKeyStroke(), action.getName());
			}
		}
		return inputMap;
	}

	public ActionMap getActionMap(){
		ActionMap actionMap = new ActionMap();
		for (ActionMapActions action : ActionMapActions.values()){
			actionMap.put(action.getName(), action.getAction());
		}
		return actionMap;
	}

	public JMenuItem getMenuItem(ActionMapActions action){
		if(keyStrokeMap.get(action) != null){
			return keyStrokeMap.get(action).getMenuItem();
		}
		return null;
	}


	public void setNullToDefaults() {
		KeyBindingPrefs defaultPrefs = new KeyBindingPrefs().makeMap();

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
		makeMap();
		for (ActionMapActions action : ActionMapActions.values()){
			keyStrokeMap.computeIfAbsent(action, k -> defaultPrefs.keyStrokeMap.get(action));
		}
	}

	private static class KSMapping {
		KeyStroke keyStroke;
		ActionMapActions action;
		JMenuItem menuItem;
		KSMapping(KeyStroke keyStroke, ActionMapActions action){
			this.keyStroke = keyStroke;
			this.action = action;
			menuItem = new JMenuItem(action.getAction());
			menuItem.setAccelerator(keyStroke);
		}

		public KeyStroke getKeyStroke() {
			return keyStroke;
		}

		public KSMapping setKeyStroke(KeyStroke keyStroke) {
			this.keyStroke = keyStroke;
			menuItem.setAccelerator(keyStroke);
			return this;
		}

		public JMenuItem getMenuItem() {
			return menuItem;
		}

		public ActionMapActions getAction() {
			return action;
		}
	}
}
