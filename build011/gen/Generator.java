/*
 * Copyright © 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
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
