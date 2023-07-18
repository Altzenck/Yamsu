package me.altzenck.yaml;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Util {
	
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
}
