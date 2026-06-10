package ru.tbank.knowhow.core_service.service.event;

@FunctionalInterface
interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;
}
