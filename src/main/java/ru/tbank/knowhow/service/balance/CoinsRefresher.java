package ru.tbank.knowhow.service.balance;

import ru.tbank.knowhow.model.Balance;

public class CoinsRefresher {

    public void increase(Balance balance, long amount) {
        balance.setCoins(balance.getCoins() + amount);
    }

    public void decrease(Balance balance, long amount) {
        balance.setCoins(balance.getCoins() - amount);
    }
}
