package ids.core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 11:32 2019/1/17
 * @ ModifiedBy:
 */
public class Vertex implements Serializable {

    /**
     * A map containing the annotations for this vertex.
     */
    protected Map<String, String> annotations = new TreeMap<>();

    /**
     * An integer indicating the depth of the vertex in the graph
     */
    private int depth;

    public int getDepth() { return depth; }

    public void setDepth(int depth) { this.depth = depth; }

    /**
     * Checks if vertex is empty
     *
     * @return Returns true if vertex contains no annotation
     */
    public final boolean isEmpty() { return annotations.size() == 0; }

    /**
     * Returns the map containing the annotations for this vertex.
     *
     * @return The map containing the annotations.
     */
    public final Map<String, String> getAnnotations() { return annotations; }

    /**
     * Adds a map of annotation.
     *
     * @param key The annotation key.
     * @param value The annotation value.
     */
    public final void addAnnotation(String key, String value) {
        if (key != null && !key.isEmpty()) {
            if (value == null) {
                value = "";
            }
            annotations.put(key, value);
        }
    }

    /**
     * Adds a map of annotation.
     *
     * @param newAnnotations New annotations to be added.
     */
    public final void addAnnotations(Map<String, String> newAnnotations) {
        for (Map.Entry<String, String> currentEntry : newAnnotations.entrySet()) {
            String key = currentEntry.getKey();
            String value = currentEntry.getValue();
            if (key != null  && !key.isEmpty()) {
                if (value == null) {
                    value = "";
                }
                addAnnotation(key, value);
            }
        }
    }

    public final String removeAnnotation(String key) { return annotations.remove(key); }

    public final String getAnnotation(String key) { return annotations.get(key); }

    /**
     * Gets the type of this vertex.
     *
     * @return A string indicating the type of this vertex.
     */
    public final String type() { return annotations.get("type"); }

    /**
     * Computes MD5 hash of annotations in the vertex.
     *
     @return A 128-bit hash digest.
     */
    public String bigHashCode()
    {
        return DigestUtils.md5Hex(this.toString());
    }


    /**
     * Computes MD5 hash of annotations in the vertex
     * @return 16 element byte array of the digest.
     */
    public byte[] bigHashCodeBytes()
    {
        return DigestUtils.md5(this.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vertex)) return false;

        Vertex vertex = (Vertex) obj;
        return annotations.equals(vertex.annotations);
    }

    public boolean isCompleteNetworkVertex() {
        String subtype = this.getAnnotation("subtype");
        String source = this.getAnnotation("source");
        if (subtype != null && subtype.equalsIgnoreCase("network socket")
                && source.equalsIgnoreCase("netfilter")) {
            return true;
        }

        return false;
    }

    public boolean isNetworkVertex() {
        String subtype = this.getAnnotation("subtype");
        if (subtype != null && subtype.equalsIgnoreCase("network socket")) {
            return true;
        }

        return false;
    }

    /**
     * Computes a function of the annotations in the vertex.
     *
     * This takes less time to compute than bigHashCode() but is less collision-resistant.
     *
     * @return An integer-valued hash code.
     */
    @Override
    public int hashCode()
    {
        final int seed1 = 67;
        int hashCode = 3;
        hashCode = seed1 * hashCode + (this.annotations != null ? this.annotations.hashCode() : 0);
        return hashCode;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "annotations=" + annotations +
                '}';
    }
}
