package com.messaging.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TransferQuoteProcessor {
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferQuoteProcessor.class);

    @Autowired
    public TransferQuoteProcessor(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
    }

    public void receiveMessage(String message) {
        LOGGER.info("Message Received");
        try {
            Quote quote = this.objectMapper.readValue(message, Quote.class);
            LOGGER.info("Quote object created");
        } catch (IOException e) {
            LOGGER.error("Exception caught", e);
        }
    }
}

