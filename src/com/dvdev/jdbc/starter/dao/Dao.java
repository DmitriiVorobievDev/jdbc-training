package com.dvdev.jdbc.starter.dao;

import com.dvdev.jdbc.starter.dto.TicketFilter;
import com.dvdev.jdbc.starter.entity.Ticket;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс содержит методы  для CRUD-операций
 * id и сущности(return-type и параметры) в методах параметризуются
 */

public interface Dao<K, E> { //K - key, E -entity

//    boolean delete(Long id);
    boolean delete(K id);

//    Ticket save(Ticket ticket);
    Ticket save(E ticket);

//    void update(Ticket ticket);
    void update(E ticket);

//    List<Ticket> findAll();
    List<E> findAll();

    Optional<E> findById(K id);
}
