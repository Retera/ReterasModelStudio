package com.hiveworkshop.rms.parsers.twiImageStuff.TGA;

public enum Type {
	// header[2] - Compression and color types:
	//  0 - No Image Data Included.
	//  1 - Uncompressed, Color mapped image,
	//  2 - Uncompressed, True Color Image,
	//  3 - Uncompressed, Black and white image,
	//  9 - Run-length encoded, Color mapped image,
	// 11 - Run-Length encoded, Black and white image
	NO_IMAGE        ((byte)  0),
	UNCOMP_COL_MAP  ((byte)  1),
	UNCOMP_TRUE_COL ((byte)  2),
	UNCOMP_GREY     ((byte)  3),
	RUNL_COL_MAP    ((byte)  9),
	RUNL_TRUE_COL   ((byte) 10),
	RUNL_GREY       ((byte) 11),
	UNKNOWN       ((byte) -1),
	;
	byte b;
	Type(byte b){
		this.b = b;
	}

	public boolean isRunL(){
		return (b & (0x08)) != 0;
	}
	public byte getByte() {
		return b;
	}

	public static Type getType(byte b){
		return switch (b) {
			case  0 -> NO_IMAGE;
			case  1 -> UNCOMP_COL_MAP;
			case  2 -> UNCOMP_TRUE_COL;
			case  3 -> UNCOMP_GREY;
			case  9 -> RUNL_COL_MAP;
			case 10 -> RUNL_TRUE_COL;
			case 11 -> RUNL_GREY;
			default -> UNKNOWN;
		};
	}
}
