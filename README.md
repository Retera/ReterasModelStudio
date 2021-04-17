# Reteras Model Studio
This repository contains the sourcecode for the Reteras Model Studio application, which is available for download on the Hive Workshop.
![Image of ReterasModelStudio](https://www.hiveworkshop.com/data/ratory-images/159/159964-e1b13fddb241fe69a198f443b00b1637.png)

Reteras Model Studio is a Java-based Warcraft III model file editor that supports both the legacy 2003 MDX model format and game as well as the new Warcraft III Reforged MDX model files.

# Building
1. Download an IDE that supports gradle. For this project I suggest Eclipse 2018-09. (https://www.eclipse.org/downloads/packages/release/2018-09/r)
2. Clone the repository from https://github.com/Retera/ReterasModelStudio.git (you can either download the ZIP, or use git)
3. Launch the downloaded Eclipse and follow the prompt to create an Eclipse workspace on any location on your hard drive separate from the ReterasModelStudio repository
4. In Eclipse, close the Welcome popup and go to File -> Import -> Gradle
5. This launches a wizard where you can choose to import the ReterasModelStudio project. In general it should be fine to use the default settings, including the project's own gradle wrapper.
6. Press finish on the gradle import when you are ready. This should load a view with the code on the left. After a few seconds, if all is well, indications of any red X boxes or other compile errors should go away.
8. Find the file in the retera-jwc3-matrixeater sub project located at src/com.matrixeater.src called MainFrame.java and double click this to open it.
9. Press the green play button in the top toolbar, and choose "Java Application" in the popup window.

If all is well, this should launch a locally compiled build of Retera Model Studio where you have full code access and can change any program behavior that you would like.

# Credits
(More details are available in Help -> About inside the program itself)

## Ghostwolf (http://github.com/flowtsohg/)
 - Added FBX support with jassimp
 - Reorganized and improved the codebase
 - Integrated Retera's transcription of Ghostwolf's mdx-m3-viewer's MDX/MDL code into this project and did cleaning/bugfixes
 - OpenGL rendering logic for Warcraft III model files (from mdx-m3-viewer)
 - Reforged MDX specifications for adding Reforged support
 - Other tips & suggestions for handling WC3 MDX files and rendering

## DrSuperGood (https://github.com/DrSuperGood)
 - blp-iio-plugin, foundational for this program to load BLP format
 - Java-only MPQ parser for legacy game data loading
 - JCASC for Reforged game data loading

## Oger-Lord (https://github.com/OgerLord)
 - Java-only MDX parser for loading legacy models (upgraded by Retera to be feature complete and support Reforged MDX models)
 - Java-only TGA parser

## Golden Gnu (https://github.com/GoldenGnu/)
 - Java-only DDS parser

## PitzerMike
 - Warcraft III object data parsing in C++ which Retera ported to Java to help preview in-game units for quick browsing to find models
 
## seanrowens (https://github.com/seanrowens)
 - OBJ parser for java

## twilac
 - Feature improvements
 - Material management in the "Model" tab in places
 - Some code reorganization on 0.05.

 
## BogdanW3
 - Some bugfixes (see GitHub Contributors)

## Testers & Idea Guys
Hayate, CanFight, Unryze, Rigborn, Jaccouille, Max, Mayday, Deolrin, Templier777, Macadamia, Mechanic, Moonman, P4RI4H, Razorclaw_X, all the people who posted on the Retera Model Studio Reforged thread on Hiveworkshop, and the growing list of people who joined the Retera Model Studio Users Group discord server that I created to motivate myself to start getting more bugs fixed.

## Special Mentions
Shadow Daemon: BLP lab command line version for certain automated tasks (now mostly phased out and replaced with DrSuperGood BLP plugin)
Terai Atsuhiro and Jay Warrick: Original Matrix Eater's tabbing system, which I originally found on a StackOverflow post they had made (now replaced by InfoNode Docking Windows)
LWJGL: Used for java OpenGL bindings
TimoHanisch, Deaod, and contributors to JStormLib on GitHub: used for MPQ parsing in past versions of the program (now phased out and replaced with DrSuperGood MPQ)
Deaod: Warcraft III unit data parser made for his ODE JNGP plugin, which was used for the unit previewing logic (now mostly phased out and replaced with Java port of PitzerMike's code)

