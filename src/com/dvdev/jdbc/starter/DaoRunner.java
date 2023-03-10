package com.dvdev.jdbc.starter;

import com.dvdev.jdbc.starter.dao.TicketDao;
import com.dvdev.jdbc.starter.dto.TicketFilter;
import com.dvdev.jdbc.starter.entity.Ticket;

import java.math.BigDecimal;

public class DaoRunner {

    public static void main(String[] args) {
      var ticket = TicketDao.getInstance().findById(5L);
        System.out.println(ticket);
    }

    private static void filterTest() {
        var ticketFilter = new TicketFilter(3, 0, "Евгений Кудрявцев", "A1");
        var tickets = TicketDao.getInstance().findAll(ticketFilter);
        System.out.println(tickets);
    }

    private static void updateTest() {
        var ticketDao = TicketDao.getInstance();
        var maybeTicket = ticketDao.findById(2L);
        System.out.println(maybeTicket);

        maybeTicket.ifPresent(ticket -> {
            ticket.setCost(BigDecimal.valueOf(188.88));
            ticketDao.update(ticket);
        });
    }

    private static void deleteTest(Long id) {
        var ticketDao = TicketDao.getInstance();
        var deleteResult = ticketDao.delete(id);
        System.out.println(deleteResult);
    }

    private static void saveTest() {
        var ticketDao = TicketDao.getInstance();
        //создаем ticket - это одна строка таблицы ticket
        var ticket = new Ticket();
        ticket.setPassengerNo("1234567");
        ticket.setPassengerName("Test");
//        ticket.setFlightId(3L);
        ticket.setSeatNo("B3");
        ticket.setCost(BigDecimal.TEN);
        var savedTicket = ticketDao.save(ticket); //инсертим объект ticket в таблицу ticket
        System.out.println(savedTicket);
    }
}
