package com.dvdev.jdbc.starter.dto;

//фильтр по параметрам
public record TicketFilter(int limit,
                           int offset,
                           String passengerName,
                           String seatNo) {

}
