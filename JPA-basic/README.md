# 영속성 관리
```
엔티티: JPA 가 관리하는 객체
엔티티 매니저: 영속성 컨텍스트에 접근할 수 있는 매니저

영속성 컨텍스트: 엔티티를 영구히 저장하는 환경
```
## 영속성 컨텍스트에서 지원하는 여러가지 기능들 

영속성 컨텍스트는 애플리케이션과 데이터베이스의 중간에 위치하고 1차 캐시, 동일성 보장, 쓰기 지연, 변경 감지 지연 로딩을 지원한다. 

### 1 차 캐시
```
Member member = new Member("Id1", "회원1");
em.persist(member); // 엔티티 매니저가 엔티티를 영속성 컨텍스트의 1 차 캐시에 저장한다. 

em.find(Member.class, "Id1"); // 1 차 캐시에 저장된 값은 데이터베이스를 거치지 않고 영속성 컨텍스트에서 조회한다.

em.find(Member.class, "Id2"); // 1 차 캐시에 없는 새로운 값을 조회하면 데이터베이스에서 값을 조회한다. 

1 차 캐시를 지원해서 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 
차원에서 제공! 
```
```
반복 가능한 읽기 란? 

REPEATABLE READ: select(공유 락), update(배타 락) // 다른 트랜잭션이 설정한 공유 락은 읽을 수 있지만 배타락은 읽지 못한다!
즉 조회의 경우 조회한 내용을 읽을 수 있지만, 다른 곳에서 값을 수정중이라면 읽을 수 없다. 
```
### 트랜잭션을 지원하는 쓰기 지연
```
em.persist(member1);
em.persist(member2);

em.commit;

JPA 는 엔티티 매니저를 통해 영속성 컨텍스트에 들어온 엔티티들을 바로 바로 데이터베이스로 전송하지 않고 쓰기 지연 SQL 저장소에
저장해둔다. 그리고 엔티티 매니저가 커밋하는 순간 플러시가 일어나고 SQL 들이 한번에 데이터베이스로 반영된다. 

참고로 엔티티 매니저는 데이터 변경시 트랜잭션을 시작하고 작업을 수행한다. 
```
### 엔티티 수정 및 삭제 - 변경 감지(Dirty Checking)
```
em.persist(member); // 영속성 컨텍스트에 엔티티 추가

Member member = emfind(Member.class, 1L);
member.setName("변경");

엔티티 매니저는 커밋 직전에 flush() 를 호출해서 1 차 캐시(pk, 엔티티 스냅샷) 내의 스냅샷과 엔티티를 서로 비교한다.
(스냅샷은 데이터베이스 조회 또는 persist 할 때의 최초 스크린샷을 의미함)

그리고 스냅샷(처음 스크린샷) 과 엔티티 사이의 변경점이 있으면 변경된 값을 데이터베이스에 반영하는데 이것을 변경 감지라고 한다. 
(flush() 를 하면 변경 점을 쓰기 지연 저장소에 등록되고 데이터베이스에 값이 반영된 후 커밋이 발생한다)


Member memberA = em.find(Member.class, 1L);
em.remove(memberA);

변경 감지를 이용하면 엔티티의 삭제도 간단하게 할 수 있다.
```
### 플러시
```
변경감지
수정된 엔티티를 쓰기 지연 SQL 에 저장
쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송(등록,수정,삭제)

플러시는 em.flush() 를 사용해서 직접 호출하거나 커밋 직전에 발생한다 그리고 JPQL 을 실행해도 자동으로 호출된다.
```
```
* 플러시의 특징

플러시는 영속성 컨텍스트를 비우지 않고, 영속성 컨텍스트의 변경 내용을 데이터베이스에 미리 동기화 한다. 즉 커밋 직전에
데이터의 값을 동기화 해서 반영하게 된다. 
```
```
* 준영속 상태란?

영속성 컨텍스트의 영속상태의 엔티티가 영속성 컨텍스트에 분리되어서 영속성 컨텍스트가 제공하는 기능을 사용하지 못하는
상태를 의미한다. 

트랜잭션상태에서 엔티티매니저가 persist 해서 엔티티를 등록하면 영속성 컨텍스트가된다 그리고 캐시에 들어가고 sql 저장소에 
등록되고 플러시가 일어나서 커밋되는 메커니즘이 적용되지 않는 상태를 의미 

em.deatch(entity), em.clear() 컨텍스트 초기화, em.close() 컨텍스트 종료 // 준영속 상태를 만드는 방법 
```
# 엔티티 매핑 

