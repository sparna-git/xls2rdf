package fr.sparna.rdf.xls2rdf.sheet.grist.api.caller;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public interface Callable<R> {

    HttpResponse<R> doGet(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException;

    HttpResponse<R> doPost(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<R> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException;

    HttpResponse<R> doPut(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<R> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException;

    HttpResponse<R> doPatch(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<R> bodyHandler, HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException;

    HttpResponse<R> doDelete(Consumer<HttpRequest.Builder> prepareHttpRequest, HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException;

}
