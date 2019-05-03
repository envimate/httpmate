package com.envimate.httpmate.examples.helloworld;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.HttpMateChainKeys;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.convenience.configurators.Configurators;
import com.envimate.httpmate.convenience.endpoints.PureJavaEndpoint;
import com.envimate.httpmate.convenience.handler.HttpHandler;
import com.envimate.httpmate.convenience.handler.HttpRequest;
import com.envimate.httpmate.convenience.handler.HttpResponse;
import com.envimate.httpmate.generator.GenerationCondition;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.http.HttpRequestMethod;

import java.util.Optional;

import static com.envimate.httpmate.HttpMateChainKeys.*;

public class Application {
    public static void main(String[] args) {
        final HttpMate httpMate = HttpMate.aLowLevelHttpMate()
                .callingTheHandler(new HttpHandler() {
                    @Override
                    public void handle(final HttpRequest request, final HttpResponse httpResponse) {
                        final Optional<String> name = request.queryParameters().getQueryParameter("name");
                        httpResponse.setBody("Hello " + name.orElse("World"));
                        httpResponse.setStatus(200);
                    }
                })
                .forRequestPath("/api/hello")
                .andRequestMethod(HttpRequestMethod.GET)
                .callingTheHandler(new Handler() {
                    @Override
                    public void handle(final MetaData metaData) {
                        final Optional<String> name = metaData.get(QUERY_PARAMETERS).getQueryParameter("name");
                        metaData.set(RESPONSE_STRING, "Hello " + name.orElse("World!"));
                        metaData.set(RESPONSE_STATUS, 200);
                    }
                })
                .forRequestPath("/api/helloDirect")
                .andRequestMethod(HttpRequestMethod.GET)
                .thatIs()
                .configured(Configurators.toLogUsing((message, metaData) -> {
                    System.out.println(message);
                }))
                .build();

        PureJavaEndpoint.pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
