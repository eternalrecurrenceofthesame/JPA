package jpabook.jpashop.repository.order.dtoquery1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * Order 조회 메커니즘
     *
     * toOne 관계(루트) 를 DTO 매핑을 사용해서 한번에 가져온다.
     * toMany 관계(컬렉션) 을 DTO 매핑을 사용해서 한번에 가져온다.
     *
     * OrderDto 를 루프로 주문 아이디 값을 이용해서 컬렉션을 찾고 OrderDTO 에 매핑한다.
     *
     * Api 에서는 findOrderQueryDtos 만 호출하면 주문과 관련된 아이템을 루트 쿼리 1 번 + 루트 컬렉션 N 번으로 가져올 수있다
     */
    public List<OrderQueryDto> findOrderQueryDtos(){

        /**
         * 1. 루트 조회 (toOne 관계) 첫번째 쿼리로 모든 주문을 가져온다.
         */
        List<OrderQueryDto> result = findOrders();


        /**
         * 2. 두 번째 쿼리로 주문 아이디(사용자) 별로 쿼리를 날린다. 즉 사용자가 주문한 컬렉션 리스트를 한번에 가져온다는 의미임
         *
         * 사용자가 열 명이면 루트 한번으로 주문을 다 가져오고, 사용자별 컬렉션 값을 열 번의 쿼리로 가져오게 된다.
         * 사용자별 주문 컬렉션 수가 많을 때 유용할듯?
         */
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result; // 전체 DTO 반환
    }


    /** Order 와 toOne 관계의 값을 DTO 로 매핑해서 가져오는 메서드 */
    private List<OrderQueryDto> findOrders(){
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.dtoquery1.OrderQueryDto(o.id, m.name, o.orderDate, o.status, o.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    /** 루트 조회 Order 와 toMany 관계에 있는 컬렉션을 DTO 로 매핑해서 가져오는 메서드 */
    private List<OrderItemQueryDto> findOrderItems(Long orderId){
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.dtoquery1.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = : orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();

    }

}
