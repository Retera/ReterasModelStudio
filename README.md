# Reteras Model Studio
This repository contains the sourcecode for the Reteras Model Studio application, which is available for download on the Hive Workshop.
![Image of ReterasModelStudio](https://www.hiveworkshop.com/data/ratory-images/159/159964-e1b13fddb241fe69a198f443b00b1637.png)

Reteras Model Studio is a Java-based Warcraft III model file editor that supports both the legacy 2003 MDX model format and game as well as the new Warcraft III Reforged MDX model files.

# Credits
(More details are available in Help -> About inside the program itself)

## Ghostwolf (http://github.com/flowtsohg/)
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

## Testers & Idea Guys
Hayate, CanFight, Unryze, Rigborn, Jaccouille, Max, Mayday, Deolrin, Templier777, Macadamia, Mechanic, Moonman, P4RI4H, and the growing list of people who joined the Retera Model Studio Users Group discord server that I created to motivate myself to start getting more bugs fixed.

## Special Mentions
Shadow Daemon: BLP lab command line version for certain automated tasks (now mostly phased out and replaced with DrSuperGood BLP plugin)
Terai Atsuhiro and Jay Warrick: Original Matrix Eater's tabbing system, which I originally found on a StackOverflow post they had made (now replaced by InfoNode Docking Windows)
LWJGL: Used for java OpenGL bindings
TimoHanisch, Deaod, and contributors to JStormLib on GitHub: used for MPQ parsing in past versions of the program (now phased out and replaced with DrSuperGood MPQ)

