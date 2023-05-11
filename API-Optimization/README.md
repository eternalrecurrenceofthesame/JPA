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
public List<SimpleOrderDto> orders(){
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

컬렉션을 조회할 때 쿼리를 최적화 하기. 컬렉션 값이 많으면 그만큼 쿼리 수가 증가한다 N + 1 의 문제가 발생할 수 있기 때문에 컬렉션을 

조회할 때는 신중하게 쿼리를 작성해야 한다. 

### 컬렉션을 페치 조인 해서 가져오는 쿼리
```
public List<Order> findAllWithItem(){
        return em.createQuery(
              "select distinct o from Order o" + 
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" + // 컬렉션
                " join fetch oi.item i", Order.class) // 컬렉션 필드 조회 
                .getResultList();

주문을 가져오면서 컬렉션으로 인한 중복 Order 를 distinct 로 제거하고 관련 필드(컬렉션의 필드 포함) 값들을 모두 페치조인으로 가져온다.
이로써 SQL 을 한번만 실행할 수 있다.

하지만 컬렉션을 페치 조인하면 **페이징**을 할 수 없다. 컬렉션 값을 페이징 시도하면 애플리케이션 메모리에서 모든 데이터베이스 값을 
읽어서 페이징을 시도하기 때문에 위험하다. 또한 컬렉션 페치 조인은 1 개의 필드 값만 사용할 수있다.
```
### 컬렉션 최적화 하기 1 

대부분의 페이징 + 컬렉션 엔티티 조회는 이 방법으로 해결할 수 있다. 
```
앞서 설명 했듯이 컬렉션을 페치조인 하면 페이징을 할 수 없다. 일대 다 의 관계에서는 페이징을 일 엔티티를 기준으로 하는데 컬렉션을 
가져오면 일 엔티티에 컬렉션 값이 포함되면 컬렉션인 다를 기준으로 페이징 되어 버린다.
```
```
* 컬렉션 조회 로직

@GetMapping("/api/orders")
public List<OrderDto> orders(@RequestParam(value = "offset", defaultValue = "0") int offset,
                             @RequestParam(value = "limit", defaultValue = "100") int limit){

List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
List<OrderDto> result = orders.stream().map(o -> new OrderDto(o)).collect(toList());

return result;     }
```
```
* 컬렉션 최적화 하기

public List<Order> findAllWithMemberDelivery(int offset, int limit){
        return em.createQuery(
              "select o from Order o" + 
              " join fetch o.member m" +
              "join fetch o.delivery d" , Order.class)
              .setFirstResult(offset)
              .setMaxResults(limit).getResultList(); }
              
ToOne 관계를 페치 조인으로 한번에 가져온 후 컬렉션을 지연 로딩으로 조회하면 된다. 컬렉션 조회시 배치 사이즈를 설정하면 
한번의 쿼리로 필요한 양 만큼 가져올 수 있고, 1 + 1 총 두 번의 쿼리로 컬렉션을 가져오면서 컬렉션 페이징까지 할 수 있다.
```
```
* 컬렉션 배치 사이즈 설정

spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 1000 // 권장 사이즈 100 ~ 1000 

SQL IN 절을 사용해서 컬렉션 수 만큼 인 쿼리를 사용해서 데이터를 가져온다. 1000 으로 설정하는 것이 성능상 좋다. 
데이터베이스에 과부하를 주지 않는 선에서 설정한다. 

배치 사이즈를 개별 설정하려면 컬렉션 필드나 엔티티에 @BatchSize 옵션을 사용해서 세밀하게 조정할 수있다.
```
### 컬렉션 최적화 하기 (DTO 를 이용한 직접 조회)

#### DTO 조회 1 
```
* dtoquery 1 참고 

루트 엔티티 조회 쿼리 1번, 엔티티별 컬렉션 조회 쿼리 N 번으로 데이터를 가져온다.
```
#### DTO 조회 2
```
* dtoquery 2 참고 

루트 엔티티 조회 쿼리 1 번, 컬렉션 조회 쿼리 1 번으로 데이터를 가져온다.
```
#### DTO 조회 3
```
쿼리 한번으로 데이터를 가져올 수 있다. (하지만 2 번 방법과 비교해서 성능 차이가 미비하며 컬렉션을 사용할 수 없다)
필요시 교재 참고.
```
### 컬렉션 조회 권장 순서
```
엔티티 방식으로 접근한다. 엔티티 조회 방식으로 해결되지 않는다면 DTO 방식 조회를 사용하고 이 방법으로도 안 된다면
JdbcTemplate 이나 NativeSQL 을 사용한다 (보통 JDBCTemplate 을 사용함)
```
#### + DTO 조회 방식 선택하기
```
조회할 루트 엔티티가 하나이고 컬렉션을 같이 조회해야한다면 dtoquery 1 방식을 사용하면 된다. 이런 경우 컬렉션을 
찾기 위한 쿼리도 1번만 실행된다. (1 + 1) 

하지만 조회할 루트 엔티티가 1000 건 정도 된다면 dtoquery 2 방식을 사용해야 한다. 루트 엔티티를 한번에 조회하는 것은 똑같지만 
루트당 컬렉션을 조회해야 하면 1 방식은 1000 번의 쿼리가 발생하기 때문에 컬렉션을 한번에 가져올 수 있는 2 번 방식을 사용한다.

dtoquery 3 의 방식은 쿼리 한번으로 모든 값을 다 가져오지만 페이징을 사용할 수 없다. 필요시 교재를 참고하자.
```

## API 개발 고급 - 실무 필수 최적화
```
* OSIV 의 동작

서비스 계층에서 트랜잭션을 시작할 때 영속성 컨텍스트에서 DB 커넥션을 가져온다. OSIV 설정이 켜져있으면 
@Transactional 이 끝나고 응답이 나갈때까지 즉 고객의 요청에서 응답까지 영속성 컨텍스트가 생존하게 된다.

컨트롤러에서도 영속성 컨텍스트와 관련된 작업을 할 수가 있게된다. (지연 로딩)

영속성 컨텍스트의 생명주기는 데이터베이스 커넥션이 유지될 때까지이며, 지연 로딩은 영속성 컨텍스트가 살아 있어야
할 수 있다.

OSIV 를 사용하면 레이지 로딩 같은 기술을 컨트롤러나 뷰에서 활용 할 수 있게 된다.
```
```
* OSIV 전략의 문제점

OSIV 를 사용하면 오랜시간 데이터베이스 커넥션을 가지고 있어야 하기 때문에 실시간 트래픽이 중요한 애플리케이션에서는
커넥션이 모자랄 수 있고 애플리케이션 문제로 이어질 수 있다.
```
```
* OSIV OFF 전략

트랜잭션이 끝나는 서비스 계층에서 영속성 컨텍스트도 끝나고 데이터베이스 커넥션도 반환된다. 그렇기 때문에 레이지 로딩을 기반으로 
성능을 최적화하는 API 코드를 모두 트랜잭션 안으로 넣어서 사용해야 한다.
(트랜잭션이 끝나기 전 지연 로딩을 강제로 호출해서 값을 받아온다!)
```

### 커멘드와 쿼리를 분리해서 문제 해결하기

실무에서 OSIV 를 끈 상태로 복잡성을 관리하는 좋은 방법으로 Command 용 service 와 Query 용 서비스를 분리하는 방법이 있다. (CQS)
```
* CQS 란?

데이터를 변경 수정하는 커멘드와 데이터를 조회하는 쿼리는 서로 같지 않다는 것을 의미한다. 비유하자면 질문을 하는것이 
대답을 바꾸지 않는 것처럼 조회를 하는 것은 해당 데이터를 변경하지 않기 때문에 데이터의 변경, 수정과 조회는 서로 분리해서 
관리할 수 있다는 개념이다.

좀 더 정확하게 말하자면 메서드는 값을 반환할 때 referentially transparent ? 한 경우만 값을 반환하며 그 결과 사이드 이펙트가 없게 된다. 

It states that every method should either be a command that performs an action, or a query that returns data to the caller, 
but not both. In other words, asking a question should not change the answer.[1] More formally, methods should return a value 
only if they are referentially transparent and hence possess no side effects.

https://en.wikipedia.org/wiki/Command–query_separation 참고
```
```
* CQS 를 애플리케이션에 적용하기

엔티티를 등록 및 수정하는 서비스 (핵심 비즈니스 로직) 와 화면을 출력하기 위한 조회용 서비스 (컬렉션 지연로딩) 두 개를 만들어서 
사용하면 된다. 이때 조회용 서비스는 읽기 전용 트랜잭션으로 만들어서 사용한다.

이렇게 두 개를 분리해서 트랜잭션 내에서 사용하게 되면 영속성 컨텍스트를 유지하면서 컬렉션 지연로딩을 초기화 한 값을 화면 계층에서 
사용할 수 있게 된다

ex)
OrderService: 핵심 비즈니스 로직
OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
```
