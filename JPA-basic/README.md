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

일대 다 단방향 매핑을 하면 다른 테이블에 외래키가 있기 때문에 연관 관계 처리를 위한 UPDATE SQL 이 추가로 실행된다 211 p

일대 다 양방향 관계를 만든다면 Member 클래스의 Team 을 테이블의 외래키와 매핑한 후 insert, update 를 false 로 지정해서
조회만 가능하게끔 해줘야 한다. (***애초에 일대 다 양방향 매핑은 존재 하지 않는다***)

*** 결론: 항상 다대 일 관계로 풀어나가야한다. ***
```
## @OneToOne

### 주 테이블에 외래키가 있는 경우
```
* 단방향

Member(테이블의 외래키와 매핑되는 엔티티)

@OneToOne
@JoinColumn(name = "locker_id") // 다대일처럼 간단하게 매핑해주면 된다.
private Locker locker;  // 데이터베이스 locker_id 에 유니크 제약 조건을 추가한다.   
```
```
* 양방향 

Product

@OneToOne(mappedBy = "locker"
private Member member; // 마찬가지로 다대일처럼 간단하게 매핑한다. 
```
### 대상 테이블에 외래키가 있는 경우
```
* 양방향

Member 

@OneToOne(mappedBy = "member")
private Locker locker;

Locker 

@OneToOne @JoinColumn(anme = "member_id")
private Member member; 

다대일과 똑같다. 일대일 관계는 대상 테이블에 외래키가 있는 **단방향 관계**를 허용하지 않는다. 
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

## 상속 관계 매핑

대분류로 아이템을 만들고 이를 상속하는 구체적인 아이템을 만들어서 공통으로 사용되는 정보를 한곳에서 관리할 수 있다. 

### 조인 전략 매핑
```
* Item

@Entity
@Inheritance(strategy = InheritenceType.JOINED) // 조인 전략 사용 
@DiscriminatorColumn(name = "dtype") // 구분 칼럼 지정, 칼럼 값으로 들어가게됨.
public abstract class Item{

@Id @GeneratedValue
@Column(name = "item_id")
private Long id;

private String name; // 이름
private int price;  // 가격 
}

아이템 추상 클래스를 상속받으면 이름과 가격 아이디값을 공통으로 사용할 수 있다. 
```
```
* Book

@Entity
@DiscriminatorValue("b")
@PrimaryKeyJoinColumn(name = "book_id") // ID 값을 재정의 할 수 있다.
public class Book extends Item

private String author;
private String isbn;

책에서 필요한 구체적인 필드값들을 정의한다! 
```
```
조인 매핑 전략을 사용하면 클래스의 응집성을 높이면서 유지보수가 용이한 상속 클래스들을 만들 수 있다.
하지만 조회할 때 조인이 많이 사용되므로 성능이 저하되고 조회 쿼리가 복잡하며 

데이터를 등록할 때 INSERT SQL 을 두 번 실행한다는 단점이 있다. 
```
### 단일 테이블 전략

단일 테이블 전략은 조인과 다르게 하나의 테이블에 책,앨범,영화 정보를 모두 구현하는 전략이다.

```
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
public absatract class Item{

@Id @GeneratedValue
@Column(name = "item_id")
private Long id;

private String name;
private int price;  // 공통 정보
}

@Entity
@DiscriminatorValue("A")
public class Album extends Item {...}
```
```
단일 테이블 전략을 사용하면 조인이 필요없고 조회 쿼리가 단순해지지만 사용하지 않는 칼럼의 경우 null 값을 허용해야한다.
테이블이 비대해지면 상황에 따라서 조회 성능이 느려질 수 있고 구분 컬럼을 꼭 사용해야 한다. 

조인 전략의 경우 구현체에 따라서 구분 컬럼 없이도 동작한다. @DiscriminatorColumn

개인적으로는 조인 전략이 훨씬 깔끔한 것 같다. 
```

## @MappedSuperclass

공통의 매핑 정보가 필요할 때 사용한다.

```
앞서 설명한 상속 관계가 아니며 엔티티가 아니기 때문에 테이블과 매핑되지 않는다. 부모 클래스를 상속 받는 
자식 클래스에 매핑되는 정보만 제공한다 직접 생성해서 사용할 일이 없으므로 추상 클래스로 만들자!

엔티티가 사용하는 공총 매핑 정보를 한곳에 모으고 클래스의 응집성과 유지보수성을 높일 수 있다.
주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보들!

참고로 @Entity 클래스는 엔티티나 @MappedSuperclass 로 지정한 클래스만 상속할 수 있다.
```
```
@MappedSuperclass
public abstract class BaseEntity{

@Id @GeneratedValue
private Long id;
private Stirng name;
...}

@Entity
public class Member extends BaseEntity{

//ID, NAME 값을 상속 받는다. 
private String email;
}
```
# 프록시와 연관 관계 관리 

멤버와 팀의 다대 일 관계에서 멤버를 조회할 때 회원 정보를 항상 조회해야 할까? 반대의 경우 팀을 조회할 때 멤버를 같이 

조회해야 하는지에 대한 논의! 

## 프록시 조회하기
```
em.find() 대신 em.getReference() 를 사용하면 데이터베이스에서 프록시 엔티티를 조회할 수 있다. 

Member member = em.getReferecne(Member.class, "id1");
member.getName();

프록시는 실제 클래스를 상속받아서 만들어지고 실제 객체의 참조를 보관한다. 프록시 객체를 초기화해도 
프록시 객체가 실제 엔티티로 바뀌는 것이 아니다. 프록시 객체를 통해서 실제 엔티티에 접근할 수 있다.

프록시 객체는 타입 체크시 == 대신 instance of 를 사용한다.
영속성 컨텍스트에 찾는 엔티티가 있으면 프록시를 호출해도 실제 엔티티를 반환한다. 
```

## 즉시 로딩과 지연 로딩

프록시 객체는 주로 연관된 엔티티를 지연 로딩할 때 사용한다! 

### 지연 로딩 을 사용해서 연관 객체를 프록시로 조회하기
```
@ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 
@JoinColumn(name = "team_id")
private Team team; 


Team team = memger.getTeam();
team.getName(); 
실제 team 을 사용하는 시점에 데이터베이스에 쿼리한다.
```
```
멤버와 팀을 자주 함께 사용하는 경우 즉시로딩으로 값을 한번에 가지고 올 수도 있다 하지만 가급적
지연로딩을 사용해야 한다(특히 실무의 경우)

즉시로딩을 사용하면 연관 객체를 조회하면서 N + 1 의 문제가 발생할 수 있다. 연관된 객체를 모두 조회하기 때문에
한번의 쿼리로 N 번의 쿼리가 발생하고 성능 문제로 이어진다. 

@ManyToOne, @fOne 은 기본이 즉시로딩이므로 항상 LAZY 로딩으로 설정하자! // toMany 관계는 기본이 지연 로딩.
```

## 영속성 전이:CASCADE

특정 엔티티를 영속 엔티티로 만들 때 연관 엔티티도 함께 영속 상태로 만들면 싶다면 CASCADE 설정을 사용하면 된다.

```
* CASCADE - (ALL, PERSIST, MERGE, REMOVE, REFRESH, DETACH)

@OneToMany(mappedBy = "parent", cascade=CascadeType.Persist)
private List<Child> children = new ArrayList<Child>();

child1.setParent(parent);  parent.getChildren.add(child1);
child2.setParent(parent);  parent.getChildren.add(child2);

em.persist(parent);
연관 관계를 설정하고 부모 객체만 영속성 상태로 만들면 관련된 자식 객체도 영속성 상태가 된다.
(간단하게 생각하면 ToOne 만 em.persist 해주면 됨)


Parent findParent = em.find(Parent.class, 1L);
em.remove(findParent); 
(엔티티를 조회하고 ToOne 관계를 삭제하면 연관된 컬렉션도 모두 삭제된다!)

참고로 PERSIST, REMOVE 는 em.persist(), em.remove() 를 실행할 때 바로 전이가 발생하지 않고 플러시를 호출할 때 
전이가 발생한다. 
```

## 고아 객체

고아 객체란 부모(One) 엔티티와 연관이 끊어진 자식(Many) 엔티티를 자동으로 삭제하는 기능을 말한다.

```
@OneToMany(mappedBy = "parent", orphanRemoval = true) // 고아 객체 설정
private List<Child> children = new ArrayList<>();

Parent parent1 = em.find(Parent.class, id);
parent1.getChildren().remove(0);

자식엔티티를 컬렉션에서 제거하면 자동으로 삭제된다.
```
```
* 주의점

참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 것으로 가정한다. 즉 참조하는 곳이 하나일 때 사용할 수 있다.
@OneToOne, @OneToMany 관계의 One 에서만 가능하다.

참고로 고아 객체에서 부모를 제거하면 자식은 고아가 된다. 따라서 OrphanRemoval 을 활성화 하면, 부모를 제거할 때 
자식도 함께 제거된다. 마치 CascadeType.REMOVE 처럼 동작한다.
```
```
* CascadeType.ALL + orphanRemovel = true 조합으로 사용하기

두 가지 옵션을 모두 활성화 하면 부모 엔티티로 자식의 생명 주기를 관리할 수 있다. 

CascadeType.All 에서 em.remove(parent); 을 하면 엔티티도 모두 삭제되는 기능 + parent1.getChildren().remove(0); 컬렉션 객체 
그래프로 자식 엔티티를 삭제하는 기능도 추가된 형태.

도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용하다.
```

# 값 타입

## 기본 값 타입
```
int,double,boolean,byte 같은 기본 타입과 Stirng, Integer 같은 래퍼 클래스, String 이 있다. 기본 값타입이나
래퍼 타입은 서로 공유되지 않는다. 

int a = 1;
int b = 2;

int a = b;  // a = 2, b = 2 
int a = 3;  // a = 3, b = 2 값 타입은 값을 대입하면 값 자체가 복사됨. 인스턴스는 대입하면 인스턴스의 주소값이 서로 공유된다.

값 타입은 식별자가 없고 값만 있어서 변경시 추적할 수 없고 생명 주기를 엔티티에 의존한다. 
```
## 임베디드 타입(복합 값 타입)
```
새로운 값 타입을 직접 정의하는 것을 JPA 는 임베디드 타입이라 한다. 주로 기본 값 타입을 모아서 복합 값 타입이라고 말함.
임베디드 타입을 사용하면 도메인 주도 설계에서 애그리거트 간 경계를 명확하게 할 수 있다.
```
```
* 값 타입 예시

Member Entity

Long id 
String name 
Period workPeriod    // Period (startDate, endDate) 
Address homeAddress  // Address (city, street, zipcode)

값 타입을 사용하면 애플리케이션의 유지 보수성과 응집도를 높일 수 있고 
Period.isWork() 처럼 해당 값 타입만 사용하는 의미 있는 메서드를 만들 수 있다. 

임베디드 타입을 포함한 모든 값 타입은 값 타입을 소유한 엔티티에 생명 주기를 의존한다. 
```
### 임베디드 타입 사용하기
```
@Embeddable: 값 타입을 정의하는 곳에 표시
@Embedded: 값 타입을 사용하는 곳에 표시
```
```
@Entity
public class Member {

@Embedded Address address;
@Embedded PhonNumber phonNumber;

@Embeddable
public class Address{
String street;
String city;
String state;
@Embedded Zipcode zipcode;} // 값 타입 내부에서도 값 타입을 사용할 수 있다.

@Embeddable
public class Zipcode{
String zip;
String plusFour;}

@Embedable
public class PhonNumber{
String areaCode;
String localNumber;
@ManyToOne PhoneServiceProvider provider;} // 엔티티참조를 값 타입에서 사용가능

참고로 값 타입에서는 기본 생성자를 필수로 만들어야 한다. 

값 타입을 null 로 만들면 입력 필드 값이 모두 null 값으로 된다.
member.setASddress(null); // 모두 null 로 반영
```
### @AttributeOverride 를 사용해서 값 타임 칼럼 값 매핑 재정의 하기
```
@Embedded
@AttributeOverrides({
  @AttributeOverride(name="city", column=@Column(name = "company_city")),
  @AttributeOverride(name="street", column=@Column(name = "company_street")),
  @AttributeOverride(name="zipcode", column=@Column(name = "company_zipcode:"))})
Address companyAddress;

위에서 예시한 주소를 회사 주소로 사용하고 싶다면 컬럼 값과 필드를 직접 오버라이드해서 매핑할 수도 있다!
```
### 값 타입과 불변 객체
```
값 타입의 필드값 들은 기본 타입이기 때문에 서로 공유되지 않지만, 값 타입 자체는 인스턴스로 만들어지면 공유될 수 있다! 
(엔티티에서 컬럼값들이 공유되는 무시무시한 상황)

이런 경우를 방지하기 위해 값 타입은 불변 객체로 만들어야한다. 값 타입은 단독으로는 사용되지 않음! 
(불변 객체란? 생성 시점 이후 절대 변경할 수 없는 객체를 의미한다)
```
```
* 값 타입 불변 객체 만들기

불변 객체를 만드는 가장 간단한 방법은 생성자로만 값을 설정하게 하고 수정자를 만들지 않으면 된다.

@Embeddable
public class Address{

private String city;   // private 으로 만들어서 클래스 내부에서만 사용할 수 있게 한다. 
protected Address() {} // protected 동일 패키지의 모든 클래스와, 다른 패키지의 자식 클래스에서 사용 

public Address(String city) {this.city = city} // 값을 생성할 때 생성자로 매개변수를 주입한다.
Getter 만 만들고 Setter 는 노출하지 않는다.}
```
```
* 불변 객체 사용

Address address = member1.getHomeAddress();
Address new Address = new Address(address.getCity());

member2.createHomeAddress(newAddress); // 인스턴스의 주소값을 공유하지 않고 새로운 인스턴스를 사용한다.
```
### 값 타입을 서로 비교하기
```
값 타입 내부의 값을 서로 비교하려면 동일성 비교( == ) 가 아닌 인스턴스의 값을 서로 비교하는 동등성 비교 a.equalas(b)
를 사용한다. 값 타입 클래스를 구현할 때 equals, hashcode 를 적절하게 재정의하자! 
```
#### + 값 타입을 식별자 타입으로 사용할 때 
```
엔티티의 식별자 값을 명확하게 하기위해 값 타입을 식별자로 사용하는 경우가 있다. ex) private ItemId id;
이 경우 ItemId 는 값타입이 되면서 동시에 식별자 타입으로도 쓰일 수 있어야 한다.

값 타입을 서로 비교할 수 있도록 값타입 객체에 equals, hashcode 를 구현하고 Serializable 인터페이스를 implements 해야
값 타입을 식별자 타입으로 사용할 수 있다.

DDD 예제 참고 
```
## 값 타입 컬렉션

데이터 베이스에는 컬렉션의 개념이 없다. 데이터 베이스에 값 타입으로 매핑되는 컬럼들을 컬렉션처럼 저장하고 싶다면

값 타입 컬렉션을 사용하면 된다. 

### 값 타입 컬렉션 구현하기
```
* Member 

@Id @GeneratedValue
private Long id;

@ElementCollection
@CollectionTable(name = "favorite_foods", //값 타입 컬렉션 테이블과 매핑
joinColumns=@JoinColumn(name="member_id"))//멤버 엔티티의 pk 값과 매핑 되는 컬럼으로 컬렉션 테이블에서(pk,fk) 가 된다. 
@Column(name = "food_name")
private Set<String> favoriteFoods = new HashSet<String>();


@ElementCollection
@CollectionTable(name = "address", 
  joinColumns = @JoinColumn(name = "member_id"))
private List<Address> addressHistory = new ArrayList<>();

@CollectionTable 을 생략하면 기본값(엔티티이름_컬렉션 속성) 을 사용해서 테이블, 컬럼과 매핑한다. 335 p
```
### 값 타입 컬렉션 테이블 구현하기
```
값 타입 컬렉션을 사용하면 컬렉션을 저장하기 위한 별도의 테이블을 추가해야 한다. 값 타입 컬렉션을 저장하기 위한 
컬렉션 테이블의 컬럼 값들은 모두 PK 값으로 구성하고 참조하는 엔티티의 PK 값을 (PK,FK) 로 가진다. 335 p, 339 p
```
### 값 타입 컬렉션 INSERT SQL 사용 예시
```
Member member = new Member();

member.setHomeAddress(new Address("주소")); // 임베디드 값 타입

member.getFavoriteFoods().add("짬뽕");
member.getFavoriteFoods().add("짜장면");
member.getFavoriteFoods().add("탕수육"); // 기본 값 타입 컬렉션 생성

member.getAddressHistory().add(new Address("부산"));
member.getAddressHistory().add(new Address("서울")); // 임베디드 값 타입의 컬렉션

em.persist(member);

이 예시에서는 member 만 영속화 시켰다. 값 타입 컬렉션을 사용하면 영속성 전이 + 고아 객체 제거 기능을 필수로 가진다. 
(부모 객체(One)가 자식 객체(Many)를 완전하게 관리할 수 있음) 

* 로직 실행시 실행되는 INSERT SQL

member:INSERT SQL 1 번
member.setHomeAddress: 컬렉션이 아닌 임베디드 값 타입은 엔티티를 저장할 때 sql 에 포함된다!

member.favoriteFoods: INSERT SQL 3 번
member.addressHistory: INSERT SQL 2 번

총 6 번의 SQL 이 실행된다. (플러시 할 때 SQL 실행)
```
### 값 타입 컬렉션 SELECT SQL 사용 예시 
```
값 타입 컬렉션도 조회할 때 페치 전략을 사용할 수 있으며 기본 값은 LAZY 이다.
@ElementCollection(fetch = FetchType.LAZY)

* 지연로딩을 가정하고 SELECT 실행하는 예시

Membmer member = em.find(Member.class, 1L); 
Address homeAddress = member.getHomeAddress(); // 회원 엔티티를 조회하면서 임베디드 값타입도 함께 조회한다.

member.getFavoriteFoods(); // LAZY
for(String favoritedFood : favoriteFoods){
System.out.println("favoriteFood = " + favoritedFood); } // LAZY 프록시 초기화

List<Address> addressHistory = member.getAddressHistory(); // LAZY
addressHistory.get(0); // LAZY 프록시 초기화 

총 3 번의 SQL 이 실행된다.
```
### 값 타입 컬렉션 수정 예시
```
Memberm member = em.find(Member.class, 1L);

member.setHomeAddress(new Address("새 주소")); // 값 타입은 불변 객체이므로 항상 새로 생성해야 한다.

Set<String> favoriteFoods = member.getFavoriteFoods();
favoriteFoods.remove("탕수육");
favoriteFoods.add("치킨"); // 기본 값타입을 수정할 때는 기존의 값을 제거하고 추가해야한다. 
                              자바의 String 은 수정 할 수 없다.
                             
List<Address> addressHistory = member.getAddressHistory();
addressHistory.remove(new Address("기존 주소")); 
addressHistory.add(new Address("새 주소")); // 값 타입은 불변해야 한다.

**참고로 값 타입은 equals,hashcode 를 꼭 구현해야 한다!!**
```
## 값 타입 컬렉션의 제약사항
```
값 타입 컬렉션은 값을 변경할 때 추적할 수 없다. 엔티티는 값을 변경하려면 엔티티 식별자로 값을 조회하면 된다.
하지만 값 타입 컬렉션은 테이블을 생성할 때 컬럼값들이 모두 PK 값이 되고 식별자라는 개념이 없다. 

그렇기 때문에 값 타입 컬렉션은 데이터베이스에서 조회한 후 수정하고 싶은 값만 수정할 수 없다. 
이런 메커니즘 때문에 JPA 는 값 타입 컬렉션의 수정이 발생하면 값 타입 테이블의 데이터를 모두 지우고 

현재 값 타입 컬렉션 **객체**에 있는 모든 값을 데이터베이스에 다시 저장하는 어마어마한 일을 수행한다 ..
(한마디로 값 타입 컬렉션 찾고 수정할 값만 수정하고 수정한 값 + 기존 컬렉션 값을 다시 저장한 객체를 만들어야 한다 339 P) 
그리고 컬렉션의 값을 다시 한땀한땀 반영하기 때문에 그만큼의 SQL 이 발생함.
```
```
* 값 타입 컬렉션에 매핑된 데이터가 많다면 일대 다 관계를 사용하라!

@Entity
public class AddressEntity{ // 값 타입 컬렉션을 엔티티로 만듦

@Id @GeneratedValue
private Long id;

@Embedded Address address; // 단순 임베디드 타입 }


@Entity
public class Member{
...
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "member_id")
private List<AddressEntity> addressHistory = new ArrayList<>();
...}

값 타입 컬렉션을 엔티티로 만들면 주 테이블과 대상 엔티티가 일치하지 않는 문제가 발생하기 때문에 부득이하게 
OneToMnay 를 사용하고 값 타입 컬렉션이 가진 속성을 직접 만들어주면 된다.
```
