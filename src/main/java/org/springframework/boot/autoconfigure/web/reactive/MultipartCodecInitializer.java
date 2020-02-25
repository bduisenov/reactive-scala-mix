package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageWriter;
import org.springframework.http.codec.multipart.SynchronossPartHttpMessageReader;

/**
 * {@link ApplicationContextInitializer} adapter for registering multipart codecs.
 */
public class MultipartCodecInitializer extends AbstractCodecInitializer {

	public MultipartCodecInitializer(boolean isClientCodec) {
		super(isClientCodec);
	}

	@Override
	protected void register(GenericApplicationContext context, CodecConfigurer configurer) {
		configurer.customCodecs().writer(new MultipartHttpMessageWriter());
		if (!isClientCodec) {
			configurer.customCodecs().reader(new MultipartHttpMessageReader(new SynchronossPartHttpMessageReader()));
		}
	}
}