## @Entity
```
JPA 가 관리하는 엔티티 클래스를 만들 때 사용하는 애노테이션으로써 테이블과 직접적으로 매핑된다. 
엔티티 클래스를 만들 때는 기본 생성자가 필수이며, final 클래스를 사용할 수 없고, 저장할 필드에 final 을 사용할 수 없다.

@Entity(name = "테이블 명") name 속성을 사용해서 테이블 명과 일치 시킬 수 있으며 테이블 명을 지정하지 않는다면
클래스 이름이 테이블 명이 되어 데이터베이스 테이블과 매핑된다.
```
## 기본 키 
### @Id
```
@Id // pk 값을 매핑한다.
@GeneratedValue(strategy = GenerationType ) // 타입을 지정해서 전략을 설정할 수 있다.
private Long id;
```
### @GeneratedValue(strategy = GenerationType.IDENTITY)
```
이 전략은 데이터베이스에 기본키 생성을 위임한다. 엔티티를 영속화 시키려면 아이디 값이 필요한데 이 전략을 사용하면 em.persist() 시점에
즉시 SQL 을 실행하고 값을 반영한 후 DB 에서 식별자를 조회한다.
```
### @GeneratedValue(strategy = GenerationType.SEQUENCE)
```
데이터 베이스 시퀀스는 유일한 값을 순서대로 생성하는 데이터베이스 오브젝트로써 오라클에서 사용된다.

@Entity
@SequenceGenerator(name = "MEMBER_SEQ_GENERATOR", // 생성기 이름 
                   sequenceName = "MEMBER_SEQ", // 매핑할 데이터베이스 시퀀스
                   initialValue = 1, // DDL 시에만 적용됨 처음 시작 값 
             allocationSize = 1) // 시퀀스 한번 호출에 증가하는 수 (시퀀스 값이 하나씩 증가하도록 되어 있다면 반드시 1로 설정해야 한다.)
                   그 외 catalog, schema // 데이터베이스 카탈로그, 스키마 이름 
public class Member{
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ_GENERATOR")
private Long id;

initialValue = 1, allocationSize = 1 // 1 부터 시작해서 1씩 증가시킨다. 시퀀스 오브젝트도 데이터베이스에서 관리하기 
때문에 데이터베이스에서 조회해야 알 수 있다. 

멤버 객체를 만들고 컨텍스트에 넣으려면 시퀀스 값을 데이터베이스에서 가져와야하는데 JPA 는 영속화 시키기 전에 쿼리를 날려서 
시퀀스 값을 요청한다. 데이터베이스에서 시퀀스 값을 얻어와서 컨텍스트 캐시 ID 에 값으로 사용하고

영속성 컨텍스트에 저장한다 여기까지 하면 아직 DB 에 인서트 쿼리를 날리지 않은 영속성 상태이고 커밋 시점에 값이 반영된다.

allocationSize 는 한번에 가져올 시퀀스의 값을 나타낸다. 1 로 설정하면 하나씩 가져온다 즉 영속화 시킬 때 마다 네트웤을
통해서 값을 계속해서 요청한다. 이 값을 50 (디폴트 값) 으로 설정하면 데이터베이스에 미리 51 개의 시퀀스 값을 올려두고 사용할 수 있다.

처음 호출할 때 시퀀스 값으로 하나를 받아오면 설정된 디폴트 값이랑 맞지 않기 때문에 50 개의 시퀀스를 요청해서 데이터베이스에
51 개의 시퀀스를 만들어두고 51 개의 시퀀스 값으로 데이터를 영속화 시킨다. 

그리고 할당된 시퀀스 값을 다 쓰면 다음 호출(+ 1) 을 하고 마찬가지로 50 개의 데이터를 받아서 사용할 수 있다.
```
### TABLE 전략
```
테이블 전략이란 애플리케이션 내에서 키 생성용 테이블을 만들고 데이터베이스 시퀀스를 흉내내는 전략이다.
즉 시퀀스를 지원하지 않는 데이터베이스도 애플리케이션 내에서 시퀀스를 만들어서 적용할 수 있다.

보통 테이블 매핑 전략은 잘 사용되지 않는다. (필요시 자료 참고)
```
### 권장하는 식별자 전략
```
null 값을 허용하지 않고 유일하며 변하지 않는 조건을 만족하려면, Long 타입 대체키(오토 인크리먼트, 시퀀스) 생성 전략을 사용한다. 

테이블의 키를 생성할 때는 자연키와 대리키 중 하나를 선택할 수 있는데 비즈니스와 관련된 자연키를 사용하면 
안 된다(ex 주민등록번호, 이메일, 전화번호) 이러한 값들은 언제나 변할 수 있다.

비즈니스와 관련 없는 임의로 생성된 대체 키를 사용하자. (ex 오라클의 시퀀스나 오토 인크리먼트)
```
## 필드와 컬럼 매핑하기

