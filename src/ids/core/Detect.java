package ids.core;

import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 11:06 2019/1/18
 * @ ModifiedBy:
 */
public class Detect {
    static {
        System.setProperty("java.util.logging.manager", ids.utility.LogManager.class.getName());
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s %4$s: %5$s%6$s%n");
    }

    private static final Pattern nodePattern = Pattern.compile("\"(.*)\" \\[label=\"(.*)\" shape=\"(\\w*)\" fillcolor=\"(\\w*)\"", Pattern.DOTALL);
    private static final Pattern edgePattern = Pattern.compile("\"(.*)\" -> \"(.*)\" \\[label=\"(.*)\" color=\"(\\w*)\"", Pattern.DOTALL);


    public static final String IDS_ROOT = Settings.getProperty("ids_root");

    public static final String FILE_SEPARATOR = String.valueOf(File.separatorChar);

    public static final String DB_ROOT = IDS_ROOT + "db" + FILE_SEPARATOR;

    /**
     * Path to configuration files.
     */
    public static final String CONFIG_PATH = IDS_ROOT + FILE_SEPARATOR + "cfg";

    /**
     * Paths to log files.
     */
    private static final String LOG_PATH = IDS_ROOT + FILE_SEPARATOR + "log";

    /**
     * Path to log files including the prefix.
     */
    private static final String LOG_PREFIX = "IDS_";

    /**
     * Date/time suffix pattern for log files
     */
    private static final String LOG_START_TIME_PATTERN = "MM.dd.yyyy-H.mm.ss";

    private static Jedis hashToName;

    public static Graph importGraph(String path) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Graph result = new Graph();
        Map<String, Vertex> vertexMap = new HashMap<>();
        try {
            BufferedReader eventReader = new BufferedReader(new FileReader(path));
            String line;
            while (true) {
                line = eventReader.readLine();
                if (line == null) break;
                processImportLine(line, result, vertexMap);
            }
            eventReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.commitIndex();
        return result;
    }
    public static void processImportLine(String line, Graph graph, Map<String, Vertex> vertexMap) {
        try {
            Matcher nodeMatcher = nodePattern.matcher(line);
            Matcher edgeMatcher = edgePattern.matcher(line);
            if (nodeMatcher.find()) {
                /*
                * 存储hash值与节点的对应关系
                 */
                String key = nodeMatcher.group(1);
                String label = nodeMatcher.group(2);
                String shape = nodeMatcher.group(3);
                Vertex vertex = new Vertex();
                String[] pairs = label.split("\\\\n");
                for (String pair : pairs) {
                    String key_value[] = pair.split(":", 2);
                    if (key_value.length == 2) {
                        vertex.addAnnotation(key_value[0], key_value[1]);
                        if (key_value[0].equals("name")) {
                            hashToName.set(key, key_value[1]);
                        }
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        hashToName = new Jedis("127.0.0.1", 6379);
        hashToName.auth("root");
        Graph graph = Detect.importGraph(args[1]);

    }

}





