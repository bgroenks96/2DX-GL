package bg.x2d.utils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Provides a Map that maps multiple values to a single key.
 * The actions performed by this class assume the backing arrays to be in sync with each other.
 * If at any point their sizes differ, a ConcurrentModifactionException will be thrown.
 * Note, however, that while this class may, in this nature, be fail-fast, it is not entirely.
 * No checking is implemented for individual value changes within the map, so it is possible that
 * concurrent changes may occur.  It is strongly recommended that instances of this class be synchronized
 * on an external lock object if being used concurrently.
 * @author Brian Groenke
 *
 * @param <K>
 * @param <V>
 */
public class Multimap<K, V> extends AbstractMap<K, V> {

	private static final int KEY_INIT = 0, VAL_INIT = 0, IN_VAL_INIT = 1;

	K[] keys;
	V[][] vals;

	@SuppressWarnings("unchecked")
	public Multimap() {
		keys = (K[]) new Object[KEY_INIT];
		vals = (V[][]) new Object[VAL_INIT][];
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized V put(K key, V value) throws IllegalArgumentException, ConcurrentModificationException {
		if(key == null || value == null)
			throw(new IllegalArgumentException("null values not accepted"));
		V prev = null;
		int prevPos = -1;
		if((prevPos = Arrays.binarySearch(keys, key)) >= 0) {
			V[] varr = vals[prevPos];
			prev = varr[varr.length - 1];
			vals[prevPos] = Utils.resizeArray(varr, varr.length + 1);
			vals[prevPos][vals[prevPos].length - 1] = value;
		} else {
			keys = Utils.resizeArray(keys, keys.length + 1);
			vals = Utils.resizeArray(vals, vals.length + 1);
			keys[keys.length - 1] = key;
			vals[vals.length - 1] = (V[]) Array.newInstance(value.getClass(), IN_VAL_INIT);
			vals[vals.length - 1][0] = value; 
		}
		verify();
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
		int pos = Arrays.binarySearch(keys, key);
		if(pos < 0)
			return null;
		else {
			return vals[pos][0];
		}
	}

	public synchronized V get(Object key, int index) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		int pos = Arrays.binarySearch(keys, key);
		if(pos < 0)
			return null;
		else {
			return vals[pos][index];
		}
	}

	public synchronized V[] getAll(Object key) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		int pos = Arrays.binarySearch(keys, key);
		if(pos < 0)
			return null;
		else {
			return vals[pos];
		}
	}

	@Override
	public synchronized V remove(Object key) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		int pos = Arrays.binarySearch(keys, key);
		if(pos < 0)
			return null;
		else {
			V prev = vals[pos][0];
			keys[pos] = null;
			vals[pos] = null;
			trimArrays();
			verify();
			return prev;
		}
	}

	public synchronized V remove(Object key, Object val) {
		if(key == null)
			throw(new IllegalArgumentException("null values not accepted"));
		int pos = Arrays.binarySearch(keys, key);
		if(pos < 0)
			return null;
		else {
			int vpos = Arrays.binarySearch(vals[pos], val);
			if(vpos < 0)
				return null;
			V prev = vals[pos][vpos];
			vals[pos][vpos] = null;
			trimArrays();
			verify();
			return prev;
		}
	}
	
	/**
	 * @return a Set with all Multimap values COPIED into it.  Changes will not be reflected.
	 */
	@Override
	public Set<V> values() {
		HashSet<V> vset = new HashSet<V>();
		for(V[] varr:vals) {
			for(V v:varr)
				vset.add(v);
		}
		return vset;
	}
	
	/**
	 * @return a Set with all Multimap keys COPIED into it.  Changes will not be reflected.
	 */
	@Override
	public Set<K> keySet() {
		HashSet<K> kset = new HashSet<K>();
		for(K k:keys)
			kset.add(k);
		return kset;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {

		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(int i=0; i<keys.length;i++) {
			K key = keys[i];
			int pos	 = Arrays.binarySearch(keys, key);
			for(int ii=0;ii<vals[pos].length;ii++) {
				V val = vals[pos][ii];
				sb.append(key + "=" + val + ((ii == vals[pos].length - 1 && i == keys.length - 1) ? "":", "));
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/*
	 * Checks for out-of-sync mappings and sorts the key/value arrays.
	 */
	private void verify() throws ConcurrentModificationException {
		if(keys.length != vals.length)
			throw(new ConcurrentModificationException("key/value mappings have become out of sync"));
		Arrays.sort(keys);
		for(V[] varr:vals)
			Arrays.sort(varr);
	}

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
}

