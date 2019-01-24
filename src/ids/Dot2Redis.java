package ids;

/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 10:46 2019/1/17
 * @ ModifiedBy:
 */
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 17:16 2019/1/14
 * @ ModifiedBy:
 */
public class Dot2Redis {
    private static Jedis jedis;
    private static final Logger logger = Logger.getLogger(Dot2Redis.class.getName());
    private static String filePath = "";
    private static final Pattern nodePattern = Pattern.compile("\"(.*)\" \\[label=\"(.*)\" shape=\"(\\w*)\" fillcolor=\"(\\w*)\"", Pattern.DOTALL);
    private static final Pattern edgePattern = Pattern.compile("\"(.*)\" -> \"(.*)\" \\[label=\"(.*)\" color=\"(\\w*)\"", Pattern.DOTALL);

    private static Map<String, String> vertex = new HashMap<>();
    private List<Map<String, String>> edgeList = new ArrayList<>();
    private static List<Map<String, String>> vertexList = new ArrayList<>();
    private ArrayList<String> edgeColumnNames = new ArrayList<>();
    private ArrayList<String> vertexColumnNames = new ArrayList<>();

    public boolean initialize(String arguments) {
        filePath = filePath + arguments;
        File dotFile = new File(filePath);
        jedis = new Jedis("127.0.0.1", 6379);
        jedis.auth("root");
        try {
            InputStream is = new FileInputStream(dotFile);
            Reader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                processImportLine(line);
            }
        } catch(Exception e) {
            Logger.getLogger(Dot2Redis.class.getName()).log(Level.WARNING, "File read unsuccessful!", e);
        }
        return true;
    }

    private static void processImportLine(String line) {
        try {
            Matcher nodeMatcher = nodePattern.matcher(line);
            Matcher edgeMatcher = edgePattern.matcher(line);
            if (nodeMatcher.find()) {
                /*
                 * 存储hash值与节点的对应关系
                 */
                jedis.select(1);
                String key = nodeMatcher.group(1);
                String label = nodeMatcher.group(2);
                String shape = nodeMatcher.group(3);
                String[] pairs = label.split("\\\\n");
                for (String pair : pairs) {
                    String key_value[] = pair.split(":", 2);
                    if (key_value[0] == "name") {
                        if (!vertex.containsKey(key)) {
                            vertex.put(key, key_value[1]);
                            /**
                             * TODO: store hash key -> name in database#1.
                             */
                            if (!jedis.exists(key)) {
                                jedis.set(key, key_value[1]);
                            }
                        } else break;
                    }
                }
            } else if (edgeMatcher.find()) {
                /**
                 * TODO: store child -> parent dependency in database#2.
                 */
                jedis.select(2);
                String childkey = edgeMatcher.group(1);
                String dstkey = edgeMatcher.group(2);
                String label = edgeMatcher.group(3);
                String[] pairs = label.split("\\\\n");
                String childname = vertex.get(childkey);
                String dstname = vertex.get(dstkey);
                jedis.sadd(dstname, childname);
            }
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error while processing line: " + line, ex);
        }
    }

    public boolean detect() {

        return true;
    }
    public static void main(String[] args) {

    }
}
