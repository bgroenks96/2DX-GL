##The 2DX Project
﻿
###_Open Source Java 2D Graphics Library_



**2DX-GL (Advanced 2-dimensional Graphics Library)**

Extends the Java2D (AWT/Swing) API by adding more high-level functionality for rendering 2D geometry, UIs, effects, animations and physics.  Also aims to provide utility methods for geometric math and general functionality.

**Snapdragon2D**

A 2D Java game engine built on 2DX-GL.  Snapdragon2D utilizes and extends the 2DX-GL API by re-applying its functionality and providing its own infrastructure for game development.  Snapdragon2D aims to squeeze the best quality and performance through manipulation of the Java standard libraries.

**N.B.** Has absolutely no connection to or affiliation with the Qualcomm Snapdragon(tm) mobile processor family.

Notable features:

-Fast, efficient, high level rendering engines (separate Java2D and OpenGL frameworks)
-99% cross-platform with expandable native library system
-2D World management and conversion framework
-2D timed animation framework (Java2D based)
-OpenGL shader support (JOGL)
-Built in image/texture management
-SnapScript language for fast, portable game scripting
-Physics framework (2DX + Snap2D expansion)
-Built in sound library via Paul Lamb's SoundSystem API
-Rendered user interface framework (independent of Swing/AWT)
-Math/geometry libraries
-Numerous provided general utilities

Planned features:

-2D lighting engine for JOGL renderer
-Particle system
-(?) Animation editor and bone system

Official builds are in the 'release_jars' directory and source folders labeled build##.

Code in the primary source folders ('snapdragon' and 'x2d') is under development and may or may not be complete/working.

If you decide to use 2DX/Snap2D for actual application production, I would appreicate it if you let me know on GitHub or by any other means so that I know to be careful about making changes!

The 'builds' folder in the project directory contains the latest development builds.
NOTE: These JARs are typically stable, but may not have all features fully implemented or complete.

Read the commit notes for detailed update information.

###2DX-GL 1st Edition (v.1.0)

Project dev status: Pre-release

To-do (next commit):

-Utility additions
-Physics engine cleanup/bug fixes if necessary

###Snapdragon2D

Project dev status: Alpha

To-do (in development):

-OpenGL rendering engine (via JOGL 2.0 libraries)
-Snapdragon2D physics package (in development)
-Rendered UI (both J2D and JOGL versions)

Follow @The2DXProject on Twitter for real-time updates on development of the project!

###Additional credits:

Snapdragon2D Sound API utilizes SoundSystem for Java by Paul Lamb [www.paulscode.com]
	
OpenGL renderer utilizes JOGL 2.0 by JogAmp [www.jogamp.org] (special thanks to Julien Gouesse for his work and help with JOGL)
