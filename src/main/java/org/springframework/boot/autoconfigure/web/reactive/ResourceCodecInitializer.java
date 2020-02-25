package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.codec.ResourceDecoder;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.ResourceHttpMessageWriter;

/**
 * {@link ApplicationContextInitializer} adapter for registering {@link Resource} codecs.
 */
public class ResourceCodecInitializer extends AbstractCodecInitializer {

	public ResourceCodecInitializer(boolean isClientCodec) {
		super(isClientCodec);
	}

	@Override
	protected void register(GenericApplicationContext context, CodecConfigurer configurer) {
		configurer.customCodecs().writer(new ResourceHttpMessageWriter());
		configurer.customCodecs().decoder(new ResourceDecoder());
	}
}
