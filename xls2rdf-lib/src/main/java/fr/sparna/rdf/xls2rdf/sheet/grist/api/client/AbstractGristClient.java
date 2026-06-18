package fr.sparna.rdf.xls2rdf.sheet.grist.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.caller.CallableGrist;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.caller.GristPath;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.cache.Cachable;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.cache.GristClientCache;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

public abstract class AbstractGristClient implements Client, Cachable {

    protected static final GristClientCache CACHE = GristClientCache.getInstance();

    private final CallableGrist caller;
    private final ObjectMapper mapper;
    private boolean useCache;

    protected AbstractGristClient(CallableGrist caller, boolean useCache){
        this.caller = caller;
        this.useCache = useCache;
        this.mapper = new ObjectMapper();
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public CallableGrist getCaller() {
        return caller;
    }

    protected HttpResponse<String> processGetRequest(GristPath path, Map<GristPath.GristPlaceholder, String> values){
        this.getCaller().prepareAndSetURI(path, values);
        try {
            return this.getCaller().doGet(null, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Xls2RdfException.rethrow(e);
        }
        return null;
    }

    protected JsonNode getJsonNode(HttpResponse<String> gristResponse) {
        if(gristResponse.statusCode() == 200) {
            try {
                return this.getMapper().readTree(gristResponse.body());
            } catch (JsonProcessingException e) {
                Xls2RdfException.rethrow(e);
            }
        }
        throw new Xls2RdfException("Grist's response is not 200, check your ID.");
    }



}
