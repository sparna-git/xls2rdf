package fr.sparna.rdf.xls2rdf.sheet.grist.api.client.cache;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

public interface Cachable {

    boolean addCache(CacheItem item, JsonNode jsonNode, String ... identifiers);

    boolean removeCache(CacheItem item, String ... identifiers);

    GristClientCache.NodeCache getNodeCache(CacheItem item, String ... identifiers);

    Set<GristClientCache.NodeCache> getCache();

    boolean isCached(CacheItem item, String ... identifiers);

    void setOverrideCache(boolean override);

    boolean isOverrideCache();

}
