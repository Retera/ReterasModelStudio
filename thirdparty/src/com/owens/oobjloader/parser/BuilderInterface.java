package com.owens.oobjloader.parser;

public interface BuilderInterface {

    int NO_SMOOTHING_GROUP = 0;
    int EMPTY_VERTEX_VALUE = Integer.MIN_VALUE;
    int MTL_KA = 0;
    int MTL_KD = 1;
    int MTL_KS = 2;
    int MTL_TF = 3;
    int MTL_MAP_KA = 0;
    int MTL_MAP_KD = 1;
    int MTL_MAP_KS = 2;
    int MTL_MAP_NS = 3;
    int MTL_MAP_D = 4;
    int MTL_DECAL = 5;
    int MTL_DISP = 6;
    int MTL_BUMP = 7;
    int MTL_REFL_TYPE_UNKNOWN = -1;
    int MTL_REFL_TYPE_SPHERE = 0;
    int MTL_REFL_TYPE_CUBE_TOP = 1;
    int MTL_REFL_TYPE_CUBE_BOTTOM = 2;
    int MTL_REFL_TYPE_CUBE_FRONT = 3;
    int MTL_REFL_TYPE_CUBE_BACK = 4;
    int MTL_REFL_TYPE_CUBE_LEFT = 5;
    int MTL_REFL_TYPE_CUBE_RIGHT = 6;

    void setObjFilename(String filename);

    void addVertexGeometric(float x, float y, float z);

    void addVertexTexture(float u, float v);

    void addVertexNormal(float x, float y, float z);

    void addPoints(int[] values);

    void addLine(int[] values);

    // The param for addFace is an array of vertex indices.   This array should have a length that is a multiple of 3.  
    //
    // For each triplet of values;
    //
    // The first value is an absolute or relative index to a geometric vertex. (VertexGeometric)
    // The second value is an absolute or relative index to a vertex texture coordinate. (VertexTexture)
    // The third vertex is an absolute or relative index to a vertex normal.  (VertexNormal)
    //
    // The indices for the texture and normal MAY be empty in which case they will be set equal to the special
    // value defined in BuilderInterface, EMPTY_VERTEX_VALUE.
    //
    // Absolute indices are positive values that specify a vertex/texture/normal by it's absolute position within the OBJ file.
    //
    // Relative indices are negative values that specify a vertex/texture/normal by it's position relative to the line the index
    // is on, i.e. a line specifying a face (triangle) may specify an geometry vertex as -5 which means the 5 most recently seen 
    // geometry vertex.
    void addFace(int[] vertexIndices);

    void addObjectName(String name);

    void addMapLib(String[] names);

    void setCurrentGroupNames(String[] names);

    void setCurrentSmoothingGroup(int groupNumber);

    void setCurrentUseMap(String name);

    void setCurrentUseMaterial(String name);

    void newMtl(String name);

    void setXYZ(int type, float x, float y, float z);

    void setRGB(int type, float r, float g, float b);

    void setIllum(int illumModel);

    void setD(boolean halo, float factor);

    void setNs(float exponent);

    void setSharpness(float value);

    void setNi(float opticalDensity);

    void setMapDecalDispBump(int type, String filename);

    void setRefl(int type, String filename);

    void doneParsingMaterial();

    void doneParsingObj(String filename);
}