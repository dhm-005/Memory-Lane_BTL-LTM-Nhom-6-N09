package com.mcserver.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcserver.service.game.GameMatchService;

@ExtendWith(MockitoExtension.class)
class TcpRequestHandlerTest {
    @Mock
    private GameMatchService gameMatchService;

    private TcpRequestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TcpRequestHandler(new ObjectMapper(), gameMatchService);
    }

    @Test
    void healthRequestChecksTheRepositoryThroughTheService() {
        when(gameMatchService.countMatches()).thenReturn(3L);

        TcpResponse response = handler.handle("{\"action\":\"health\"}");

        assertTrue(response.ok());
        assertEquals(Map.of("status", "UP", "matchCount", 3L), response.data());
        verify(gameMatchService).countMatches();
    }

    @Test
    void malformedJsonReturnsAProtocolError() {
        TcpResponse response = handler.handle("{not-json}");

        assertFalse(response.ok());
        assertEquals("INVALID_JSON", response.errorCode());
    }

    @Test
    void createMatchRequiresAnIntegerJoinCode() {
        TcpResponse response = handler.handle("{\"action\":\"matches.create\",\"joinCode\":\"abc\"}");

        assertFalse(response.ok());
        assertEquals("INVALID_REQUEST", response.errorCode());
    }
}
