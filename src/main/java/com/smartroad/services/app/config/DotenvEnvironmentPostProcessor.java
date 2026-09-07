package com.smartroad.services.app.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads key=value pairs from a local ".env" file in the project root (if present)
 * into the Spring {@link ConfigurableEnvironment} as a low-priority property source,
 * so local development secrets (DB, SMTP, JWT, etc.) can be supplied via ".env"
 * without ever being committed to version control.
 *
 * Added with lowest precedence: real OS environment variables and JVM system
 * properties (e.g. those set in CI/production) always take priority over ".env".
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenvFile";

    /**
     * Reads ".env" from the current working directory and registers its entries
     * as the lowest-priority property source in the environment.
     *
     * @param environment the environment to contribute properties to
     * @param application the running {@link SpringApplication}
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = Path.of(".env");

        if (!Files.isRegularFile(envFile)) {
            return;
        }

        Map<String, Object> values = readEnvFile(envFile);

        if (!values.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
        }
    }

    /**
     * Parses a ".env" file into key/value pairs, skipping blank lines and comments.
     *
     * @param envFile the path to the ".env" file
     * @return a map of parsed environment entries, empty if the file cannot be read
     */
    private Map<String, Object> readEnvFile(Path envFile) {
        Map<String, Object> values = new LinkedHashMap<>();

        try {
            List<String> lines = Files.readAllLines(envFile);

            for (String rawLine : lines) {
                parseLine(rawLine, values);
            }
        } catch (IOException e) {
            return values;
        }

        return values;
    }

    /**
     * Parses a single ".env" line and, if it is a valid key=value entry, adds it to the map.
     *
     * @param rawLine the raw line from the ".env" file
     * @param values the map to populate with the parsed entry
     */
    private void parseLine(String rawLine, Map<String, Object> values) {
        String line = rawLine.strip();

        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }

        int separator = line.indexOf('=');

        if (separator <= 0) {
            return;
        }

        String key = line.substring(0, separator).strip();
        String value = line.substring(separator + 1).strip();
        value = stripSurroundingQuotes(value);

        values.put(key, value);
    }

    /**
     * Removes a single matching pair of surrounding quotes from a value, if present.
     *
     * @param value the raw value to strip quotes from
     * @return the value without surrounding quotes
     */
    private String stripSurroundingQuotes(String value) {
        boolean isDoubleQuoted = value.length() > 1 && value.startsWith("\"") && value.endsWith("\"");
        boolean isSingleQuoted = value.length() > 1 && value.startsWith("'") && value.endsWith("'");

        if (isDoubleQuoted || isSingleQuoted) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }
}
