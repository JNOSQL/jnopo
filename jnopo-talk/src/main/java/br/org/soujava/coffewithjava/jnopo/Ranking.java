package br.org.soujava.coffewithjava.jnopo;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Ranking(Map<String, Integer> data) {
    @WithSpan
    public static Ranking winnerRanking(Playoffs playoffs) {
        return of(playoffs, g -> g.winner().name());
    }

    private static Ranking of(Playoffs playoffs, Function<GameMatch, String> groupingFunction) {
        var data =
                playoffs.listPlayoffsWithWinnerAndLoser()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        groupingFunction,
                                        Collectors.collectingAndThen(Collectors.toList(), Collection::size))
                        ).entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return new Ranking(data);
    }
}
