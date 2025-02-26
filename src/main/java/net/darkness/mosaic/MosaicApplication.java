package net.darkness.mosaic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
import java.nio.file.*;
import java.util.Random;

@SpringBootApplication
@EnableJpaRepositories
public class MosaicApplication {
    public static final String IMAGES_DIRECTORY = "images";
    public static final Random RANDOM_GENERATOR = new Random();

    static {
        try {
            Files.createDirectories(Path.of(IMAGES_DIRECTORY));
        } catch (IOException ignored) {
        }
    }

    public static void main(String... args) {
        SpringApplication.run(MosaicApplication.class, args);
    }
}
