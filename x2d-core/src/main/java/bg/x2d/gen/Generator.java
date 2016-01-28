/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

/**
 * Contains classes relating to generic type Object generation.
 */
package bg.x2d.gen;

/**
 * Represents an object that can generate another type of object usually in a
 * pseudo-random or configurable manner. <br>
 * Generator should be implemented by objects that are intended to be used to
 * generate random results of some type, or generate something else in a
 * procedural and configurable process. It must return whatever type it is
 * parameterized to be. <br>
 * Usage examples: NumberGenerator (in 2DX), PathGenerator, WordGenerator,
 * SequenceGeneratorm, MapGenerator, ListGenerator, ImageGenerator, etc.
 * 
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public interface Generator<T> {

    public T generate();
}
