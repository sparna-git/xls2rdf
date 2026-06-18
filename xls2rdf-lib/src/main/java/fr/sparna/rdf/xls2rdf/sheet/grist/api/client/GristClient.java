package fr.sparna.rdf.xls2rdf.sheet.grist.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.caller.CallableGrist;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.caller.GristPath;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.cache.CacheItem;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.cache.GristClientCache;

import java.util.Map;
import java.util.Set;

public class GristClient extends AbstractGristClient {

    public GristClient(CallableGrist caller, boolean useCache) {
        super(caller, useCache);
    }

    public static GristClient getInstance(CallableGrist caller, boolean useCache){
        return new GristClient(caller, useCache);
    }

    @Override
    public JsonNode getGristWorkspace(int workspaceId) {
        if(this.isUseCache() && this.isCached(CacheItem.WORKSPACE, Integer.toString(workspaceId))){
            return this.getNodeCache(CacheItem.WORKSPACE, Integer.toString(workspaceId)).getJsonNode();
        }
        JsonNode node = this.getJsonNode(this.processGetRequest(GristPath.GET_DOCUMENT, Map.of(GristPath.GristPlaceholder.DOC_ID, Integer.toString(workspaceId))));
        if(this.isUseCache()) this.addCache(CacheItem.WORKSPACE, node, Integer.toString(workspaceId));
        return node;
    }

    @Override
    public JsonNode getGristDocument(String documentId) {
        if(this.isUseCache() && this.isCached(CacheItem.DOCUMENT, documentId)){
            return this.getNodeCache(CacheItem.DOCUMENT, documentId).getJsonNode();
        }
        JsonNode node = this.getJsonNode(this.processGetRequest(GristPath.GET_DOCUMENT, Map.of(GristPath.GristPlaceholder.DOC_ID, documentId)));
        if(this.isUseCache()) this.addCache(CacheItem.DOCUMENT, node, documentId);
        return node;
    }

    @Override
    public JsonNode getGristTables(String documentId) {
        if(this.isUseCache() && this.isCached(CacheItem.TABLES, documentId)){
            return this.getNodeCache(CacheItem.TABLES, documentId).getJsonNode();
        }
        JsonNode node = this.getJsonNode(this.processGetRequest(GristPath.GET_TABLES, Map.of(GristPath.GristPlaceholder.DOC_ID, documentId)));
        if(this.isUseCache()) this.addCache(CacheItem.TABLES, node, documentId);
        return node;
    }

    @Override
    public JsonNode getGristRecords(String documentId, String tableId) {
        String[] identifiers = {documentId, tableId};
        if(this.isUseCache() && this.isCached(CacheItem.RECORDS, identifiers)){
            return this.getNodeCache(CacheItem.RECORDS, identifiers).getJsonNode();
        }
        JsonNode node = this.getJsonNode(this.processGetRequest(GristPath.GET_RECORDS, Map.of(
                GristPath.GristPlaceholder.DOC_ID, documentId,
                GristPath.GristPlaceholder.TABLE_ID, tableId)));
        if(this.isUseCache()) this.addCache(CacheItem.RECORDS, node, identifiers);
        return node;
    }

    @Override
    public JsonNode getGristColumns(String documentId, String tableId) {
        String[] identifiers = {documentId, tableId};
        if(this.isUseCache() && this.isCached(CacheItem.COLUMNS, identifiers)){
            return this.getNodeCache(CacheItem.COLUMNS, identifiers).getJsonNode();
        }
        JsonNode node = this.getJsonNode(this.processGetRequest(GristPath.GET_COLUMNS, Map.of(
                GristPath.GristPlaceholder.DOC_ID, documentId,
                GristPath.GristPlaceholder.TABLE_ID, tableId)));
        if(this.isUseCache()) this.addCache(CacheItem.COLUMNS, node, identifiers);
        return node;
    }

    @Override
    public boolean addCache(CacheItem item, JsonNode jsonNode, String... identifiers) {
        return CACHE.addCache(item, jsonNode, identifiers);
    }

    @Override
    public boolean removeCache(CacheItem item, String... identifiers) {
        return CACHE.removeCache(item, identifiers);
    }

    @Override
    public GristClientCache.NodeCache getNodeCache(CacheItem item, String... identifiers) {
        return CACHE.getNodeCache(item, identifiers);
    }

    @Override
    public Set<GristClientCache.NodeCache> getCache() {
        return CACHE.getCache();
    }

    @Override
    public boolean isCached(CacheItem item, String... identifiers) {
        return CACHE.isCached(item, identifiers);
    }

    @Override
    public void setOverrideCache(boolean override) {
        CACHE.setOverrideCache(override);
    }

    @Override
    public boolean isOverrideCache() {
        return CACHE.isOverrideCache();
    }
}