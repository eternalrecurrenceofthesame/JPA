package jpabook.jpashop.repository.order.dtoquery1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 컬렉션 값을 매핑하는 DTO 로써 루트 엔티티를 조회한 후 컬렉션 값을 한번에 매핑할 때 사용한다.
 *
 * Order,Item 다대 다 관계의 중간 테이블을 매핑하는 DTO 임.
 */
@Data
@EqualsAndHashCode(of = "orderId")
public class OrderItemQueryDto {


    @JsonIgnore // OrderQueryDto 의 주문 번호와 양방향 참조가 걸리기 때문에 JsonIgnore 를 설정한다.
    private Long orderId; // 주문 번호 (OrderItem 중간 테이블에서 가져온 주문 번호)

    private String itemName; // 상품명
    private int orderPrice;
    private int count;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
