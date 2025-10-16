package com.bag2bag.st.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交易實體
 */
@Data
public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long itemId;
    private Long buyerId;
    private Long sellerId;
    private LocalDateTime meetTime;
    private String meetPlace;
    private Integer priceCents;
    private Integer status;   // 0..6
    private Integer buyerOk;  // 0/1
    private Integer sellerOk; // 0/1
    private Integer locked;   // 0/1
    private Integer version;  // optimistic lock
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String tradeItem;
    private String mode;
}
