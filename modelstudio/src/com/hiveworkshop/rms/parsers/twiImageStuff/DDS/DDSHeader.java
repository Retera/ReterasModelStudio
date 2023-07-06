package com.hiveworkshop.rms.parsers.twiImageStuff.DDS;

import com.hiveworkshop.rms.parsers.twiImageStuff.ReaderUtils;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.CharInt;

import java.util.Arrays;

public class DDSHeader {
	int dwSize = 124; // size of header
	int dwFlags = 0x1 | 0x2 | 0x4 /*| 0x8*/ | 0x1000 /*| 0x20000 */ | 0x80000 /*| 0x800000 */;
	int dwHeight;
	int dwWidth;
	int dwPitchOrLinearSize;  // max( 1, ((width+3)/4) ) * block-size
	int dwDepth = 0; // for dept hmask
	int dwMipMapCount;
	int[] dwReserved1 = new int[11]; // Unused int[11];
	DDS_PIXELFORMAT ddspf;
	int dwCaps = 0x1000;
	int dwCaps2 = 0;
	int dwCaps3 = 0;        // Unused
	int dwCaps4 = 0;        // Unused
	int dwReserved2 = 0;    // Unused
	// Flags:
	//  DDSD_CAPS	        Required in every .dds file.	                                0x1
	//  DDSD_HEIGHT	        Required in every .dds file.	                                0x2
	//  DDSD_WIDTH	        Required in every .dds file.	                                0x4
	//  DDSD_PITCH	        Required when pitch is provided for an uncompressed texture.	0x8
	//  DDSD_PIXELFORMAT	Required in every .dds file.	                                0x1000
	//  DDSD_MIPMAPCOUNT	Required in a mipmapped texture.	                            0x20000
	//  DDSD_LINEARSIZE	    Required when pitch is provided for a compressed texture.	    0x80000
	//  DDSD_DEPTH	        Required in a depth texture.	                                0x800000

	public DDSHeader(){
		ddspf = new DDS_PIXELFORMAT();

	}
	public DDSHeader(byte[] header){
		int byteI = 0;

		dwSize =                ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwFlags =               ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwHeight =              ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwWidth =               ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwPitchOrLinearSize =   ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwDepth =               ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwMipMapCount =         ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		for(int i = 0; i<dwReserved1.length; i++){
			dwReserved1[i] = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
		}
		ddspf = new DDS_PIXELFORMAT(header);
		dwCaps =        ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwCaps2 =       ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwCaps3 =       ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwCaps4 =       ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
		dwReserved2 =   ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
		byteI+=4;
	}
	public DDSHeader(int[] header){
		int byteI = 0;
		dwSize              = header[byteI++];
		dwFlags             = header[byteI++];
		dwHeight            = header[byteI++];
		dwWidth             = header[byteI++];
		dwPitchOrLinearSize = header[byteI++];
		dwDepth             = header[byteI++];
		dwMipMapCount       = header[byteI++];
		for(int i = 0; i < dwReserved1.length; i++){
			dwReserved1[i] = header[byteI++];
		}
		ddspf = new DDS_PIXELFORMAT(header);
		byteI = 26;
		dwCaps      = header[byteI++];
		dwCaps2     = header[byteI++];
		dwCaps3     = header[byteI++];
		dwCaps4     = header[byteI++];
		dwReserved2 = header[byteI++];
	}

	@Override
	public String toString() {
		return "DDSHeader{" +
				"\n\tdwSize=" + dwSize +
				"\n\tdwFlags=" + dwFlags + ", " + dwFlags() + ", " + flagsOf(dwFlags) +
				"\n\tdwHeight=" + dwHeight +
				"\n\tdwWidth=" + dwWidth +
				"\n\tdwPitchOrLinearSize=" + dwPitchOrLinearSize +
				"\n\tdwDepth=" + dwDepth +
				"\n\tdwMipMapCount=" + dwMipMapCount +
				"\n\tdwReserved1=" + Arrays.toString(dwReserved1) +
				"\n\t" + ddspf +
				"\n\tdwCaps=" + dwCaps + ", " + dwCaps() + ", " + flagsOf(dwCaps) +
				"\n\tdwCaps2=" + dwCaps2 + ", " + dwCaps2() +
				"\n\tdwCaps3=" + dwCaps3 +
				"\n\tdwCaps4=" + dwCaps4 +
				"\n\tdwReserved2=" + dwReserved2 +
				"\n}";
	}

