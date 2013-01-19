package bg.x2d.gen;

/**
 * Represents an object that can generate another type of object usually in a pseudo-random or configurable manner.
 * <br>
 * Generator should be implemented by objects that are intended to be used to generate random results of some type, or generate something else in a procedural and configurable process.
 * It must return whatever type it is parameterized to be.
 * <br>
 * Usage examples: NumberGenerator (in 2DX), PathGenerator, WordGenerator, SequenceGeneratorm, MapGenerator, ListGenerator, ImageGenerator, etc.
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public interface Generator<T> {
	
	public T generate();
}
