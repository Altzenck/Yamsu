package me.altzenck.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

	public static Integer asInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
}