	// Flags:
	//  DDSD_CAPS	        Required in every .dds file.	                                0x1
	//  DDSD_HEIGHT	        Required in every .dds file.	                                0x2
	//  DDSD_WIDTH	        Required in every .dds file.	                                0x4
	//  DDSD_PITCH	        Required when pitch is provided for an uncompressed texture.	0x8
	//  DDSD_PIXELFORMAT	Required in every .dds file.	                                0x1000
	//  DDSD_MIPMAPCOUNT	Required in a mipmapped texture.	                            0x20000
	//  DDSD_LINEARSIZE	    Required when pitch is provided for a compressed texture.	    0x80000
	//  DDSD_DEPTH	        Required in a depth texture.	                                0x800000
	String dwFlags(){
		String fString = "[";
		fString += (dwFlags&0x000001)==0x000001 ? "DDSD_CAPS, "         : "";
		fString += (dwFlags&0x000002)==0x000002 ? "DDSD_HEIGHT, "       : "";
		fString += (dwFlags&0x000004)==0x000004 ? "DDSD_WIDTH, "        : "";
		fString += (dwFlags&0x000008)==0x000008 ? "DDSD_PITCH, "        : "";
		fString += (dwFlags&0x001000)==0x001000 ? "DDSD_PIXELFORMAT, "  : "";
		fString += (dwFlags&0x020000)==0x020000 ? "DDSD_MIPMAPCOUNT, "  : "";
		fString += (dwFlags&0x080000)==0x080000 ? "DDSD_LINEARSIZE, "   : "";
		fString += (dwFlags&0x800000)==0x800000 ? "DDSD_DEPTH, "        : "";
		fString += "]";
		return fString;
	}
	String dwCaps(){
		String fString = "[";
		fString += (dwCaps&0x000002)==0x000002 ? "DDSCAPS_ALPHA, "   : "";
		fString += (dwCaps&0x000008)==0x000008 ? "DDSCAPS_COMPLEX, " : "";
		fString += (dwCaps&0x001000)==0x001000 ? "DDSCAPS_TEXTURE, " : "";
		fString += (dwCaps&0x400000)==0x400000 ? "DDSCAPS_MIPMAP, "  : "";
		fString += "]";
		return fString;
	}
	String dwCaps2(){
		String fString = "[";
		fString += (dwCaps2&0x000200)==0x000200 ? "DDSCAPS2_CUBEMAP, "           : "";
		fString += (dwCaps2&0x000400)==0x000400 ? "DDSCAPS2_CUBEMAP_POSITIVEX, " : "";
		fString += (dwCaps2&0x000800)==0x000800 ? "DDSCAPS2_CUBEMAP_NEGATIVEX, " : "";
		fString += (dwCaps2&0x001000)==0x001000 ? "DDSCAPS2_CUBEMAP_POSITIVEY, " : "";
		fString += (dwCaps2&0x002000)==0x002000 ? "DDSCAPS2_CUBEMAP_NEGATIVEY, " : "";
		fString += (dwCaps2&0x004000)==0x004000 ? "DDSCAPS2_CUBEMAP_POSITIVEZ, " : "";
		fString += (dwCaps2&0x008000)==0x008000 ? "DDSCAPS2_CUBEMAP_NEGATIVEZ, " : "";
		fString += (dwCaps2&0x200000)==0x200000 ? "DDSCAPS2_VOLUME, "            : "";
		fString += "]";
		return fString;
	}
	String flagsOf(int value){
		String fString = "[";
		fString += (value&0x000001)==0x000001 ?      "0x1, " : "";
		fString += (value&0x000002)==0x000002 ?      "0x2, " : "";
		fString += (value&0x000004)==0x000004 ?      "0x4, " : "";
		fString += (value&0x000008)==0x000008 ?      "0x8, " : "";
		fString += (value&0x000010)==0x000010 ?     "0x10, " : "";
		fString += (value&0x000020)==0x000020 ?     "0x20, " : "";
		fString += (value&0x000040)==0x000040 ?     "0x40, " : "";
		fString += (value&0x000080)==0x000080 ?     "0x80, " : "";
		fString += (value&0x000100)==0x000100 ?    "0x100, " : "";
		fString += (value&0x000200)==0x000200 ?    "0x200, " : "";
		fString += (value&0x000400)==0x000400 ?    "0x400, " : "";
		fString += (value&0x000800)==0x000800 ?    "0x800, " : "";
		fString += (value&0x001000)==0x001000 ?   "0x1000, " : "";
		fString += (value&0x002000)==0x002000 ?   "0x2000, " : "";
		fString += (value&0x004000)==0x004000 ?   "0x4000, " : "";
		fString += (value&0x008000)==0x008000 ?   "0x8000, " : "";
		fString += (value&0x010000)==0x010000 ?  "0x10000, " : "";
		fString += (value&0x020000)==0x020000 ?  "0x20000, " : "";
		fString += (value&0x040000)==0x040000 ?  "0x40000, " : "";
		fString += (value&0x080000)==0x080000 ?  "0x80000, " : "";
		fString += (value&0x100000)==0x100000 ? "0x100000, " : "";
		fString += (value&0x200000)==0x200000 ? "0x200000, " : "";
		fString += (value&0x400000)==0x400000 ? "0x400000, " : "";
		fString += (value&0x800000)==0x800000 ? "0x800000, " : "";
		fString += "]";
		return fString;
	}

