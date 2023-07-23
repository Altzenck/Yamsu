package me.altzenck.yml;

import java.io.*;
import java.util.Map;
import javax.annotation.Nonnull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Altzenck
 * @version 1.2.0
 */
public class Yml extends YamlBase{

   @SuppressWarnings("unchecked")
   private Yml(InputStream is) {
	   super((Map<String,Object>) new Yaml().load(is));
   }
   
   public void setDefaults(@Nonnull File file) {
	   setDefaults(Yml.loadYaml(file));
   }
   
   public void setDefaults(@Nonnull Reader reader) {
	   setDefaults(Yml.loadYaml(reader));
   }
   
   public void setDefaults(@Nonnull InputStream is) {
	   setDefaults(Yml.loadYaml(is));
   }
   
   public void save(@Nonnull File file) {
	   file.getParentFile().mkdirs();
	   if(file.isDirectory()) throw new IllegalArgumentException("The specified file is a directory!");
	   DumperOptions options = new DumperOptions();
	   options.setIndent(2);
	   options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	   if(file.exists()) file.delete();
	   try {
		StringReader in = new StringReader(new Yaml(options).dump(current));
	    FileOutputStream os = new FileOutputStream(file);
	    char[] buffer = new char[2048];
	    int i = 0;
		while((i = in.read(buffer, 0, buffer.length))  != -1) {
			os.write(new String(buffer).getBytes(), 0, i);
		}
		os.close();
	   } catch (IOException e) {
		e.printStackTrace();
	   }
   }
   
   public static Yml loadYaml(@Nonnull File file) {
	try {
		return loadYaml(new FileInputStream(file));
	} catch (Exception e) {
		return loadYaml(new ByteArrayInputStream(new byte[0]));
	}
   }
   
   public static Yml loadYaml(@Nonnull Reader reader) {
	 char[] buffer = new char[2048];
	 int i = 0;
	 StringBuilder sb = new StringBuilder();
	 try {
	   while((i = reader.read(buffer, 0, buffer.length))  != -1) {
	   	 sb.append(buffer, 0, i);
	   }
	   reader.close();
	 } catch (IOException|NullPointerException e) {sb = new StringBuilder();}
	 return loadYaml(new ByteArrayInputStream(sb.toString().getBytes()));
	 
   }
   
   public static Yml loadYaml(@Nonnull InputStream is) {
     return new Yml(is);
   }
   
}
