package com.moirai.alloc.search.query.infra.openSearch;

import org.apache.http.*;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class AwsRequestSigningApacheInterceptor implements HttpRequestInterceptor {

    private final String service;
    private final Aws4Signer signer;
    private final AwsCredentialsProvider credentialsProvider;
    private final Region region;

    private final String host;
    private final String scheme;

    public AwsRequestSigningApacheInterceptor(
            String service,
            Aws4Signer signer,
            AwsCredentialsProvider credentialsProvider,
            Region region,
            String host,
            String scheme
    ) {
        this.service = service;
        this.signer = signer;
        this.credentialsProvider = credentialsProvider;
        this.region = region;
        this.host = host;
        this.scheme = scheme;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        try {
            URI uri = URI.create(
                    scheme + "://" + host + request.getRequestLine().getUri()
            );

            SdkHttpFullRequest sdkRequest = toSdkRequest(request, uri);

            Aws4SignerParams params = Aws4SignerParams.builder()
                    .awsCredentials(credentialsProvider.resolveCredentials())
                    .signingName(service)
                    .signingRegion(region)
                    .build();

            SdkHttpFullRequest signed = signer.sign(sdkRequest, params);

            // signed headers를 Apache request에 주입
            List<Header> headers = new ArrayList<>();
            signed.headers().forEach((k, v) -> v.forEach(val -> headers.add(new BasicHeader(k, val))));
            for (Header h : headers) request.setHeader(h);

        } catch (Exception e) {
            throw new IOException("Failed to sign request", e);
        }
    }

    private SdkHttpFullRequest toSdkRequest(HttpRequest request, URI uri) {
        SdkHttpMethod method = SdkHttpMethod.fromValue(request.getRequestLine().getMethod());

        SdkHttpFullRequest.Builder b = SdkHttpFullRequest.builder()
                .method(method)
                .uri(uri);

        for (Header h : request.getAllHeaders()) {
            b.appendHeader(h.getName(), h.getValue());
        }

        return b.build();
    }
}
