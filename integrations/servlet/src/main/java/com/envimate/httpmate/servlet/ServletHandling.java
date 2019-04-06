package com.envimate.httpmate.servlet;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.chains.MetaData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.util.Streams.streamInputStreamToOutputStream;
import static com.envimate.httpmate.util.Streams.stringToInputStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServletHandling {

    public static void handle(final HttpMate httpMate,
                              final HttpServletRequest request,
                              final HttpServletResponse response) throws IOException {
        final MetaData metaData = extractMetaDataFromHttpServletRequest(request);
        final InputStream body = request.getInputStream();
        metaData.set(BODY_STREAM, body);
        metaData.set(IS_HTTP_REQUEST, true);

        httpMate.handleRequest(metaData, httpResponse -> {
            final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
            responseHeaders.forEach(response::setHeader);
            final int responseStatus = metaData.get(RESPONSE_STATUS);
            response.setStatus(responseStatus);
            final OutputStream outputStream = response.getOutputStream();
            final InputStream responseBody = metaData.getOptional(STREAM_RESPONSE).orElseGet(() -> stringToInputStream(""));
            streamInputStreamToOutputStream(responseBody, outputStream);
        });
    }

    public static MetaData extractMetaDataFromHttpServletRequest(final HttpServletRequest request) {
        final String path = request.getPathInfo();
        final String method = request.getMethod();
        final Map<String, String> headers = extractHeaders(request);
        final Map<String, String> queryParameters = extractQueryParameters(request);

        final MetaData metaData = emptyMetaData();
        metaData.set(RAW_HEADERS, headers);
        metaData.set(RAW_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_METHOD, method);
        metaData.set(PATH, path);
        return metaData;
    }

    private static Map<String, String> extractHeaders(final HttpServletRequest request) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        final Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final String value = request.getHeader(headerName);
            headers.put(headerName, value);
        }
        return headers;
    }

    private static Map<String, String> extractQueryParameters(final HttpServletRequest request) {
        final Map<String, String> queryParameters = new HashMap<>();
        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String parameterName = parameterNames.nextElement();
            final String value = request.getParameter(parameterName);
            queryParameters.put(parameterName, value);
        }
        return queryParameters;
    }
}
