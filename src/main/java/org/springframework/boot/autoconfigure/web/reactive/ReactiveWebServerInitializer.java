package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.server.ConfigurableReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.support.HandlerFunctionAdapter;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.function.server.support.ServerResponseResultHandler;
import org.springframework.web.reactive.result.SimpleHandlerAdapter;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
import org.springframework.web.reactive.result.view.ViewResolutionResultHandler;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.ArrayList;

import static org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.EnableWebFluxConfiguration;
import static org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.WebFluxConfig;
import static org.springframework.web.server.adapter.WebHttpHandlerBuilder.LOCALE_CONTEXT_RESOLVER_BEAN_NAME;
import static org.springframework.web.server.adapter.WebHttpHandlerBuilder.SERVER_CODEC_CONFIGURER_BEAN_NAME;
import static org.springframework.web.server.adapter.WebHttpHandlerBuilder.WEB_HANDLER_BEAN_NAME;
import static org.springframework.web.server.adapter.WebHttpHandlerBuilder.applicationContext;

/**
 * {@link ApplicationContextInitializer} adapter for Reactive webflux server auto-configurations.
 */
public class ReactiveWebServerInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	private ServerProperties serverProperties;

	private ResourceProperties resourceProperties;

	private ConfigurableReactiveWebServerFactory serverFactory;

	private WebFluxProperties webFluxProperties;

	public ReactiveWebServerInitializer(ServerProperties serverProperties, ResourceProperties resourceProperties, WebFluxProperties webFluxProperties, ConfigurableReactiveWebServerFactory serverFactory) {
		this.serverProperties = serverProperties;
		this.resourceProperties = resourceProperties;
		this.webFluxProperties = webFluxProperties;
		this.serverFactory = serverFactory;
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		context.registerBean("webServerFactoryCustomizerBeanPostProcessor", WebServerFactoryCustomizerBeanPostProcessor.class, WebServerFactoryCustomizerBeanPostProcessor::new);

		context.registerBean(ReactiveWebServerFactoryCustomizer.class, () -> new ReactiveWebServerFactoryCustomizer(this.serverProperties));
		context.registerBean(ConfigurableReactiveWebServerFactory.class, () -> serverFactory);
		context.registerBean(ErrorAttributes.class, () -> new DefaultErrorAttributes(serverProperties.getError().isIncludeException()));
		context.registerBean(ErrorWebExceptionHandler.class,  () -> {
			ErrorWebFluxAutoConfiguration errorConfiguration = new ErrorWebFluxAutoConfiguration(this.serverProperties);
			return errorConfiguration.errorWebExceptionHandler(context.getBean(ErrorAttributes.class), this.resourceProperties, context.getBeanProvider(ViewResolver.class), context.getBean(SERVER_CODEC_CONFIGURER_BEAN_NAME, ServerCodecConfigurer.class), context);
		});
		context.registerBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class, () -> new EnableWebFluxConfigurationWrapper(context, webFluxProperties));
		context.registerBean(LOCALE_CONTEXT_RESOLVER_BEAN_NAME, LocaleContextResolver.class, () -> context.getBean(EnableWebFluxConfigurationWrapper.class).localeContextResolver());
		context.registerBean("responseStatusExceptionHandler", WebExceptionHandler.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).responseStatusExceptionHandler());

		context.registerBean(RouterFunctionMapping.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).routerFunctionMapping(context.getBean(SERVER_CODEC_CONFIGURER_BEAN_NAME, ServerCodecConfigurer.class)));
		context.registerBean(SERVER_CODEC_CONFIGURER_BEAN_NAME, ServerCodecConfigurer.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).serverCodecConfigurer());
		context.registerBean("webFluxAdapterRegistry", ReactiveAdapterRegistry.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).webFluxAdapterRegistry());
		context.registerBean("handlerFunctionAdapter", HandlerFunctionAdapter.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).handlerFunctionAdapter());
		context.registerBean("responseBodyResultHandler", ResponseBodyResultHandler.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).responseBodyResultHandler(context.getBean("webFluxAdapterRegistry", ReactiveAdapterRegistry.class), context.getBean(SERVER_CODEC_CONFIGURER_BEAN_NAME, ServerCodecConfigurer.class), context.getBean("webFluxContentTypeResolver", RequestedContentTypeResolver.class)));
		context.registerBean("responseEntityResultHandler", ResponseEntityResultHandler.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).responseEntityResultHandler(context.getBean("webFluxAdapterRegistry", ReactiveAdapterRegistry.class), context.getBean(SERVER_CODEC_CONFIGURER_BEAN_NAME, ServerCodecConfigurer.class), context.getBean("webFluxContentTypeResolver", RequestedContentTypeResolver.class)));
		context.registerBean("webFluxContentTypeResolver", RequestedContentTypeResolver.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).webFluxContentTypeResolver());
		context.registerBean("webFluxConversionService", FormattingConversionService.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).webFluxConversionService());
		context.registerBean("requestMappingHandlerAdapter", RequestMappingHandlerAdapter.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).requestMappingHandlerAdapter(context.getBean("webFluxAdapterRegistry", ReactiveAdapterRegistry.class), context.getBean(SERVER_CODEC_CONFIGURER_BEAN_NAME, ServerCodecConfigurer.class), context.getBean("webFluxConversionService", FormattingConversionService.class), context.getBean("webFluxValidator", Validator.class)));
		context.registerBean("serverResponseResultHandler", ServerResponseResultHandler.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).serverResponseResultHandler(context.getBean(SERVER_CODEC_CONFIGURER_BEAN_NAME, ServerCodecConfigurer.class)));
		context.registerBean("simpleHandlerAdapter", SimpleHandlerAdapter.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).simpleHandlerAdapter());
		context.registerBean("viewResolutionResultHandler", ViewResolutionResultHandler.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).viewResolutionResultHandler(context.getBean("webFluxAdapterRegistry", ReactiveAdapterRegistry.class), context.getBean("webFluxContentTypeResolver", RequestedContentTypeResolver.class)));
		context.registerBean("webFluxValidator", Validator.class, () -> context.getBean("fuWebFluxConfiguration", EnableWebFluxConfigurationWrapper.class).webFluxValidator());
		context.registerBean(HttpHandler.class, () -> applicationContext(context).build());
		context.registerBean(WEB_HANDLER_BEAN_NAME, DispatcherHandler.class, () -> new DispatcherHandler());
		context.registerBean(WebFluxConfig.class, () -> new WebFluxConfig(resourceProperties, webFluxProperties, context, context.getBeanProvider(HandlerMethodArgumentResolver.class), context.getBeanProvider(CodecCustomizer.class),
			context.getBeanProvider(ResourceHandlerRegistrationCustomizer.class), context.getBeanProvider(ViewResolver.class)));
	}

	private class EnableWebFluxConfigurationWrapper extends EnableWebFluxConfiguration {

		public EnableWebFluxConfigurationWrapper(GenericApplicationContext context, WebFluxProperties webFluxProperties) {
			super(webFluxProperties, context.getBeanProvider(WebFluxRegistrations.class));
			setConfigurers(new ArrayList<>(context.getBeansOfType(WebFluxConfigurer.class).values()));
		}

		@Override
		public ServerCodecConfigurer serverCodecConfigurer() {
			ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
			configurer.registerDefaults(false);
			getApplicationContext().getBeanProvider(CodecCustomizer.class)
					.forEach((customizer) -> customizer.customize(configurer));
			return configurer;
		}
	}

}
