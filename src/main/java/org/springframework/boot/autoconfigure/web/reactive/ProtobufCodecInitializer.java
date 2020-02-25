package org.scalalang.boot.reactive;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.protobuf.ProtobufDecoder;
import org.springframework.http.codec.protobuf.ProtobufEncoder;

/**
 * {@link ApplicationContextInitializer} adapter for registering Protobuf codecs.
 * @see ProtobufEncoder
 * @see ProtobufDecoder
 */
public class ProtobufCodecInitializer extends AbstractCodecInitializer {


	public ProtobufCodecInitializer(boolean isClientCodec) {
		super(isClientCodec);
	}

	@Override
	protected void register(GenericApplicationContext context, CodecConfigurer configurer) {
		configurer.customCodecs().encoder(new ProtobufEncoder());
		configurer.customCodecs().decoder(new ProtobufDecoder());

	}
}
