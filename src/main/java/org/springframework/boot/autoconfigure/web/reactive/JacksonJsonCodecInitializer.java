package org.springframework.boot.autoconfigure.web.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.ServerSentEventHttpMessageWriter;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

/**
 * {@link ApplicationContextInitializer} adapter for registering Jackson JSON codecs.
 */
public class JacksonJsonCodecInitializer extends AbstractCodecInitializer {

	public JacksonJsonCodecInitializer(boolean isClientCodec) {
		super(isClientCodec);
	}

	@Override
	protected void register(GenericApplicationContext context, CodecConfigurer configurer) {
		ObjectMapper mapper = context.getBean(ObjectMapper.class);
		Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(mapper);
		configurer.customCodecs().decoder(new Jackson2JsonDecoder(mapper));
		configurer.customCodecs().encoder(encoder);
		configurer.customCodecs().writer(new ServerSentEventHttpMessageWriter(encoder));
	}
}
