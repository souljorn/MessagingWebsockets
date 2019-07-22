package com.messaging.quote;

public class Quote {
    private Long id;
    private String quoteResourceId;
    private String quoteName;

    public Quote() {
        super();
    }

    public Quote(Long id, String quoteResourceId, String quoteName) {
        super();
        this.id = id;
        this.quoteResourceId = quoteResourceId;
        this.quoteName = quoteName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuoteResourceId() {
        return quoteResourceId;
    }

    public void setQuoteResourceId(String quoteResourceId) {
        this.quoteResourceId = quoteResourceId;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }
}

