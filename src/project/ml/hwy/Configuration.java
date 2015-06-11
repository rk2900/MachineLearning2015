package project.ml.hwy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	private static Properties config=null;
	private static final String configPath = "config.properties";
	public static String read(String key,String defaultValue)
	{
		if(null==config)
		{
			config = new Properties();
			try {
				config.load(new FileInputStream(configPath));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return config.getProperty(key,defaultValue);
	}
}