자주 사용하는 매핑 유형을 설명 (필요시 자세히 찾아보기)

### @Column
```
name: 컬럼 이름 지정
```
### @Enumerated
```
@Enumerated(EnumType.STRING) // enum 을 매핑할 때 사용한다
private RoleType roleType;  // ADMIN, USER

enum 을 매핑할 때는 항상 STRING 값을 사용해야 한다. 그렇지 않으면 순서대로 저장하는데 값이 바뀌면 다르게 저장된다.
```
### @Temporal
```
날짜를 매핑할 때 사용하는데 LocalDate, LocalDateTime 을 사용하면 생략할 수 있다 (한마디로 안 쓴다는 의미)
```
# 연관 관계 매핑 기초

객체를 테이블에 맞춰서 테이블 클래스를 만들면 값을 찾을 때 각각의 테이블별로 값을 조회해야 한다. (참조를 사용할 수없음)

JPA 는 애노테이션을 사용해서 객체는 객체대로 만들고, 테이블은 테이블대로 만들어서 참조를 사용할 수 있게 해준다.

## 단방향 연관 관계
```
@Entity 
public class Member{

@ManyToOne
@JoinColumn(name = "TEAM_ID") // 테이블의 외래키 값
private Team team; // Team 클래스가 있다고 가정함.}
```
```
Team team = new Team("TeamA"); em.persist(team);
Member member = new Member("member1"); em.persist(member);

member.setTeam(team); // 단방향 연관 관계만 설정한다. (멤버만 팀을 조회할 수 있음)

Member findMember = em.find(Member.class, member.getId());
findMember.getTeam(); // 연관관계 매핑시 객체 그래프를 탐색할 수 있다. 
```
## 양방향 연관 관계
```
@Entity 
public class Member{

@ManyToOne
@JoinColumn(name = "TEAM_ID") // 테이블의 외래키 값
private Team team; // Team 클래스가 있다고 가정함.}
```
```
@Entity
public class Team{
...
@OneToMany(mappedBy = "team") // 멤버의 팀 필드를 가리킴
List<Member> members = new ArrayList<Member>(); // 필드에서 바로 초기화 한다. }

양방향으로 매핑하면 반대 방향에서도 객체 그래프를 탐색할 수 있다.

Team findTeam = em.find(Team.class, team.getId());
findTeam.getMembers().getSize(); // 역방향 조회! 
```
### 양방향 연관 관계 매핑 상세
```
양방향 연관 관계에서는 연관 관계의 주인을 설정해야 하며 **외래키** 가 있는 곳이 연관 관계의 주인이 된다. 
연관 관계의 주인만이 외래키를 관리할 수 있다(등록, 수정)

연관 관계의 주인이 아니면 mappedBy 속성을 사용해서 연관 관계의 주인을 지정해줘야 하며 조회만 가능
```
```
* 양방향 매핑시 주의할 점

앞서 설명했다시피 연관관계의 주인만 등록과 수정이 가능하다 즉 연관 관계의 주인에서 값을 수정해야 연관 관계로
매핑된다. 순수 객체관계를 고려해서 주인 쪽에서 연관 객체를 등록할 때 연관 관계 편의 메서드를 만들어서 사용할 수도 있다.

team.setMember(member); (x)
member.setTeam(team); (o)

또한 양방향 매핑시 무한 참조를 조심해야 한다. ex) toString(), lombko, json 생성 라이브러리 
```
# 다양한 연관 관계 매핑
```
* 단방향과 양방향의 개념에 대해서

테이블의 경우 외래 키 하나로 양쪽으로 조인할 수 있다. (방향이라는 개념이 없음)

객체는 참조용 필드가 있는 쪽으로만 탐색 할 수 있다. 이때 한쪽만 참조하는 경우 단방향, 양쪽이 서로 참조하면 양방향이 된다.
(앞서 만든 Member, Team 관계 참고!) 
```
```
* 연관 관계의 주인 

주인은 앞서 설명했지만 외래키를 관리하는 엔티티가 연관 관계의 주인이 된다. 그리고 주인의 반대편은 외래키에 영향을
주지 않고 단순한 조회만 가능하다.

데이터베이스 설계상 외래키는 항상 다 쪽으로 간다는 것을 기억하자 !! 
```
## @Many(주인)ToOne 
```
연관 관계 주인 엔티티와 외래키 테이블이 일치하는 관계이다. 보편적으로 가장 많이 사용한다. 앞서 설명한 
Member 와 Team 의 관계로써 단방향 양방향 모두 무난한 관계임
```
## @One(주인)ToMany
```
연관 관계의 주인과 외래키 테이블이 일치하지 않는 관계로 이 모델은 권장하지 않는다. 다대 일로 풀어나가야 한다. 

@Entity
public class Team{
...
@OneToMany
@JoinColumn(name = "TEAM_ID")
private List<Member> members = new ArrayList<Member>(); 
...}

@Entity
public class Member{
...
@ManyToOne
@JoinColumn(name = "TEAM_ID" , insertable = false, updateable = flase) // 양방향 설정시 사용한다.
private Team team 
...}

일대 다 관계를 사용할 때는 연관 관계의 주인이 되는 Team 에서 데이터 베이스의 pk 값인 TEAM_ID 와 매핑해야하는데
이때 @JoinColumn 을 꼭 사용해줘야 한다 그렇지 않으면 조인 테이블 방식?? 을 사용해서 새로운 테이블을 만들어 버린다.

또한 양방향 관계를 만들때는 Member 클래스의 Team 을 테이블의 외래키와 매핑한 후 insert, update 를 false 로 지정해서
조회만 가능하게끔 해줘야 한다. (***애초에 일대 다 양방향 매핑은 존재 하지 않는다***)

*** 결론: 항상 다대 일 관계로 풀어나가야한다. ***
```
## @OneToOne
```
```
## @ManyToMany