	public DDSHeader setSize(int dwWidth, int dwHeight) {
		this.dwWidth = dwWidth;
		this.dwHeight = dwHeight;
		this.dwPitchOrLinearSize = Math.max( 1, ((dwWidth+3)/4) * ((dwWidth+3)/4)) * 8;
//		dwReserved1[0] = ('G') | ('I' << 8) | ('M' << 16) | ('P' << 24);
		dwReserved1[0] = ('T') | ('R' << 8) | ('M' << 16) | ('S' << 24);
		dwReserved1[1] = ('-') | ('D' << 8) | ('D' << 16) | ('S' << 24);
		dwReserved1[2] = 199003;
//		dwReserved1[0] = 'T';
//		dwReserved1[1] = 'R';
//		dwReserved1[2] = 'M';
//		dwReserved1[3] = 'S';
//		dwReserved1[4] = '-';
//		dwReserved1[5] = 'D';
//		dwReserved1[6] = 'D';
//		dwReserved1[7] = 'S';
//		this.dwPitchOrLinearSize = dwWidth*dwHeight;
//		this.dwMipMapCount = 1;
		setDwMipMapCount(1);

		return this;
	}

	public DDSHeader setDwMipMapCount(int dwMipMapCount) {
		this.dwMipMapCount = dwMipMapCount;
		if(dwMipMapCount != 0){
			dwFlags |= 0x20000;
			dwCaps  |= 0x8;
			dwCaps  |= 0x400000;
		}
		return this;
	}

	public DDSHeader setDwPitchOrLinearSize(int dwPitchOrLinearSize) {
		this.dwPitchOrLinearSize = dwPitchOrLinearSize;
		return this;
	}

	public void write(BinaryWriter writer){
		writer.writeUInt32(dwSize);
		writer.writeUInt32(dwFlags);
		writer.writeUInt32(dwHeight);
		writer.writeUInt32(dwWidth);
		writer.writeUInt32(dwPitchOrLinearSize);
		writer.writeUInt32(dwDepth);
		writer.writeUInt32(dwMipMapCount);
		writer.writeInt32Array(dwReserved1);
		ddspf.write(writer);
		writer.writeUInt32(dwCaps);
		writer.writeUInt32(dwCaps2);
		writer.writeUInt32(dwCaps3);
		writer.writeUInt32(dwCaps4);
		writer.writeUInt32(dwReserved2);
	}

	//https://dench.flatlib.jp/ddsformatmemo.html
//	FourCC	    format
//  '1TXD'	    DXT1
//  '2TXD'	    DXT2
//  '3TXD'	    DXT3
//  '4TXD'	    DXT4
//  '5TXD'	    DXT5
//  '2ITA'	    3Dc ATI2	    ATI's Normal compression format
//  0x00000024	A16B16G16R16
//  0x0000006e	Q16W16V16U16
//  0x0000006f	R16F
//  0x00000070	G16R16F
//  0x00000071	A16B16G16R16F
//  0x00000072	R32F
//  0x00000073	G32R32F
//  0x00000074	A32B32G32R32F
//  0x00000075	CxV8U8
//  0x0000003f	Q8W8V8U8	    (nVIDIA tool only DirectX9 texture tool returns bitmask with DDPF_BUMPDUDV)

	// Actual format example
//	format	        dwBitCount	dwRBitMask	dwGBitMask	dwBBitMask	Alpha Mask	dwFourCC	dwPfFlags
//	A8R8G8B8	    32	        0x00ff0000	0x0000ff00	0x000000ff	0xff000000	0	        DDPF_RGB|DDPF_ALPHAPIXELS
//	A8B8G8R8	    32	        0x000000ff	0x0000ff00	0x00ff0000	0xff000000	0	        DDPF_RGB|DDPF_ALPHAPIXELS
//	G16R16	        32	        0x0000ffff	0xffff0000	0x00000000	0x00000000	0	        DDPF_RGB
//	R5G6B5	        16	        0x0000f800	0x000007e0	0x0000001f	0x00000000	0	        DDPF_RGB
//	L16	            16	        0x0000ffff	0x00000000	0x00000000	0x00000000	0	        DDPF_LUMINANCE
//	Q8W8V8U8	    32	        0x000000ff	0x0000ff00	0x00ff0000	0xff000000	0	        DDPF_BUMPUDV
//	DXT1	        0	        0	        0	        0	        0	        "DXT1"	    DDPF_FOURCC
//	A32B32G32R32F	0	        0	        0	        0	        0	        0x00000074	DDPF_FOURCC
//	A8L8	        16	        0x000000ff	0x00000000	0x00000000	0x0000ff00	0	        DDPF_LUMINANCE|DDPF_ALPHAPIXELS

