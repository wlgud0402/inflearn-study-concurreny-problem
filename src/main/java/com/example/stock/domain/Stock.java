package com.example.stock.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    // 낙관적락을 사용하기 위한 버전 컬럼
    @Version
    private Long version;

    public Stock() {
    }

    public Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public void decrease(Long quantity){
        if(this.quantity - quantity < 0){
            throw new RuntimeException("foo");
        }

        this.quantity -= quantity;
    }
}
