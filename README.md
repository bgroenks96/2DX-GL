###The 2DX Project - _Open Source 2D Java Graphics Library_

The 2DX Project aims to provide powerful, versatile, and felxible tools for 2D graphics programming in Java.

**2DX-GL (Advanced 2-dimensional Graphics Library)**

A standalone, dependency free library that extends the Java2D AWT API by adding more high-level functionality for rendering 2D geometry, effects, animations, physics, and general utilities.

**Snap2D**

A 2D Java game engine built on 2DX-GL.  Snap2D utilizes and extends the 2DX-GL API by re-applying its functionality and providing its own infrastructure for game development.  Snap2D aims to provide high performance 2D graphics rendering as well as a wide range of facilities for building games in Java.

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

-Integration with NiftyGUI provides support for building effective, high-performance user interfaces

-Math/geometry libraries

-Numerous provided general utilities

Planned features:

-2D lighting engine for JOGL renderer

-Particle system

-(?) Animation editor and bone system

Official builds are in the 'release_jars' directory and source folders labeled build##.

Code in the primary source folders ('snapdragon' and 'x2d') is under development and may or may not be complete/working.

If you decide to use 2DX/Snap2D for actual application production, I would appreicate it if you let me know on GitHub or by any other means so that I know to be careful about making changes!

The 'builds' folder in the project directory contains the latest development builds.  All JARs in this folder include both the base 2DX-GL and Snap2D libraries.

_NOTE: These JARs are typically stable, but may not have all features fully implemented or complete._

Read the commit notes for detailed update information.

**2DX-GL 1st Edition (v.1.0)**

Project dev status: API-Stable

To-do (next commit):

-Utility additions

-Physics engine cleanup/bug fixes if necessary

**Snap2D**

Project dev status: API-Alpha

To-do (in development):

-OpenGL rendering engine (via JOGL 2.0 libraries)

-Snap2D physics package (in development)

-Rendered UI (both J2D and JOGL versions)

Follow @The2DXProject on Twitter for real-time updates on development of the project!

**Additional credits:**

Snap2D Sound API utilizes SoundSystem for Java by Paul Lamb [www.paulscode.com]

Snap2D integrates with NiftyGUI by void256 [http://nifty-gui.lessvoid.com/]
	
OpenGL renderer utilizes JOGL 2.0 by JogAmp [www.jogamp.org]

**Special Thanks**

Julien Gouesse and Sven Gothel (JogAmp)
