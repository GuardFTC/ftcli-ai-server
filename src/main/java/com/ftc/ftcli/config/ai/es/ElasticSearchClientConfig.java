package com.ftc.ftcli.config.ai.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.ftc.ftcli.properties.embedding.StoreESProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-15 10:20:08
 * @describe: ES客户端配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({StoreESProperties.class})
public class ElasticSearchClientConfig {

    private final StoreESProperties esProperties;

    @Bean
    public ElasticsearchClient esClient() {

        //1.创建HTTPHost
        HttpHost httpHost = HttpHost.create(esProperties.getUrl());

        //2.创建鉴权请求头
        Header[] authorizations = {
                new BasicHeader("Authorization", "ApiKey " + esProperties.getApiKey())
        };

        //3.创建RestClient
        RestClient restClient = RestClient
                .builder(httpHost)
                .setDefaultHeaders(authorizations)
                .build();

        //4.创建Transport
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        //5.创建ES客户端,返回
        return new ElasticsearchClient(transport);
    }
}
