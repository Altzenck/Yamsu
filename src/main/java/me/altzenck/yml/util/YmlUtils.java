package me.altzenck.yml.util;

import com.google.common.base.Preconditions;
import me.altzenck.util.ArrayMap;
import me.altzenck.util.ArraySet;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YmlUtils {

	public String join(int to, String... elements) {return join(0, to, null, elements);}

	public String join(int from, int to, String... elements) {return join(from, to, null, elements);}

	public static String join(int from, int to, @Nullable String l, @Nonnull String... elements) {
		if(l == null) l = " ";
		String text = "", ns[] = arrayRange(from, to, elements);
		for(int i = 0; i < ns.length; i++) 
			text += ((i == 0)? "": l) + ns[i];
		return text;
	}

	public static String[] arrayRange(int to, String... elements) {return arrayRange(0, to, elements);}

	public static String[] arrayRange(int from, int to, @Nonnull String... elements) {
		if(to < 0) to = elements.length + to;
		return Arrays.copyOfRange(elements, from, to);
	}

	protected static String[] splitRange(int to, String text, String regex) {return splitRange(0, to, text, regex);}

	protected static String[] splitRange(int from, int to, @Nonnull String text, String regex) {
		return arrayRange(from, to, text.split(regex));
	}

	public static <E> E removeLast(List<E> l) {
		return l.remove(l.size()-1);
	}

	public static String stringJoinFromObject(CharSequence de, Object... elements) {
		String n = "";
		for(Object o: elements) {
			if(o instanceof Iterable) {
				Iterator<?> o1 = ((Iterable<?>) o).iterator();
				while(o1.hasNext()) 
					n += ((n.isBlank())? "": de) + o1.next().toString();
				continue;
			}
			n += ((n.isBlank())? "": de) + o.toString();
		}
		return n;
	}

	public static Number asNumber(Object o) {
		return asNumber(o, Number.class);
	}

	public static Number asNumber(Object o, Class<? extends Number> clazz) {
		Number num = new NumberReference(o);
		if(clazz == Integer.class) {
			return num.intValue();
		}
		if(clazz == Long.class) {
			return num.longValue();
		}
		if(clazz == Double.class) {
			return num.doubleValue();
		}
		if(clazz == Float.class) {
			return num.floatValue();
		}
		if(clazz == Short.class) {
			return num.shortValue();
		}
		if(clazz == Byte.class) {
			return num.byteValue();
		}
		if(clazz == Number.class) {
			return num;
		}
		throw new IllegalArgumentException("clazz");
	}

	private static class NumberReference extends Number {

		private Number number = 0;

		NumberReference(Object o) {
			try {
				number = (o instanceof Number)? (Number) o : NumberFormat.getInstance().parse(o.toString());
			} catch (ParseException ignored) {
			}
		}

		@Override
		public int intValue() {
			return number.intValue();
		}

		@Override
		public long longValue() {
			return number.longValue();
		}

		@Override
		public float floatValue() {
			return number.floatValue();
		}

		@Override
		public double doubleValue() {
			return number.doubleValue();
		}

		@Override
		public short shortValue() {
			return number.shortValue();
		}

		@Override
		public byte byteValue() {
			return super.byteValue();
		}
	}

	public static void requireNotVoid(String s) {
		Preconditions.checkArgument(s != null && !s.isBlank());
	}

	public static Object deepClone(Object obj) {
		if (obj == null) return null;
		if (isStringOrPrimitive(obj)) return obj;

		if (obj instanceof Map) {
			Map<Object, Object> clonedMap = new ArrayMap<>();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet())
				clonedMap.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
			return clonedMap;
		}

		if (obj instanceof List) {
			List<Object> clonedList = new ArrayList<>();
			for (Object item : (List<?>) obj)
				clonedList.add(deepClone(item));
			return clonedList;
		}

		if (obj instanceof Set) {
			Set<Object> clonedSet = new ArraySet<>();
			for (Object item : (Set<?>) obj)
				clonedSet.add(deepClone(item));
			return clonedSet;
		}
		try {
			return cloneCustomObject(obj);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Cloning Object: " + obj, e);
		}
	}

	public static boolean isStringOrPrimitive(Object obj) {
		return obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character;
	}

	private static Object cloneCustomObject(Object original) throws ReflectiveOperationException {
		Class<?> clazz = original.getClass();
		Object clone = clazz.getDeclaredConstructor().newInstance();

		for (var field : clazz.getDeclaredFields()) {
			field.setAccessible(true);

			Object fieldValue = field.get(original);
			field.set(clone, deepClone(fieldValue));
		}

		return clone;
	}
}