package br.org.soujava.coffewithjava.jokenpo.server;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public record Message(Type type, Map<Field, String> data) {

    private static final Jsonb jsonb = JsonbBuilder.create();

    public String toJson() {
        return jsonb.toJson(this);
    }

    public static Message fromJson(String json) {
        return jsonb.fromJson(json, Message.class);
    }


    public static enum Field {

        error,
        gameId,
        opponentName,
        opponentMovement,
        movement;

        void setValue(Message message, String value) {
            getData(message).merge(this, value, (oldValue, newValue) -> newValue);
        }

        Map<Field, String> getData(Message message) {
            return Optional.ofNullable(message.data()).orElseGet(HashMap::new);
        }

        String get(Message message) {
            return getOptional(message).orElse(null);
        }

        Optional<String> getOptional(Message message) {
            return Optional.ofNullable(getData(message).get(this));
        }

        public static MessageSetter messageSetter(Message message) {
            return new MessageSetter(message);
        }
    }

    public record MessageSetter(Message message) {

        MessageSetter set(Field field, String value) {
            field.setValue(this.message, value);
            return new MessageSetter(message);
        }

        MessageSetter unset(Field field) {
            field.setValue(this.message, null);
            return new MessageSetter(message);
        }
    }

    public static enum Type {

        CONNECT,
        CONNECTED,
        NEW_GAME,
        WAITING_PLAYERS,
        PLAY_GAME,
        ERROR,
        GAME_INVALID,
        GAME_READY,
        GAME_RUNNING,
        GAME_OVER_ABANDONED,
        GAME_OVER_YOU_WIN,
        GAME_OVER_YOU_LOSE,
        GAME_OVER_DRAW;

        @SafeVarargs
        final Message build(Consumer<MessageSetter>... consumers) {
            var consumer = Arrays.stream(consumers)
                    .filter(Objects::nonNull)
                    .reduce(m -> {
                    }, Consumer::andThen);
            LinkedHashMap<Field, String> data = new LinkedHashMap<>();
            Message message = new Message(this, data);
            consumer.accept(new MessageSetter(message));
            return message;
        }
    }
}
