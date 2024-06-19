package com.nashss.se.noteworthy.services.dynamodb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DynamoDbClientProviderTest {
    @Test
    public void getDynamoDBClient_nullRegion_throwsException() {
        // GIVEN + WHEN + THEN
        assertThrows(IllegalArgumentException.class,
                () -> DynamoDbClientProvider.getDynamoDBClient(null));
    }
}
