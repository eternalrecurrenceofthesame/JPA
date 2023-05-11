package jpabook.jpashop.repository.order.dtoquery1;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이 DTO 는 엔티티와 연관된 toOne 관계의 필드 값을 가져올 때 사용한다.
 */
@Data
@EqualsAndHashCode(of = "orderId") // orderId 값으로 동등성(equals) 비교를 한다.
public class OrderQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems; // DTO 에 넣을 만들 컬렉션 값

    public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
