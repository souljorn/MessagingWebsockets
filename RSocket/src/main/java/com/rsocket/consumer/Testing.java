package com.rsocket.consumer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class Testing{

    private static final Logger LOG = LoggerFactory.getLogger(Testing.class);

    private static Server server;

    public Testing() {
    }

    @BeforeClass
    public static void setUpClass() {
        server = new Server();
    }



    @Test
    public void whenSendingAString_thenRevceiveTheSameString() {
        ReqResClient client = new ReqResClient();
        String string = "Hello RSocket";

        assertEquals(string, client.callBlocking(string));

        client.dispose();
    }
}