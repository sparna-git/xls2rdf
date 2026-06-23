package fr.sparna.rdf.xls2rdf.sheet.grist.api.caller;

import fr.sparna.rdf.xls2rdf.sheet.grist.api.connector.GristConnector;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public  class GristCaller implements CallableGrist {

    private final GristConnector gristConnector;

    private final HttpClient client;
    private URI preparedURI;

    public GristCaller(HttpClient client, GristConnector gristConnector){
        this.client = client;
        this.gristConnector = gristConnector;
    }

    public HttpClient getClient(){
        return this.client;
    }

    public GristConnector getGristConnector(){
        return this.gristConnector;
    }

    @Override
    public URI getPreparedURI(){
        return this.preparedURI;
    }

    @Override
    public void setPreparedURI(URI preparedURI){
        this.preparedURI = preparedURI;
    }

    @Override
    public HttpResponse<String> doDelete(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler) throws IOException, InterruptedException {
        HttpRequest.Builder deleteRequestBuilder = HttpRequest.newBuilder(this.getPreparedURI())
                .DELETE();
        this.applyAuthorization(deleteRequestBuilder);
        if(Objects.nonNull(prepareHttpRequest)) prepareHttpRequest.accept(deleteRequestBuilder);
        HttpRequest deleteRequest = deleteRequestBuilder.build();
        return this.getClient().send(deleteRequest, bodyHandler);
    }

    @Override
    public HttpResponse<String> doPatch(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException {
        HttpRequest.Builder patchRequestBuilder = HttpRequest.newBuilder(this.getPreparedURI())
                .method("PATCH", bodyPublisher);
        this.applyAuthorization(patchRequestBuilder);
        if(Objects.nonNull(prepareHttpRequest)) prepareHttpRequest.accept(patchRequestBuilder);
        HttpRequest patchRequest = patchRequestBuilder.build();
        return this.getClient().send(patchRequest, bodyHandler);
    }

    @Override
    public HttpResponse<String> doPut(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException {
        HttpRequest.Builder putRequestBuilder = HttpRequest.newBuilder(this.getPreparedURI())
                .PUT(bodyPublisher);
        this.applyAuthorization(putRequestBuilder);
        if(Objects.nonNull(prepareHttpRequest)) prepareHttpRequest.accept(putRequestBuilder);
        HttpRequest putRequest = putRequestBuilder.build();
        return this.getClient().send(putRequest, bodyHandler);
    }

    @Override
    public HttpResponse<String> doPost(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException {
        HttpRequest.Builder postRequestBuilder = HttpRequest.newBuilder(this.getPreparedURI())
                .POST(bodyPublisher);
        this.applyAuthorization(postRequestBuilder);
        if(Objects.nonNull(prepareHttpRequest)) prepareHttpRequest.accept(postRequestBuilder);
        HttpRequest postRequest = postRequestBuilder.build();
        return this.getClient().send(postRequest, bodyHandler);
    }

    @Override
    public HttpResponse<String> doGet(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler) throws IOException, InterruptedException {
        HttpRequest.Builder getRequestBuilder = HttpRequest.newBuilder(this.getPreparedURI())
                .GET();
        this.applyAuthorization(getRequestBuilder);
        if(Objects.nonNull(prepareHttpRequest)) prepareHttpRequest.accept(getRequestBuilder);
        HttpRequest getRequest = getRequestBuilder.build();
        return this.getClient().send(getRequest, bodyHandler);
    }


    protected void applyAuthorization(HttpRequest.Builder request){
        request.setHeader("Authorization", "Bearer ".concat(this.getGristConnector().getTokenAPI()));
    }

    //A APPELER AVANT CHAQUE doX POUR PREPARE LA CHEMIN DE LA REQUETE
    // URI = Connector.baseAddress + gristPath
    @Override
    public URI prepareAndSetURI(GristPath gristPath, Map<GristPath.GristPlaceholder, String> values) {
        String buffer = gristPath.getPath();
        for(Map.Entry<GristPath.GristPlaceholder, String> entry : values.entrySet()){
            buffer = buffer.replace(entry.getKey().getPlaceholder(), entry.getValue());
        }
        this.setPreparedURI(this.getGristConnector().getBaseAddress().resolve(buffer));
        return this.preparedURI;
    }
}
