package com.hiveworkshop.rms.ui.language;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;

public enum TextKey {
	UNIT("Unit"),
	MODEL("Model"),
	OBJECT_EDITOR("Object Editor"),
	NEW("New"),
	CLOSE("Close"),
	REVERT("Revert"),
	OPEN("Open"),
	SAVE("Save"),
	SAVE_AS("Save As"),
	UNDO("Undo"),
	REDO("Redo"),
	DELETE("Delete"),
	CLONE_SELECTION("CloneSelection"),
	CUT("Cut"),
	COPY("Copy"),
	PASTE("Paste"),
	DUPLICATE("Duplicate"),
	MAXIMIZE_FOCUSED_WINDOW("Maximize Focused Window"),
	NEXT_KEYFRAME("Jump to next keyframe"),
	PREV_KEYFRAME("Jump to previous keyframe"),
	JUMP_ONE_FRAME_FW("Jump forward 1 frame"),
	JUMP_TEN_FRAMES_FW("Jump forward 10 frames"),
	JUMP_ONE_FRAME_BW("Jump back 1 frame"),
	JUMP_TEN_FRAMES_BW("Jump back 10 frames"),
	PLAY_ACTION("Play/pause Animation"),
	SET_TRANSL_MODE_ANIM("set_Transl_Mode_Anim"),
	SET_SCALING_MODE_ANIM("set_Scaling_Mode_Anim"),
	SET_ROTATE_MODE_ANIM("set_Rotate_Mode_Anim"),
	SET_EXTRUDE_MODE_ANIM("set_Extrude_Mode_Anim"),
	SET_EXTEND_MODE_ANIM("set_Extend_Mode_Anim"),
	SELECTION_TYPE_ANIMATE("Selection_Type_ANIMATE"),
	SELECTION_TYPE_VERTEX("Selection_Type_VERTEX"),
	SELECTION_TYPE_CLUSTER("Selection_Type_CLUSTER"),
	SELECTION_TYPE_FACE("Selection_Type_FACE"),
	SELECTION_TYPE_GROUP("Selection_Type_GROUP"),
	TOGGLE_WIREFRAME("Toggle wireframe"),
	CREATE_FACE("Create face from selection"),
	MOD_ADD_SELECT("Add Selection Modifier"),
	UNMOD_ADD_SELECT("unShiftSelect"),
	MOD_REMOVE_SELECT("Remove Selection Modifier"),
	UNMOD_REMOVE_SELECT("unAltSelect"),
	SELECT_ALL("Select All"),
	INVERT_SELECTION("Invert Selection"),
	EXPAND_SELECTION("Expand Selection"),
	RIG_ACTION("RigAction"),
	MERGE_GEOSETS("Merge Geosets"),
	EXPORT_STATIC_MESH("Export Animated to Static Mesh"),
	EXPORT_ANIMATED_PNG("Export Animated Frame PNG"),
	COPY_KFS_BETWEEN_ANIMS("Copy Keyframes Between Animations"),
	BACK2BACK_ANIMATION("Create Back2Back Animation"),
	SCALING_ANIM_LENGTHS("Change Animation Lengths by Scaling"),
	ASSIGN_800("Assign FormatVersion 800"),
	ASSIGN_1000("Assign FormatVersion 1000"),
	SD_TO_HD("SD -> HD (highly experimental, requires 900 or 1000)"),
	HD_TO_SD("HD -> SD (highly experimental, becomes 800)"),
	REMOVE_LODS("Remove LoDs (highly experimental)"),
	RECALC_TANGENTS("Recalculate Tangents (requires 900 or 1000)"),
	IMP_GEOSET_INTO_GEOSET("Import Geoset into Existing Geoset"),
	SMOOTH_VERTS("Twilac-Style SmoothVerts"),
	EDIT_MODEL_COMPS("Edit/delete model components"),
	IMPORT_ANIM("Import Animation"),
	RECALCULATE_NORMALS("Recalculate Normals"),
	RECALCULATE_EXTENTS("Recalculate Extents"),
	SNAP_VERTICES("Snap Vertices"),
	SNAP_NORMALS("Snap Normals"),
	LINEARIZE_ANIMATIONS("Linearize Animations"),
	SIMPLIFY_KEYFRAMES("Simplify Keyframes"),
	MINIMIZE_GEOSETS("Minimize Geosets"),
	SIMPLIFY_SELECTED_GEOMETRY("Simplify Selected Geometry"),
	SORT_NODES("Sort Nodes"),
	REMOVE_MATERIALS_DUPLICATES("Remove Materials Duplicates"),
	VIEW_MATRICES("View Selected \"Matrices\""),
	FLIP_FACES("Flip all selected faces"),
	FLIP_NORMALS("Flip all selected normals"),
	IMPORT_FROM_FILE("From File"),
	IMPORT_FROM_UNIT("From Unit"),
	IMPORT_FROM_WC3_MODEL("From WC3 Model"),
	IMPORT_FROM_OBJECT_EDITOR("From Object Editor"),
	IMPORT_FROM_WORKSPACE("From Workspace"),
	EXPORT_TEXTURE("Export Texture"),
	KEYBOARDKEY(""),

	;
	String defaultTranslation;
	TextKey(String s){
		defaultTranslation = s;
	}

	public String getDefaultTranslation() {
		return defaultTranslation;
	}

	public String getTranslation(){
		return ProgramGlobals.getTranslator().getText(this);
	}

	@Override
	public String toString() {
		return getTranslation();
	}
}
