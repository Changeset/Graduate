package ids.storage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 10:57 2019/1/18
 * @ ModifiedBy:
 */
public class ScaffoldFactory {
    public static Scaffold createScaffold(String scaffoldDatabaseName)
    {
        Scaffold scaffold;
        String packagePath = "ids.storage.";
        try
        {
            scaffold = (Scaffold) Class.forName(packagePath + scaffoldDatabaseName).newInstance();
        }
        catch(Exception ex)
        {
            scaffold = createDefaultScaffold();
            Logger.getLogger(ScaffoldFactory.class.getName()).log(Level.SEVERE, "Scaffold database not found! Creating default Scaffold", ex);
        }

        return scaffold;
    }

    public static Scaffold createDefaultScaffold()
    {
        return new Redis();
    }
}
