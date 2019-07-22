package com.quote.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteService {
    private QuoteRepository quoteRepository;

    @Autowired
    public QuoteService(QuoteRepository quoteRepository){
        super();
        this.quoteRepository = quoteRepository;
    }

    public List<Quote> getAllQuotes(){
        List<Quote> quotes = new ArrayList<>();
        this.quoteRepository.findAll().forEach(quotes::add);
        return quotes;
    }
}