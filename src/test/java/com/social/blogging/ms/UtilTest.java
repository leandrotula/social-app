package com.social.blogging.ms;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class UtilTest {

    public static Stream<Arguments> provideTweetRequestInvalidData() {
        return Stream.of(
                Arguments.of(null, "test tweet"),
                Arguments.of("testuser", ""),
                Arguments.of("", ""),
                Arguments.of(null, null)
        );
    }

    public static Stream<Arguments> provideFollowerRequestInvalidData() {
        return Stream.of(
                Arguments.of(null, "user1"),
                Arguments.of("user2", ""),
                Arguments.of("", ""),
                Arguments.of(null, null)
        );
    }
}
