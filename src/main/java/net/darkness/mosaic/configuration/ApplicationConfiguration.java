package net.darkness.mosaic.configuration;

import org.springframework.context.annotation.*;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public IDialect conditionalCommentDialect() {
        return new Java8TimeDialect();
    }
}
