package fr.sparna.rdf.xls2rdf.sheet.grist.api.client.cache;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class GristClientCache implements Cachable{

    private final Set<NodeCache> cache;
    private boolean override;

    public GristClientCache (){
        this.cache = new HashSet<>();
        this.override = true;
    }

    @Override
    public boolean addCache(CacheItem item, JsonNode jsonNode, String... identifiers) {
        NodeCache newNode;
        if(override){
            if(this.isCached(item, identifiers)){
                this.removeCache(item, identifiers);
                newNode = new NodeCache(item, jsonNode, identifiers);
                return this.cache.add(newNode);
            }
        }
        newNode = new NodeCache(item, jsonNode, identifiers);
        return this.cache.add(newNode);
    }

    @Override
    public boolean removeCache(CacheItem item, String... identifiers) {
        return this.cache.remove(new NodeCache(item, null, identifiers));
    }

    @Override
    public NodeCache getNodeCache(CacheItem item, String... identifiers) {
        Optional<NodeCache> node = this.cache.stream()
                .filter(n -> n.item.equals(item))
                .filter(n -> Arrays.equals(n.identifiers, identifiers))
                .findFirst();
        return node.orElse(null);
    }

    @Override
    public Set<NodeCache> getCache() {
        return Set.copyOf(this.cache);
    }

    @Override
    public boolean isCached(CacheItem item, String... identifiers) {
        return this.cache.stream()
                .filter(n -> n.item.equals(item))
                .anyMatch(n -> Arrays.equals(n.identifiers, identifiers));
    }

    @Override
    public void setOverrideCache(boolean override) {
        this.override = override;
    }

    @Override
    public boolean isOverrideCache() {
        return this.override;
    }

    public static class NodeCache {

        private final CacheItem item;
        private final String[] identifiers;
        private final JsonNode jsonNode;

        public NodeCache(CacheItem item, JsonNode jsonNode, String... identifiers){
            this.item = item;
            this.identifiers = Arrays.copyOf(identifiers, identifiers.length);
            this.jsonNode = jsonNode;
        }

        public String[] getIdentifiers() {
            return Arrays.copyOf(this.identifiers, this.identifiers.length);
        }

        public CacheItem getItem() {
            return item;
        }

        public JsonNode getJsonNode(){
            return this.jsonNode;
        }

        @Override
        public boolean equals(Object obj) {

            if(obj == null) return false;

            if(obj == this) return true;

            if(!(obj instanceof NodeCache node)) return false;

            return this.item.equals(node.item)
                    &&
                    Arrays.equals(this.identifiers, node.identifiers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(this.identifiers), this.item);
        }
    }

}
