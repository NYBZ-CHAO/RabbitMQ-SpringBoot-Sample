package com.zysaaa.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * created by zc  2020/12/30 16:12
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RabbitTest {

    @Autowired
    private Client client;


    @Test
    public void sendAndReceiveTest() throws IOException {
        client.sendUsingReceiveQueue();
    }

}
