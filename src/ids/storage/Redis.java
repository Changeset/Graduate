package ids.storage;

import ids.core.Edge;
import ids.core.Graph;
import ids.core.Vertex;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 10:57 2019/1/18
 * @ ModifiedBy:
 */
public class Redis extends Scaffold {
    private static Logger logger = Logger.getLogger(Redis.class.getName());

    // 操作parentName -> childName的连接
    private static Jedis ruleScaffold;

    // 操作hash -> name数据库的连接
    private static Jedis hashToName;

    // 操作childHash -> parentHash数据库的连接
    private static Jedis childScaffold;

    // 操作parentHash -> childHash数据库的连接
    private static Jedis parentScaffold;

    // redis数据库连接池
    private static JedisPool connectionPool;

    static {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(20);

            connectionPool = new JedisPool(jedisPoolConfig, "localhost", 6379);
    }
    @Override
    public boolean initialize(String arguments) {
        try {
            ruleScaffold = connectionPool.getResource();
            ruleScaffold.select(0);

            hashToName = connectionPool.getResource();
            hashToName.select(2);

            childScaffold = connectionPool.getResource();
            childScaffold.select(4);

            parentScaffold = connectionPool.getResource();
            parentScaffold.select(3);

            ruleScaffold.configSet("dir", "/tmp/db/redis/");
            ruleScaffold.configSet("maxmemory", "5GB");
            ruleScaffold.configSet("maxmemory-policy", "allkeys-lfu");

            logger.log(Level.INFO, "Scaffold initialized successfully!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to initialize scaffold!", e);
            return false;
        }

        return true;
    }

    @Override
    protected void globalTxCheckin(boolean forcedFlush) {

    }

    /**
     * This method is invoked by the AbstractStorage to shut down the storage.
     *
     * @return True if scaffold was shut down successfully.
     */
    public boolean shutdown() {
        try {
            ruleScaffold.close();
            hashToName.close();
            childScaffold.close();
            parentScaffold.close();
            logger.log(Level.INFO, "Scaffold closed successfully!");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error shutting down scaffold!");
            return false;
        }
        return true;
    }

    public Set<String> getChildren(String parentHash) { return null; }

    public Set<String> getParents(String childHash) { return null; }

    public Set<String> getNeighbors(String hash) { return null; }

    public Map<String, Set<String>> getLineage(String hash, String direction, int maxDepth) { return null; }

    public Map<String, Set<String>> getPaths(String source_hash, String destination_hash, int maxDepth) {
        return null;
    }

    /**
     * if the rule exists return true
     * @param childHash
     * @param parentHash
     * @return
     */
    public boolean isRule(String childHash, String parentHash) {
        return ruleScaffold.exists(childHash) && ruleScaffold.get(childHash).equals(parentHash);
    }

    /// TODO: 将差集替换成用游标查询
    public List<String> getRootVertexList() {
        Set<String> parentSet = parentScaffold.keys("*");
        Set<String> childSet = childScaffold.keys("*");
        Set<String> resKeySet = new HashSet<>(parentSet);
        resKeySet.removeAll(childSet);
        List<String> result = new LinkedList<>();
        for (String hashKey : resKeySet) {
            if (hashToName.exists(hashKey)) {
                result.add(hashKey);
            }
        }
        return result;
    }
    public boolean insertRule(String childHash, String parentHash) {
        if (hashToName.exists(childHash) && hashToName.exists(parentHash)) {
            ruleScaffold.set(childHash, parentHash);
            return true;
        }
        return false;
    }
    public boolean insertHashName(String hash, String name) {
        try {
            hashToName.set(hash, name);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Scaffold hashName insertion not successful!", e);
            return false;
        }
        return true;
    }

    public boolean insertEntry(Edge incomingEdge) {
        try {
            String childVertexHash = incomingEdge.getChildVertex().bigHashCode();
            String parentVertexHash = incomingEdge.getParentVertex().bigHashCode();
            childScaffold.sadd(childVertexHash, parentVertexHash);
            parentScaffold.sadd(parentVertexHash, childVertexHash);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Scaffold entry insertion not successful!", e);
            return false;
        }
        return true;
    }

    public Graph queryManager(Map<String, List<String>> params) { return null; }

    public static void main(String[] args) {

    }

}
