package ids.storage;

import ids.core.Edge;
import ids.core.Graph;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static Jedis childScaffold;
    private static Jedis parentScaffold;

    @Override
    public boolean initialize(String arguments) {
        try {
            childScaffold = new Jedis("localhost");
            parentScaffold = new Jedis("localhost");

            childScaffold.configSet("dir", "/tmp/db/redis/");
            childScaffold.configSet("maxmemory", "5GB");
            childScaffold.configSet("maxmemory-policy", "allkeys-lfu");

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
