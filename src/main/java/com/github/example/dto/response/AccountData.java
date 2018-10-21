package com.github.example.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;


public class AccountData implements Serializable {
    private static final long serialVersionUID = -4311698332553337798L;

    private UUID id;
    private BigDecimal balance;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
