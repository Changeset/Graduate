package ids.core;

import ids.storage.Redis;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.org.apache.xalan.internal.xslt.EnvironmentCheck.WARNING;
import static ids.core.AbstractStorage.DIRECTION_ANCESTORS;

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

    private static final Logger logger = Logger.getLogger(Detect.class.getName());

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

    private static final double threshold = 0.7;
    private static int maxDepth = 9999;

    public static Graph importGraph(String path, Redis redisScaffold) {
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
                processImportLine(line, result, vertexMap, redisScaffold);
            }
            eventReader.close();
        } catch (Exception e) {
            Logger.getLogger(Detect.class.getName()).log(Level.WARNING, "File read unsuccessful!", e);
            e.printStackTrace();
        }
        result.commitIndex();
        return result;
    }
    public static void processImportLine(String line, Graph graph, Map<String, Vertex> vertexMap, Redis redisScaffold) {
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
                            redisScaffold.insertHashName(key, key_value[1]);
                        }
                    }
                }
                graph.putVertex(vertex);
                vertexMap.put(key, vertex);
            } else if (edgeMatcher.find()) {
                /*
                * 存储父节点和子节点的关系
                 */
                String childkey = edgeMatcher.group(1);
                String dstkey = edgeMatcher.group(2);
                String label = edgeMatcher.group(3);
                Vertex childVertex = vertexMap.get(childkey);
                Vertex parentVertex = vertexMap.get(dstkey);
                Edge edge = new Edge(childVertex, parentVertex);
                redisScaffold.insertEntry(edge);
                if (redisScaffold.insertRule(childkey, dstkey)) {
                    logger.log(Level.SEVERE, "rules " + childVertex + " -> " + parentVertex + " added.");
                }
                graph.putEdge(edge);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public static List<Boolean> detectGraph(Graph graph, Redis redisScaffold, int maxDepth) {
        List<String> rootVertexHashList = redisScaffold.getRootVertexList();
        List<Boolean> resultList = new LinkedList<>();
        int depi = 0;
        long edgeCount = 0;
        for (String rootVertexHash : rootVertexHashList) {
            Graph detectGraph = new Graph();
            detectGraph = graph.getLineage(rootVertexHash, "ancestors", maxDepth);
            Set<Edge> detectEdgeSet = detectGraph.edgeSet();
            int detectEdgeCount = detectGraph.edgeSet().size();
            int detectDepi = 0;
            edgeCount += detectEdgeCount;
            detectGraph.setEdgeCount(detectEdgeCount);
            for (Edge edge : detectEdgeSet) {
                detectDepi += onlineDetect(edge, redisScaffold);
            }
            depi += detectDepi;
            double result = (double)detectDepi / (double)detectEdgeCount;
            if (result >= threshold) resultList.add(false);
            else resultList.add(true);
        }
//        double result = (double)depi / (double)edgeCount;
        redisScaffold.shutdown();
        return resultList;
    }

    public static int onlineDetect(Edge detectEdge, Redis redisScaffold) {
        int depi = 0;
        String childVertex = detectEdge.getChildVertex().bigHashCode();
        String parentVertex = detectEdge.getParentVertex().bigHashCode();
        if(redisScaffold.isRule(childVertex, parentVertex)) depi++;
        return depi;
    }
    public static void main(String[] args) {
        System.out.println(args[0]);
        if (args.length != 1) {
            System.out.println("input parameters error!");
            return;
        }
//        try {
//            String logFilename = System.getProperty("ids.log");
//            if (logFilename == null) {
//                new File(LOG_PATH).mkdirs();
//                Date currentTime = new java.util.Date(System.currentTimeMillis());
//                String logStartTime = new java.text.SimpleDateFormat(LOG_START_TIME_PATTERN).format(currentTime);
//                logFilename = LOG_PATH + FILE_SEPARATOR + LOG_PREFIX + logStartTime + ".log";
//            }
//            final Handler logFileHandler = new FileHandler(logFilename);
//            logFileHandler.setFormatter(new SimpleFormatter());
//            logFileHandler.setLevel(Level.parse(Settings.getProperty("logger_level")));
//            Logger.getLogger("").addHandler(logFileHandler);
//        } catch (IOException | SecurityException exception) {
//            System.err.println("Error initializing exception logger");
//        }
        Detect detectInstance = new Detect();
        Redis redisScaffold = new Redis();
        redisScaffold.initialize("");
        Graph provenanceGraph = detectInstance.importGraph(args[0], redisScaffold);
        List<Boolean> detectResult = detectGraph(provenanceGraph, redisScaffold, maxDepth);
        for (boolean res : detectResult) {
            if (res) {
                Logger.getLogger(Detect.class.getName()).log(Level.WARNING, "Intrusion detected!");
                System.out.println("Attack detected!");
            } else {
                Logger.getLogger(Detect.class.getName()).log(Level.WARNING, "No intrusion detected.");
            }
        }
    }
}