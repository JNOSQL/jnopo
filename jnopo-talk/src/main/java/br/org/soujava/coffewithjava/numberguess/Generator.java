package br.org.soujava.coffewithjava.numberguess;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.io.Serializable;

@ApplicationScoped
public class Generator implements Serializable {

    private final java.util.Random random = new java.util.Random(System.currentTimeMillis());

    private final int maxNumber = 100;

    java.util.Random getRandom() {
        return random;
    }

    @Produces
    @Random
    int next() {
        // a number between 1 and 100
        return getRandom().nextInt(maxNumber - 1) + 1;
    }

    @Produces
    @MaxNumber
    int getMaxNumber() {
        return maxNumber;
    }
}
