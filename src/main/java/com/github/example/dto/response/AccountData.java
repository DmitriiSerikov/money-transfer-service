package com.github.example.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;


public class AccountData implements Serializable {
    private static final long serialVersionUID = -4311698332553337798L;

    private long id;
    private BigDecimal balance;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
