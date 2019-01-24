package ids.core;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 11:05 2019/1/18
 * @ ModifiedBy:
 */
public class Settings {
    private static final String settingsFile = "cfg/ids.core.Detect.config";
    private final static Properties idsProerties = new Properties();

    static
    {
        // load settings from the file
        try
        {
            idsProerties.load(new FileInputStream(settingsFile));
        }
        catch(Exception ex)
        {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, "Error reading settings file! Shutting down...", ex);
            System.exit(-1);
        }
    }

    public static String getProperty(String property) {
        return idsProerties.getProperty(property);
    }

    public static String getDefaultConfigFilePath(Class<?> forClass){
        return "cfg/" + forClass.getName() + ".config";
    }

    public static String getDefaultOutputFilePath(Class<?> forClass){
        return "cfg/" + forClass.getName() + ".out";
    }

}
