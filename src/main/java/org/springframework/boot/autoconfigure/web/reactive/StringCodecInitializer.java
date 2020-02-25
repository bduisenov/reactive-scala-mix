package org.scalalang.boot.reactive;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.codec.CodecConfigurer;

/**
 * {@link ApplicationContextInitializer} adapter for registering {@link String} codecs.
 */
public class StringCodecInitializer extends AbstractCodecInitializer {

	private final boolean textPlainOnly;

	public StringCodecInitializer(boolean isClientCodec, boolean textPlainOnly) {
		super(isClientCodec);
		this.textPlainOnly = textPlainOnly;
	}

	@Override
	protected void register(GenericApplicationContext context, CodecConfigurer configurer) {
		configurer.customCodecs().encoder(textPlainOnly ? CharSequenceEncoder.textPlainOnly() : CharSequenceEncoder.allMimeTypes());
		configurer.customCodecs().decoder(textPlainOnly ? StringDecoder.textPlainOnly() : StringDecoder.allMimeTypes());
	}
}
