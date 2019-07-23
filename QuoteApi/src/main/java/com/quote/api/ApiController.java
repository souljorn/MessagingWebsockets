package com.quote.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private QuoteService quoteService;

    @Value("${amqp.queue.name}")
    private String queueName;
    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteService.class);
    private final RabbitTemplate rabbitTemplate;
    private final ConfigurableApplicationContext context;
    private final ObjectMapper objectMapper;


    @Autowired
    public ApiController(QuoteService quoteService, RabbitTemplate rabbitTemplate,
                         ConfigurableApplicationContext context, ObjectMapper objectMapper) {
        super();
        this.quoteService = quoteService;
        this.rabbitTemplate = rabbitTemplate;
        this.context = context;
        this.objectMapper = objectMapper;

    }

    @GetMapping("/quotes")
    public List<Quote> getAllQuotes() {
        List<Quote> quotes = this.quoteService.getAllQuotes();
        quotes.forEach(quote -> {
            try {
                String jsonString = objectMapper.writeValueAsString(quote);
                rabbitTemplate.convertAndSend(queueName, jsonString);
            } catch (JsonProcessingException e) {
                LOGGER.error("parsing exception", e);
            }
        });

        return this.quoteService.getAllQuotes();
    }

}
