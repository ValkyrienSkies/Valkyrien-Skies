package org.valkyrienskies.mod.common.util.names;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Generates names from a noun list
 */
public class NounListNameGenerator implements NameGenerator {

    private static final int NOUN_LIST_LENGTH = 6801;

    @Getter
    private static NounListNameGenerator instance = new NounListNameGenerator();
    private List<String> nouns = new ArrayList<>(NOUN_LIST_LENGTH);

    @SneakyThrows
    private NounListNameGenerator() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream("assets/valkyrienskies/nounlist.txt"))));

        String line;
        while ((line = reader.readLine()) != null) {
            nouns.add(line);
        }
    }

    @Override
    public String generateName() {
        return ThreadLocalRandom.current()
            .ints(3, 0, nouns.size())
            .mapToObj(nouns::get)
            .collect(Collectors.joining("-"));
    }
}
