package ids.core;

import java.util.Map;
import java.util.TreeMap;

/**
 * @ Author: Xuelong Liao
 * @ Description:
 * @ Date: created in 11:33 2019/1/17
 * @ ModifiedBy:
 */
public class Edge {

    /**
     * A map containing the annotations for this edge.
     */
    protected Map<String, String> annotations = new TreeMap<>();
    private Vertex childVertex;
    private Vertex parentVertex;

    /**
     * Constructor taking only the source and destination vertices.
     *
     * @param childVertex Source vertex
     * @param parentVertex Destination vertex
     */
    public Edge(Vertex childVertex, Vertex parentVertex) {
        setChildVertex(childVertex);
        setParentVertex(parentVertex);
    }
    /**
     * Checks if edge is empty.
     *
     * @return Returns true if edge contains no annotation,
     * and both end points are empty
     */
    public final boolean isEmpty() { return annotations.size() == 0 && childVertex == null && parentVertex == null; }

    /**
     * Returns the map containing the annotations for this edge.
     *
     * @return The map containing the annotations.
     */
    public final Map<String, String> getAnnotations() { return annotations; }

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
            if (key != null && !key.isEmpty()) {
                if (value == null) {
                    value = "";
                }
                addAnnotation(key, value);
            }

        }
    }

    /**
     * Removes an annotation.
     *
     * @param key The annotation key to be removed.
     * @return The annotation that is removed, or null of no such annotation key
     * existed.
     */
    public final String removeAnnotation(String key) { return annotations.remove(key); }

    /**
     * Gets an annotation.
     *
     * @param key The annotation key.
     * @return The value of the annotation corresponding to the key.
     */
    public final String getAnnotation(String key) { return annotations.get(key); }

    /**
     * Gets the type of this edge.
     *
     * @return A string indicating the type of this edge.
     */
    public final String type() { return annotations.get("type"); }

    /**
     * Gets the source vertex.
     *
     * @return The source vertex attached to this edge.
     */
    public final Vertex getChildVertex() {
        return childVertex;
    }

    /**
     * Gets the destination vertex.
     *
     * @return The destination vertex attached to this edge.
     */
    public final Vertex getParentVertex() {
        return parentVertex;
    }

    /**
     * Sets the source vertex.
     *
     * @param childVertex The vertex that is to be set as the source for this
     * edge.
     */
    public final void setChildVertex(Vertex childVertex) {
        this.childVertex = childVertex;
    }

    /**
     * Sets the destination vertex.
     *
     * @param parentVertex The vertex that is to be set as the destination
     * for this edge.
     */
    public final void setParentVertex(Vertex parentVertex) {
        this.parentVertex = parentVertex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;

        Edge that = (Edge) o;

        if (!annotations.equals(that.annotations)) return false;
        if (!childVertex.equals(that.annotations)) return false;
        return parentVertex.equals(that.parentVertex);
    }
    /**
     * Computes a function of the annotations in the edge and the vertices it is incident upon.
     *
     * This takes less time to compute than bigHashCode() but is less collision-resistant.
     *
     * @return An integer-valued hash code.
     */
    @Override
    public int hashCode()
    {
        int result = annotations.hashCode();
        result = 31 * result + childVertex.hashCode();
        result = 31 * result + parentVertex.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "annotations=" + annotations +
                ", childVertex=" + childVertex +
                ", parentVertex=" + parentVertex +
                '}';
    }

}
