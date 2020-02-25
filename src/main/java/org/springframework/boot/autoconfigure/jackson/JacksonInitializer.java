package org.springframework.boot.autoconfigure.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.ArrayList;

import static org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration;
import static org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration;
import static org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.JacksonObjectMapperConfiguration;

/**
 * {@link ApplicationContextInitializer} adapter for {@link JacksonAutoConfiguration}.
 */
public class JacksonInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private JacksonProperties properties;

    public JacksonInitializer(JacksonProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize(GenericApplicationContext context) {
        context.registerBean(Jackson2ObjectMapperBuilderCustomizer.class, () -> new Jackson2ObjectMapperBuilderCustomizerConfiguration().standardJacksonObjectMapperBuilderCustomizer(context, this.properties));
        JacksonObjectMapperBuilderConfiguration configuration = new JacksonObjectMapperBuilderConfiguration();
        context.registerBean(Jackson2ObjectMapperBuilder.class, () ->
                configuration.jacksonObjectMapperBuilder(context, new ArrayList<>(context.getBeansOfType(Jackson2ObjectMapperBuilderCustomizer.class).values())));
        context.registerBean(ObjectMapper.class, () -> new JacksonObjectMapperConfiguration().jacksonObjectMapper(context.getBean(Jackson2ObjectMapperBuilder.class)));
    }
}