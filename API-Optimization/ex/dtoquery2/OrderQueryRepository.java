package jpabook.jpashop.repository.order.dtoquery2;

import jpabook.jpashop.repository.order.dtoquery1.OrderItemQueryDto;
import jpabook.jpashop.repository.order.dtoquery1.OrderQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * 루트 1 번, 컬렉션 1 번으로 데이터를 가져오는 메서드
     */
    public List<OrderQueryDto> findAllByDto_optimization(){

        /**
         * 루트 엔티티를 한번에 가져온다.
         */
        List<OrderQueryDto> result = findOrders();

        /**
         * 루트 엔티티 (주문) 의 키 값들을 추출해서, OrderItems 를 (주문 아이디, 주문 아이템) 컬렉션 DTO 값으로 만든다.
         * (쉽게 말해서 모든 주문 키값을 넣고 한번에 주문과 관련된 컬렉션 값들을 다 가져온다고 생각하면 된다.)
         */
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        /**
         * 가져온 컬렉션을 루프로 루트 엔티티의 아이디 값으로 컬렉션을 조회한 후 컬렉션에 추가한다
         * (이 과정에서 쿼리는 발생하지 않는다 이미 값을 다 가져옴)
         */
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }


    /** 루트 조회 Order 와 toOne 관계의 값을 DTO 로 매핑해서 가져오는 메서드 */
    private List<OrderQueryDto> findOrders(){
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.dtoquery1.OrderQueryDto(o.id, m.name, o.orderDate, o.status, o.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }


    /** 조회한 루트 엔티티에서 주문 아이디 값을 추출하는 메서드 */
    private List<Long> toOrderIds(List<OrderQueryDto> result){
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds){

        /**
         * 주문들의 컬렉션을 한번에 가져오는 로직
         */
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.dtoquery1.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in : orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        /**
         * Collectors.groupingBy
         *
         * 조회한 OrderItemQueryDto 값들을 주문 아이디 별로 매핑해서 key, value 쌍의 map 으로 만들어서 반환한다.
         */
        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }

}
