package com.hiveworkshop.rms.parsers.mdlx.util;

public class MdxFlags {

	int DONT_INHERIT_TRANSLATION    = 0x000001; // 0x0001: dont inherit translation
	int DONT_INHERIT_SCALING        = 0x000002; // 0x0004: dont inherit scaling
	int DONT_INHERIT_ROTATION       = 0x000004; // 0x0002: dont inherit rotation
	int BILLBOARDED                 = 0x000008; // 0x0008: billboarded
	int BILLBOARDED_LOCK_X          = 0x000010; // 0x0010: billboarded lock x
	int BILLBOARDED_LOCK_Y          = 0x000020; // 0x0020: billboarded lock y
	int BILLBOARDED_LOCK_Z          = 0x000040; // 0x0040: billboarded lock z
	int CAMERA_ANCHORED             = 0x000080; // 0x0080: camera anchored

//	DONTINHERIT_TRANSLATION("DontInherit { Translation }"),
//	DONTINHERIT_SCALING("DontInherit { Scaling }"),
//	DONTINHERIT_ROTATION("DontInherit { Rotation }"),
//	BILLBOARDED("Billboarded"),
//	BILLBOARD_LOCK_X("BillboardedLockX", "BillboardLockX"),
//	BILLBOARD_LOCK_Y("BillboardedLockY", "BillboardLockY"),
//	BILLBOARD_LOCK_Z("BillboardedLockZ", "BillboardLockZ"),
//	CAMERA_ANCHORED("CameraAnchored");

	int TYPE_HELPER                 = 0x000000;
	int TYPE_BONE                   = 0x000100;
	int TYPE_LIGHT                  = 0x000200;
	int TYPE_EVENTOBJECT            = 0x000400;
	int TYPE_ATTACHMENT             = 0x000800;
	int TYPE_PARTICLEEMITTER        = 0x001000;
	int TYPE_COLLISION_SHAPE        = 0x002000;
	int TYPE_RIBBONEMITTER          = 0x004000;
	int TYPE_POPCORN                = 0x000000;


	int PE_MDL                      = 0x008000; // 0x008000: if particle emitter: emitter uses mdl
	int PE_TGA                      = 0x010000; // 0x010000: if particle emitter: emitter uses tga

	int PE2_UNSHADED                = 0x008000; // 0x008000: if particle emitter 2: unshaded
	int PE2_SORT_PRIM_FAR_Z         = 0x010000; // 0x010000: if particle emitter 2: sort primitives far z
	int PE2_LINE_EMITTER            = 0x020000; // 0x020000: line emitter
	int PE2_UNFOGGED                = 0x040000; // 0x040000: unfogged
	int PE2_MODEL_SPACE             = 0x080000; // 0x080000: model space
	int PE2_XY_QUAD                 = 0x100000; // 0x100000: xy quad
}