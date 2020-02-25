package org.springframework.boot.autoconfigure.web.reactive.function.client;

import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ApplicationContextInitializer} adapter for {@link WebClientAutoConfiguration}.
 */
public class ReactiveWebClientBuilderInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	private final String baseUrl;

	public ReactiveWebClientBuilderInitializer(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		context.registerBean(WebClient.Builder.class, () -> new WebClientAutoConfiguration().webClientBuilder(context.getBeanProvider(WebClientCustomizer.class)));
		context.registerBean(DefaultWebClientCodecCustomizer.class, () -> new DefaultWebClientCodecCustomizer(this.baseUrl, new ArrayList<>(context.getBeansOfType(CodecCustomizer.class).values())));
	}

	/**
	 * Variant of {@link WebClientCodecCustomizer} that configure empty default codecs by defaults
	 */
	static public class DefaultWebClientCodecCustomizer implements WebClientCustomizer {

		private final List<CodecCustomizer> codecCustomizers;

		private final String baseUrl;

		public DefaultWebClientCodecCustomizer(String baseUrl, List<CodecCustomizer> codecCustomizers) {
			this.codecCustomizers = codecCustomizers;
			this.baseUrl = baseUrl;
		}

		@Override
		public void customize(WebClient.Builder builder) {
			builder.exchangeStrategies(ExchangeStrategies.empty()
							.codecs(codecs -> this.codecCustomizers
									.forEach((customizer) -> customizer.customize(codecs))).build());
			if (this.baseUrl != null) {
				builder.baseUrl(this.baseUrl);
			}
		}
	}
}