	public class DDS_PIXELFORMAT {
		int dwSize = 32; // size of DDS_PIXELFORMAT
		int dwFlags = 0x4;
//		int dwFourCC = ('D' << 24) | ('X' << 16) | ('T' << 8) | ('5');
//		int dwFourCC = ('D') | ('X' << 8) | ('T' << 16) | ('5' << 24);
		int dwFourCC = ('D') | ('X' << 8) | ('T' << 16) | ('1' << 24);
		int dwRGBBitCount = 0;
		int dwRBitMask = 0;
		int dwGBitMask = 0;
		int dwBBitMask = 0;
		int dwABitMask = 0;
//		int dwRBitMask = 0x00ff0000;
//		int dwGBitMask = 0x0000ff00;
//		int dwBBitMask = 0x000000ff;
//		int dwABitMask = 0xff000000;
		DDS_PIXELFORMAT(){
		}
		DDS_PIXELFORMAT(byte[] header){
			int byteI = 0;
			dwSize          = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
			dwFlags         = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
			dwFourCC        = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
			dwRGBBitCount   = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
			dwRBitMask      = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
			dwGBitMask      = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
			dwBBitMask      = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
			dwABitMask      = ReaderUtils.fromBytes(header[byteI+0], header[byteI+1], header[byteI+2], header[byteI+3]);
			byteI+=4;
		}
		DDS_PIXELFORMAT(int[] header){
			int byteI = 18;
			dwSize          = header[byteI++];
			dwFlags         = header[byteI++];
			dwFourCC        = header[byteI++];
			dwRGBBitCount   = header[byteI++];
			dwRBitMask      = header[byteI++];
			dwGBitMask      = header[byteI++];
			dwBBitMask      = header[byteI++];
			dwABitMask      = header[byteI++];
		}

		public void write(BinaryWriter writer){
			writer.writeUInt32(dwSize);
			writer.writeUInt32(dwFlags);
			writer.writeUInt32(dwFourCC);
			writer.writeUInt32(dwRGBBitCount);
			writer.writeUInt32(dwRBitMask);
			writer.writeUInt32(dwGBitMask);
			writer.writeUInt32(dwBBitMask);
			writer.writeUInt32(dwABitMask);
		}

		@Override
		public String toString() {
			return "DDS_PIXELFORMAT{" +
					"\n\t\tdwSize=" + dwSize +
					"\n\t\tdwFlags=" + dwFlags + ", " + dwFlags() + ", " + flagsOf(dwFlags) +
					"\n\t\tdwFourCC=" + dwFourCC + ", " + CharInt.toString(dwFourCC) +
					"\n\t\tdwRGBBitCount=" + dwRGBBitCount +
					"\n\t\tdwRBitMask=" + dwRBitMask +
					"\n\t\tdwGBitMask=" + dwGBitMask +
					"\n\t\tdwBBitMask=" + dwBBitMask +
					"\n\t\tdwABitMask=" + dwABitMask +
					"\n\t}";
		}

		String dwFlags(){
			String fString = "[";
			fString += (dwFlags&0x000001)==0x000001 ? "DDPF_ALPHAPIXELS, "      : "";
			fString += (dwFlags&0x000002)==0x000002 ? "DDPF_ALPHA, "            : "";
			fString += (dwFlags&0x000004)==0x000004 ? "DDPF_FOURCC, "           : "";
			fString += (dwFlags&0x000008)==0x000008 ? "DDPF_PALETTEINDEXED4, "  : ""; // Palet 16 colors (probably not used in DX9)
			fString += (dwFlags&0x000020)==0x000020 ? "DDPF_PALETTEINDEXED8, "  : ""; // Palette 256 colors
			fString += (dwFlags&0x000040)==0x000040 ? "DDPF_RGB, "              : ""; // Indicates that the format is defined by dwRGBBitCount/dwRBitMask/dwGBitMask/dwBBitMask/dwRGBAlphaBitMask
			fString += (dwFlags&0x000200)==0x000200 ? "DDPF_YUV, "              : "";
			fString += (dwFlags&0x200000)==0x200000 ? "DDPF_LUMINANCE, "        : "";
			fString += (dwFlags&0x800000)==0x800000 ? "DDPF_BUMPUDV, "          : ""; // Indicates that pixel is signed (originally for bump)
			fString += "]";
			return fString;
		}
	};
}
