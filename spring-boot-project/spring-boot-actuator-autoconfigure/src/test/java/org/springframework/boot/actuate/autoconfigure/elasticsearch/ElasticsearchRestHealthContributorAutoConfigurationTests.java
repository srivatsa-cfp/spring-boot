/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.elasticsearch;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.elasticsearch.ElasticsearchRestClientHealthIndicator;
import org.springframework.boot.actuate.elasticsearch.ElasticsearchRestHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ElasticsearchRestClientAutoConfiguration}.
 *
 * @author Filip Hrisafov
 */
class ElasticsearchRestHealthContributorAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ElasticsearchRestClientAutoConfiguration.class,
					ElasticSearchRestHealthContributorAutoConfiguration.class,
					HealthContributorAutoConfiguration.class));

	@Test
	@SuppressWarnings("deprecation")
	void runShouldCreateIndicator() {
		this.contextRunner.run((context) -> assertThat(context).hasSingleBean(ElasticsearchRestHealthIndicator.class)
				.hasBean("elasticsearchHealthContributor"));
	}

	@Test
	void runWithoutRestHighLevelClientAndWithoutRestClientShouldNotCreateIndicator() {
		this.contextRunner
				.withClassLoader(
						new FilteredClassLoader(org.elasticsearch.client.RestHighLevelClient.class, RestClient.class))
				.run((context) -> assertThat(context).doesNotHaveBean(ElasticsearchRestClientHealthIndicator.class)
						.doesNotHaveBean("elasticsearchHealthContributor"));
	}

	@Test
	@SuppressWarnings("deprecation")
	void runWithoutRestHighLevelClientAndWithRestClientShouldCreateIndicator() {
		this.contextRunner.withUserConfiguration(CustomRestClientConfiguration.class)
				.run((context) -> assertThat(context).hasSingleBean(ElasticsearchRestClientHealthIndicator.class)
						.doesNotHaveBean(ElasticsearchRestHealthIndicator.class)
						.hasBean("elasticsearchHealthContributor"));
	}

	@Test
	@SuppressWarnings("deprecation")
	void runWithRestHighLevelClientAndWithRestClientShouldCreateIndicator() {
		this.contextRunner.withUserConfiguration(CustomRestHighClientConfiguration.class)
				.run((context) -> assertThat(context).hasSingleBean(ElasticsearchRestClientHealthIndicator.class)
						.hasBean("elasticsearchHealthContributor"));
	}

	@Test
	void runWhenDisabledShouldNotCreateIndicator() {
		this.contextRunner.withPropertyValues("management.health.elasticsearch.enabled:false")
				.run((context) -> assertThat(context).doesNotHaveBean(ElasticsearchRestClientHealthIndicator.class)
						.doesNotHaveBean("elasticsearchHealthContributor"));
	}

	@Configuration(proxyBeanMethods = false)
	static class CustomRestClientConfiguration {

		@Bean
		RestClient customRestClient(RestClientBuilder builder) {
			return builder.build();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@SuppressWarnings("deprecation")
	static class CustomRestHighClientConfiguration {

		@Bean
		org.elasticsearch.client.RestHighLevelClient customRestHighClient(RestClientBuilder builder) {
			return new org.elasticsearch.client.RestHighLevelClient(builder);
		}

		@Bean
		RestClient customClient(org.elasticsearch.client.RestHighLevelClient restHighLevelClient) {
			return restHighLevelClient.getLowLevelClient();
		}

	}

}
