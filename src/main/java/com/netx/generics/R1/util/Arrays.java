package com.netx.generics.R1.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.Array;

import com.netx.basic.R1.eh.Checker;


public class Arrays {

	private Arrays() {
		super();
	}

	public static int find(Object o, Object[] in) {
		Checker.checkNull(o, "o");
		Checker.checkNull(in, "in");
		for(int i=0; i<in.length; i++) {
			if(o.equals(in[i])) {
				return i;
			}
		}
		return -1;
	}

	public static <T> List<T> toList(T[] array) {
		Checker.checkNull(array, "array");
		List<T> list = new ArrayList<T>();
		for(int i=0; i<array.length; i++) {
			list.add(array[i]);
		}
		return list;
	}
	
	public static Object[] toArrayOf(Class<?> c, Object[] array) {
		Checker.checkNull(c, "c");
		Checker.checkNull(array, "array");
		Object[] result = (Object[])Array.newInstance(array.getClass().getComponentType(), array.length);
		for(int i=0; i<array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}
	
	public static Object[] toArrayOf(java.lang.Class<?> c, Iterator<?> it) {
		Checker.checkNull(c, "c");
		Checker.checkNull(it, "it");
		LinkedList<Object> list = new LinkedList<Object>();
		while(it.hasNext()) {
			list.add(it.next());
		}
		return toArrayOf(c, list.toArray());
	}

	public static Object[] toArrayOf(java.lang.Class<?> c, Iterable<?> it) {
		Checker.checkNull(it, "col");
		return toArrayOf(c, it.iterator());
	}

	public static Object[] toObjectArray(boolean[] array) {
		Object[] objArray = new Object[array.length];
		for(int i=0; i<array.length; i++) {
			objArray[i] = new Boolean(array[i]);
		}
		return objArray;
	}

	public static Object[] toObjectArray(char[] array) {
		Object[] objArray = new Object[array.length];
		for(int i=0; i<array.length; i++) {
			objArray[i] = new Character(array[i]);
		}
		return objArray;
	}

	public static Object[] toObjectArray(short[] array) {
		Object[] objArray = new Object[array.length];
		for(int i=0; i<array.length; i++) {
			objArray[i] = new Short(array[i]);
		}
		return objArray;
	}

	public static Object[] toObjectArray(int[] array) {
		Object[] objArray = new Object[array.length];
		for(int i=0; i<array.length; i++) {
			objArray[i] = new Integer(array[i]);
		}
		return objArray;
	}

	public static Object[] toObjectArray(long[] array) {
		Object[] objArray = new Object[array.length];
		for(int i=0; i<array.length; i++) {
			objArray[i] = new Long(array[i]);
		}
		return objArray;
	}

	public static Object[] toObjectArray(float[] array) {
		Object[] objArray = new Object[array.length];
		for(int i=0; i<array.length; i++) {
			objArray[i] = new Float(array[i]);
		}
		return objArray;
	}

	public static Object[] toObjectArray(double[] array) {
		Object[] objArray = new Object[array.length];
		for(int i=0; i<array.length; i++) {
			objArray[i] = new Double(array[i]);
		}
		return objArray;
	}

	public static <T> T[] concat(T[] array1, T[] array2) {
		Checker.checkNull(array1, "array1");
		Checker.checkNull(array2, "array2");
		List<T> result = new ArrayList<T>();
		for(T elem : array1) {
			result.add(elem);
		}
		for(T elem : array2) {
			result.add(elem);
		}
		// toArray may be used with array1, because it's
		// not big enougth to hold the results of concat.
		return result.toArray(array1);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] subtract(T[] array1, T[] array2) {
		List<T> result = new ArrayList<T>();
		for(int i=0; i<array2.length; i++) {
			boolean found = false;
			for(int j=0; j<array1.length; j++) {
				if(array1[j].equals(array2[i])) {
					found = true;
					break;
				}
			}
			if(!found) {
				result.add(array2[i]);
			}
		}
		return (T[])result.toArray((Object[])Array.newInstance(array1.getClass().getComponentType(), 0));
	}

	public static <T> void copy(T[] from, int fromIndex, T[] to, int toIndex, int length) {
		Checker.checkNull(to, "to");
		Checker.checkNull(from, "from");
		Checker.checkIndex(toIndex, "toIndex");
		Checker.checkIndex(fromIndex, "fromIndex");
		Checker.checkIndex(length, "length");
		if(toIndex+length > to.length) {
			throw new IndexOutOfBoundsException("index: "+(toIndex+length)+"; array length: "+to.length);
		}
		if(fromIndex+length > from.length) {
			throw new IndexOutOfBoundsException("index: "+(fromIndex+length)+"; array length: "+from.length);
		}
		System.arraycopy(from, fromIndex, to, toIndex, length);
	}

	public static <T> void copy(T[] from, int fromIndex, T[] to, int toIndex) {
		copy(from, fromIndex, to, toIndex, from.length);
	}

	public static <T> void copy(T[] from, T[] to) {
		copy(from, 0, to, 0);
	}

}