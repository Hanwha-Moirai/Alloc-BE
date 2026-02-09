package com.moirai.alloc.search.query.infra.openSearch;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.regions.Region;


@Configuration
public class OpenSearchConfig {

    @Value("${opensearch.host}")
    private String host;

    @Value("${opensearch.port}")
    private int port;

    @Value("${opensearch.scheme}")
    private String scheme;

    @Bean
    public RestHighLevelClient openSearchClient() {

        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        Aws4Signer signer = Aws4Signer.create();

        // OpenSearch service name은 보통 "es"
        String serviceName = "es";
        Region region = Region.AP_NORTHEAST_2;

        HttpRequestInterceptor interceptor =
                new AwsRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider, region);

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.addInterceptorLast(interceptor)
                );

        return new RestHighLevelClient(builder);
    }
}
