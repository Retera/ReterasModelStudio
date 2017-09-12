# JWC3
Retera's Java WC3 libraries.

Before using this repository, you should be aware that other similar repos
such as the following exist:
https://github.com/OgerLord/WcDataLibrary

And, in fact, this repo contains older versions of the code found at the above
link, repackaged and then modified to convert from OgreLord's MDX classes into
my old MDL classes that I wrote back in high school.

So, basically, this repo in its current state is one step in a much larger
process -- the sharing of code -- in which one central Warcraft Modding Data
repo should eventually come in to being, so that independent authors
don't do silly things (like me!) by copying other peoples'
code repackaged and outdated.

# Contents

## HayatesCharacterEngine
This is a program written based on a concept drawing from Hayate, suggesting
a program that could create new unit models in only a few clicks.
It is not finished, but can produce models that work and look interesting
in some cases.

## JWC3
This is a bunch of Warcraft-related libraries gathered together.
Contains a sparse amount of repackaged code from other authors,
combined with Retera-authored utilities to use that code.
The MDL class and its associated package and libraries were written
by Retera in high school when writing the original Matrix Eater
software that used no libraries.

## JWorldEdit
This was a basic start on a Warcraft III world editor implementation
in Java. It's not too far along, but contains some interesting code.

## JWC3-MatrixEater
This is an implementation of the Matrix Eater 3D modeling software
that relies on both the JWC3 project and on the JWorldEdit project.

## RealityInteractive
RealityInteractive stuff is from somewhere online and is not used in the
main Matrix Eater software. You can find it online, and although it was
checked in lazily for building, it is not written by me.
Follow its original license agreement information for info on its origins.

## HayatesCharacterEngine
This was an attempt to build a humanoid character builder
that generates WC3 models from a few simple inputs. If somebody upgraded it,
and loaded in more templates, it might become even more powerful than it is.

## JesusHipsterAttachmentSystem
This is available on the Hive Workshop website for download in a production
version if you search for it. It allows you to add customizable models onto
an attachment model and stuff, coded by Retera but all following the ideas
of JesusHipster in terms of how it operated on models.

## MDXLib
This is something old, and I'm not sure what it is. You could look through it
but it isn't going to be as useful as the MDX and MDL utilities found inside
the JWC3 project.

## MatrixEater3D
This is an older version of the project that moved to JWC3-MatrixEater. It probably
does not depend on the JWC3 project in the same way, but also would be much older
versions of the code.

## ModCompiler
I don't remember what this folder is, and I never finished whatever I was working
on inside it. I believe it was intended for helping to combine my Warcraft mod,
Heaven's Fall, which is still under development.

## ODE-master
This is a repackaged version of Deaod's code for the Object Data Extractor
plugin to the Jass NewGen Pack. However, I repurposed it to load unit data
inside my Java code. I don't think I edited it very much, but it needs
to be included to build the AssetExtractor project.

## JWC3-AssetExtractor
This is a weird program that I accidentally checked in that I wrote for a friend
which basically can extract any unit models, portraits, textures, icons, etc. from
Warcraft 3 maps and mod MPQs, and is notable in that it does not use the listfile,
instead parsing the data as though it were the Warcraft 3 game itself, so it can
open any "protected map" and extract the models from them. That's pretty lame,
don't steal people's stuff. The point of that technology is to load source files
from the Warcraft 3 game engine, created by Blizzard, to help fans author new
fan-made models based on Blizzard Entertainment content. Don't use this program
to extract models from maps you did not receive permission to edit. It has a
warning about this on maps that have no listfile, so it tells you that you better
know what you are doing ethically before performing the extraction in that case.

## JWC3-Scripts
Specific case scripts that I wrote for specific circumstances, when I just wanted some
scripts with the JWC3 library included. This probably is not going to be useful
outside of those specific cases that I wanted them for.

## mpqlib
I haven't been compiling against this folder for some time. It is an outdated MPQ library
that is not as good as the one written by DrSuperGood. If you want an MPQ library, I suggest
the DrSuperGood library which is the one included in JWC3. In fact, you would be better off
to go track down that guy, and get the latest version of his stuff, because mine repackaged
version is years old.
