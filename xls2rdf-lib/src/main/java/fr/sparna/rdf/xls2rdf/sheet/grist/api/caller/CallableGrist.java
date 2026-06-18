package fr.sparna.rdf.xls2rdf.sheet.grist.api.caller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Consumer;

public interface CallableGrist extends Callable<String> {

    URI prepareAndSetURI(GristPath gristPath, Map<GristPath.GristPlaceholder, String> values);

    @Override
    HttpResponse<String> doGet(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler) throws IOException, InterruptedException;

    @Override
    HttpResponse<String> doPost(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException;

    @Override
    HttpResponse<String> doPut(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException;

    @Override
    HttpResponse<String> doPatch(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException;

    @Override
    HttpResponse<String> doDelete(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<String> bodyHandler) throws IOException, InterruptedException;

    void setPreparedURI(URI preparedURI);

    URI getPreparedURI();

}
