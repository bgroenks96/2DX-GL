package bg.x2d.utils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Provides a Map that maps multiple values to a single key.  This map is backed by a
 * Hashtable that maps a single key to a dynamic array of values.  All read/write methods of Multimap
 * are synchronized so objects may be modified asynchronously by multiple threads.
 * @author Brian Groenke
 * @param <K> the type for keys
 * @param <V> the type for values
 */
public class Multimap<K, V> extends AbstractMap<K, V> {

	private static final int KEY_INIT = 0, VAL_INIT = 0, IN_VAL_INIT = 1;

	Hashtable<K, V[]> ktv;

	@SuppressWarnings("unchecked")
	public Multimap() {
		ktv = new Hashtable<K, V[]>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized V put(K key, V value) throws IllegalArgumentException, ConcurrentModificationException {
		if(key == null || value == null)
			throw(new IllegalArgumentException("null values not accepted"));
		V prev = null;
		V[] varr = ktv.get(key);
		if(varr != null) {
			if(findValueIndex(value, varr) < 0) {
				varr = append(varr, value);
			}
		} else {
			varr = (V[]) Array.newInstance(value.getClass(), 1);
			varr[0] = value;
		}

		ktv.put(key, varr);
		return prev;
	}

	public synchronized void putAll(K key, V... values) {
		for(V v:values)
			put(key, v);
	}



	@Override
	public synchronized V get(Object key) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		V[] varr = ktv.get(key);
		if(varr != null)
			return varr[0];
		else
			return null;
	}

	public synchronized V get(Object key, int index) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		V[] varr = ktv.get(key);
		if(varr != null)
			return varr[index];
		else
			return null;
	}

	public synchronized V[] getAll(Object key) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		return ktv.get(key);
	}

	@Override
	public synchronized V remove(Object key) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		return ktv.remove(key)[0];
	}

	public synchronized V remove(Object key, Object val) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		V[] varr = ktv.get(key);
		V prev = null;
		if(varr != null) {
			int ind = findValueIndex((V)val, varr);
			if(ind >= 0) {
				prev = varr[ind];
				varr[ind] = null;
				trimArray(varr, val.getClass(), varr.length - 1);
			}
		}
		
		return prev;
	}

	/**
	 * @return a Set with all Multimap values COPIED into it.  Changes will not be reflected.
	 */
	@Override
	public synchronized Set<V> values() {
		HashSet<V> vset = new HashSet<V>();
		for(V[] varr:ktv.values()) {
			for(V v:varr)
				vset.add(v);
		}
		return vset;
	}

	/**
	 * @return a Set with all Multimap keys COPIED into it.  Changes will not be reflected.
	 */
	@Override
	public synchronized Set<K> keySet() {
		HashSet<K> kset = new HashSet<K>();
		for(K k:ktv.keySet())
			kset.add(k);
		return kset;
	}

	@Override
	public synchronized Set<java.util.Map.Entry<K, V>> entrySet() {
		return null;
	}
	
	@Override
	public int size() {
		return values().size();
	}
	
	@Override
	public void clear() {
		ktv.clear();
	}

	@Override
	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(K key:ktv.keySet()) {
			sb.append(key + "=");
			for(V val:ktv.get(key)) {
				sb.append(val + ", ");
			}
			sb.deleteCharAt(sb.length() - 2);
		}
		sb.append("}");
		return sb.toString();
	}

	private int findValueIndex(V val, V[] vset) {
		for(int i=0; i < vset.length; i++) {
			if(vset[i].equals(val))
				return i;
		}

		return -1;
	}
	
	/*
	 * Appends the given object onto the existing array by allocating a new array with +1 size.
	 */
	private V[] append(V[] varr, V val) {
		V[] narr = Arrays.copyOf(varr, varr.length + 1);
		narr[narr.length - 1] = val;
		return narr;
	}

	/*
	 * Copies the contents of the array into a new smaller array, omitting any null values.
	 */
	private V[] trimArray(V[] arr, Class<?> type, int newSize) {
		if(newSize >= arr.length)
			throw(new IllegalArgumentException("new size must be less than previous"));
		V[] narr = (V[]) Array.newInstance(type, newSize);
		for(int i=0;i<narr.length;i++) {
			if(arr[i] != null)
				narr[i] = arr[i];
		}
		
		return narr;
	}

	/*
	private void trimArrays() {
		int nulls = 0;
		for(K k:keys)
			if(k == null)
				nulls++;
		for(int i=0;i<keys.length;i++) {
			if(keys[i] == null) {
				if(i+1 < keys.length) {
					int ii = 1;
					while(i + ii < keys.length && keys[i + ++ii] == null);
					if(i + ii < keys.length) {
						keys[i] = keys[i + ii];
						keys[i + ii] = null;
					}
				}
			}
		}
		if(keys.length > 0)
			keys = Utils.resizeArray(keys, keys.length - nulls);

		nulls = 0;
		for(V[] varr:vals)
			if(varr == null)
				nulls++;
		for(int i=0;i<vals.length;i++) {
			if(vals[i] == null) {
				if(i+1 < vals.length) {
					int ii = 1;
					while(i + ii < vals.length && vals[i + ++ii] == null);
					if(i + ii < vals.length) {
						vals[i] = vals[i + ii];
						vals[i + ii] = null;
					}
				}
			} else {
				int nullCount = 0;
				V[] varr = vals[i];
				for(V v:varr)
					if(v == null)
						nullCount++;
				for(int ii=0;ii<varr.length;ii++) {
					if(varr[ii] == null) {
						if(ii+1 < varr.length) {
							int iii = 0;
							while(ii + ++iii < varr.length && varr[ii + iii] == null);
							if(ii + iii < varr.length) {
								varr[ii] = varr[ii + iii];
								varr[ii + iii] = null;
							}
						}
					}
				}
				vals[i] = Utils.resizeArray(vals[i], vals[i].length - nullCount);
			}
		}

		if(vals.length > 0)
			vals = Utils.resizeArray(vals, vals.length - nulls);
	}
	 */
}