관계형 데이터베이스는 정규화된 테이블 2 개로 다대다 관계를 표현할 수 없다. 연결 테이블을 만들어줘야 한다. 

반대로 객체 세상에서는 각 클래스에 컬렉션을 만들어서 다대다 관계를 쉽게 풀어갈 수 있다. 

### @ManyToMany 연결 테이블 사용 매핑
```
* 다대다 단방향 매핑

@ManyToMany
@JoinTable(name = "MEMBER_PRODUCT", // 데이터베이스의 연결 테이블
           joinColumns = @JoinColumn(name = "MEMBER_ID"), // 회원과 매핑할 연결 테이블 정보
           inverseJoinColumns = @JoinColumn(name = "PRODUCT_ID")) // 반대 방향인 상품과 매핑할 컬럼 정보
private List<Product> products new ArrayList<Product>();
```
```
* 다대다 양방향 매핑

@ManyToMany(mappedBy = "products") // 역방향 추가
private List<Member> members;

Product product = em.find(Product.class, "productA");
product.getMembers(); // 객체 그래프 탐색 
```
연결 테이블을 사용하면 편리하지만 실무에서 사용하면 안 된다 연결 테이블이 단순히 연결만하고 끝나면 상관 없지만 보통

연결만 하고 끝나지 않는다. 주문 시간이나, 주문 수량과 같은 다른 데이터가 필요할 수 있다.

### @ManyToMany 는 @ManyToOne 관계로 풀어나가야 한다! 
```
* 중간 테이블 역할을 하는 엔티티 예시

@Entity
public class Order{

@Id @GeneratedValue
@Column(name = "order_id") // 중간 테이블을 식별하는 기본키 
private Long id;

@ManyToOne
@JoinColumn(name = "member_id")
private Member member;

@ManyToOne
@JoinColumn(name = "product_id")
private Product product;
...
}
```
```
* Member

@OneToMany(mappedBy = "member")
private List<Order> orders = new ArrayList<Order>();

상품은 단방향으로 만듦 상품에서 주문을 조회할 일은 없다! 비즈니스 규칙에 따라 유동적으로 만들면 된다.
```
```
위에서 만든 것 처럼 중간 테이블을 엔티티로 만들고 @ManyToOne 관계 풀어나가면 비즈니스 요구사항으로 테이블의 칼럼이 
추가되더라도 손쉽게 엔티티를 변경할 수 있다.

Order order = em.find(Order.class, orderId);
order.getMember(); 
```
# 고급 매핑 
