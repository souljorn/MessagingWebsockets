package com.quote.api;

import javax.persistence.*;

@Entity
@Table(name ="QUOTE")
public class Quote {

    @Id
    @Column(name="QUOTE_ID")
    @GeneratedValue
    private long id;
    @Column(name="QUOTE_RESOURCE_ID")
    private String resourceID;
    @Column(name="QUOTE_NAME")
    private String quoteName;

    public Quote(){
        super();
    }
    public Quote(long id, String resourceID, String quoteName) {
        super();
        this.id = id;
        this.resourceID = resourceID;
        this.quoteName = quoteName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }
}
