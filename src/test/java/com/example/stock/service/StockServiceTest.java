package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
//    private StockService stockService;
    private PessimisticLockStockService stockService;


    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before(){
        Stock stock = new Stock(1L, 100L);
        stockRepository.save(stock);
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void stock_decrease(){
        // when
        stockService.decrease(1L, 1L);

        // then
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(99, stock.getQuantity());
    }

    @Test
    @DisplayName("동시에 100개의 요청이 들어올 경우")
    public void same_time_100_request() throws InterruptedException {
        // given
        int threadCount = 100;
        // 멀티스레드를 위한 ExecutorService 사용 ( ExecutorService : 비동기로 실행하는 작업을 단순화하여 사용할수 있게 도와주는 자바의 API )
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 100개의 요청이 끝날때까지 기다려야 하므로 CountDownLatch 사용
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for(int i = 0; i < threadCount; i++){
            executorService.submit(()->{
                try{
                    stockService.decrease(1L, 1L);
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();

        // then
        assertEquals(0L, stock.getQuantity());
    }



}