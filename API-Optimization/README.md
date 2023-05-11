# API 개발과 성능 최적화

API 개발에 JPA 를 사용하면서 발생하는 쿼리 성능을 최적화 하는 방법! (지연 로딩으로 발생하는 문제 해결)

## 엔티티를 DTO 로 반환하기
```
참고로 엔티티는 항상 외부로 노출하면 안된다 엔티티가 외부로 노출될 경우 API 스펙이 변경될 수 있다.
DDD 의 관점에서도 저수준 모듈이 고수준 모듈에 의존하는 모양이 되기 때문에 엔티티는 DTO 로 감싸서 사용해야 한다.
```
```
* DTO  예시 

@Data
static class SimpleOrderDto{

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
        }
    }
```
### 페치 조인을 사용한 성능 최적화
```
* RestController

@GetMapping("/api/fetch/simple-orders")
public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;}

stream.map 을 이용해서 찾은 order 를 dto 로 쉽게 반환할 수 있다. 
```
```
* Repository

 public List<Order> findAllWithMemberDelivery(){
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .getResultList();
        )}

join fetch 를 사용하면 연관된 LAZY 필드의 프록시값을 한번에 가지고 온다. 총 쿼리 1 번
```

### JPA 에서 DTO 로 바로 조회해오기
```
Repository 에서 값을 찾을 때 바로 DTO 로 감싸서 반환하는 것도 가능하다. 

public List<Dto> findOrderDtos(){
       return em.createQuery(
               "select new DTO 의 상세 경로(매핑할 필드 값)" +
                       " from Order o" +
                       " join o.member m" +
                       " join o.delivery d", Dto.class)
               .getResultList();}

필드 값을 매핑할 때는 순서에 유의해야 한다!
```
### 엔티티를 DTO 로 반환하는 매뉴얼
```
1. 페치 조인을 사용한다.(여기서 대부부분 해결된다.)
2. 성능이 나오지 않으면 DTO 방식을 사용한다(크게 효과적이진 않다.)

3. 위 두 가지 방법으로 해결되지 않으면 네이티브 SQL 이나 JDBC Template 을 사용한다
(보통 JDBC Template 을 사용한다.)
```

## 컬렉션 최적화 하기 

컬렉션을  조회할 때 쿼리를 최적화 하기.
