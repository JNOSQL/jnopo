package br.org.soujava.coffewithjava.jokenpo.server;

import java.util.Map;

public record PlayerData(String sessionId, Map<String, String> data) {
}