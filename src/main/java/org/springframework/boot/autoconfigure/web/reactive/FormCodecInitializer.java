package org.scalalang.boot.reactive;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.FormHttpMessageReader;
import org.springframework.http.codec.FormHttpMessageWriter;

/**
 * {@link ApplicationContextInitializer} adapter for registering Form codecs.
 */
public class FormCodecInitializer extends AbstractCodecInitializer {

	public FormCodecInitializer(boolean isClientCodec) {
		super(isClientCodec);
	}

	@Override
	protected void register(GenericApplicationContext context, CodecConfigurer configurer) {
		configurer.customCodecs().writer(new FormHttpMessageWriter());
		configurer.customCodecs().reader(new FormHttpMessageReader());
	}
}
