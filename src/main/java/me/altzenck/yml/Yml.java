package me.altzenck.yml;

import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import me.altzenck.util.ReflectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import me.altzenck.io.IOUtils;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author Altzenck
 * @version 2.0-SNAPSHOT
 */
public class Yml extends MemorySection {

   private final Yaml snakeYaml;

   private Yml(Reader reader) {
	   super(null, "");
	   AtomicReference<Yaml> refSnakeYaml = new AtomicReference<>();
	   ReflectionUtils.getDeclaredField(MemorySection.class, this, "map").set(load(refSnakeYaml, reader));
	   snakeYaml = refSnakeYaml.get();
   }

   public void save(@Nonnull File file) {
	   file.getParentFile().mkdirs();
	   if(file.isDirectory()) throw new IllegalArgumentException("The specified file is a directory!");
	   if(file.exists()) file.delete();
	   try {
		StringReader in = new StringReader(snakeYaml.dump(map));
	    FileOutputStream os = new FileOutputStream(file);
		IOUtils.copyBytes(in, os, 2048);
	   } catch (IOException e) {
		e.printStackTrace();
	   }
   }

   public static Yml loadYaml(@Nonnull File file) {
	   try {
		   return loadYaml(new FileInputStream(file));
	   } catch (IOException e) {
		   e.printStackTrace();
		   return loadYaml(new StringReader(""));
	   }
   }

   public static Yml loadYaml(@Nonnull Reader reader) {
	    return new Yml(reader);
   }

   public static Yml loadYaml(@Nonnull InputStream is) {
       return loadYaml(new InputStreamReader(is));
   }

   public Yml withDefaultValues(File file) {
	   try {
		   return withDefaultValues(new FileInputStream(file));
	   } catch (IOException e) {
		   e.printStackTrace();
	   }
	   return this;
   }

	protected static Map<String, Object> load(AtomicReference<Yaml> refSnakeYaml, Reader reader) {
		LoaderOptions loaderOptions = new LoaderOptions();
		loaderOptions.setProcessComments(true);
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setProcessComments(true);
		dumperOptions.setIndent(2);
		dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		refSnakeYaml.set(new Yaml(new Constructor(loaderOptions), new Representer(dumperOptions), dumperOptions, loaderOptions));
		return refSnakeYaml.get().load(reader);
	}

	public Yml withDefaultValues(InputStream is) {
		return withDefaultValues(new InputStreamReader(is));
	}

	public Yml withDefaultValues(Reader reader) {
	    Map<String, Object> load = load(new AtomicReference<>(), reader);
		DefaultSection ds = new DefaultSection(load, "");
		ReflectionUtils.getDeclaredField(MemorySection.class, this, "def").set(ds);
	    return this;
	}

	private void setEmptyDefaultValue() {
	    withDefaultValues(new StringReader(""));
	}

	public Yml withCopyDefaultValues() {
	    setEmptyDefaultValue();
	    setDefaults(this);
	    return this;
	}

	public Yml withPathSeparator(char separator) {
	    pathProvider.setSeparator(separator).inmutable();
	    return this;
	}
}