package com.example.stock.transaction;

import com.example.stock.service.StockService;
import lombok.RequiredArgsConstructor;

/**
 * 트랜잭셔널 어노테이션이 붙은 메서드나 클래스를 프록시객체로 생성하고 이를 사용함
 * startTransaction 이 실제 메소드보다 먼저 실행되기 때문에 트랜잭셔널 어노테이션이 붙은 메서드를 단순히 syncronized 로 동시성을 해결할 순 없다.
 * */
@RequiredArgsConstructor
public class TransactionStockService {
    private final StockService stockService;

    public void decrease(Long id, Long quantity){
        startTransaction();

        stockService.decrease(id, quantity);

        endTransaction();
    }

    public void startTransaction(){

    }

    public void endTransaction(){

    }
}
