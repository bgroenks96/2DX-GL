package bg.x2d.gen;

/**
 * Generates a random number between the specified bounds using the parameterized number type.
 * <br>
 * This implementation of Generator finds a pseudo-random number between the max and min specified, using a simple algorithm that incorporates Math.random().
 * <br>
 * NumberGenerator will only take number types that are part of the Java(tm) language package (Integer, Double, Long, Float, Short, Byte).
 * @author Brian
 *
 * @param <T>Any of the Java primitive values (through auto-boxing) or their respective class representations.
 * @since 2DX 1.0 (1st Edition)
 */
public class NumberGenerator<T extends Number> implements Generator<T> {
	
	private T lowBound, highBound;
	
	public NumberGenerator(T min, T max) {
		Number d = (Number) min;
		Number d2 = (Number) max;
		
		if(d.doubleValue() < d2.doubleValue()) {
			lowBound = min;
			highBound = max;
		} else {
			throw new IllegalArgumentException("Minimum value must be less than maximum value.");
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T generate() {
		double low = ((Number)lowBound).doubleValue();
		double high = ((Number)highBound).doubleValue();
		double diff = high - low;
		Double result = low + (Math.random()*diff);
		if(lowBound instanceof Double) return (T) result;
		else if(lowBound instanceof Integer) return (T) new Integer(result.intValue());
		else if(lowBound instanceof Long) return (T) new Long(result.longValue());
		else if(lowBound instanceof Short) return (T) new Short(result.shortValue());
		else if(lowBound instanceof Byte) return (T) new Byte(result.byteValue());
		else if(lowBound instanceof Float) return (T) new Float(result.floatValue());
		else throw(new NumberFormatException("NumberGenerator only supports the Java(tm) language Number (java.lang.Number) types."));
	}
}
