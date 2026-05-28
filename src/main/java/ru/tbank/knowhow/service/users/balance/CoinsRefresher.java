package ru.tbank.knowhow.service.users.balance;

import ru.tbank.knowhow.model.users.balance.Balance;

public final class CoinsRefresher {

    public void increase(Balance balance, long amount) {
        balance.setCoins(balance.getCoins() + amount);
    }

    public void decrease(Balance balance, long amount) {
        if (balance.getCoins() < amount) {
            throw new IllegalArgumentException("The balance cannot be negative");
        }
        balance.setCoins(balance.getCoins() - amount);
    }
}
