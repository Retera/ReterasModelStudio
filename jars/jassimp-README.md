Prerequisites:
1. VS command line tools (or port the compilation command to G++ etc.)
2. CMake
3. Assimp https://github.com/assimp/assimp
4. JDK

Natives:
1. Configure CMake (e.g. with cmake-gui):
    BUILD_SHARED_LIBS = false
    ASSIMP_BUILD_ALL_IMPORTERS_BY_DEFAULT = false
    ASSIMP_NO_EXPORT = true
    ASSIMP_BUILD_OBJ_IMPORTER = true
    ASSIMP_BUILD_FBX_IMPORTER = true
2. Generate the files with CMake.
3. Build a release version of Assimp.
4. Build a release version of the included zlib under `contrib/zlib`.
5. Copy the resulting `lib` files to `port/jassimp/jassimp-native`.
6. Run the VS CLI (note there are 32bit and 64bit versions), and navigate to `jassimp-native`.
7. `cl /Fe"jassimp-natives64.dll" /EHsc /O1 src/jassimp.cpp assimp-vc141-mt.lib zlibstatic.lib /I %JAVA_HOME%\include /I %JAVA_HOME%\include\win32 /I ..\..\..\include /MD /LD`
8. `jar cf jassimp-natives.jar jassimp-natives64.dll`
9. Move `jassimp-natives.jar` into `ReterasModelStudio/jars`.

Bindings:
1. Go to `jassimp/jassimp/src`.
2. Open `jassimp/JassimpLibraryLoader.java`, and comment the `System.loadLibrary` call.
2. `javac -d build jassimp/*`
3. `cd build`
4. `jar cf jassimp.jar jassimp/*`
5. Move `jassimp.jar` into `ReterasModelStudio/jars`.
