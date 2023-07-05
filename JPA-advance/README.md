# JPA 고급 주제

JPA 와 관련된 고급 주제를 정리하는 저장소. 교재의 페이지 순으로 정리한다.

## 복합 키를 사용한 매핑 254 p

#### + 식별 관계와 비식별 관계란 ? 
```
* 식별 관계

식별 관계는 부모 테이블의 기본 키를 내려 받아서 자식 테이블에서 기본키 + 외래키로 사용하는 관계를 말한다.

CHILD
PARENT_ID(PK,FK) // 기본키 + 외래키로 사용
CHILD_ID(PK)
```
```
* 비식별 관계

비식별 관계는 부모 테이블의 기본키를 외래키로만 사용하는 관계이다. 255 p

CHILD
CHILD_ID(PK)
PARENT_ID(FK)

비식별 관계는 선택적 비식별 관계(외래키에 null 을 허용) 와 필수적 비식별 관계(외래키에 null 허용 x) 로 구분할 수 있다.
보통 비식별 관계를 주로 사용하고 필요한 곳에 식별 관계를 사용한다.
```
### @IdClass

@IdClass 를 사용해서 부모의 복합키를 자식의 비식별 관계로 매핑해보기. (부모의 복합키 PK+PK 를 자식 엔티티와 매핑한다.)
```
@Entity
@IdClass(ParentId.class)
public class Parent{

@Id
@Column(name = "parent_id1")
private String id1;

@Id
@Column(name = parent_id2")  // 복합 키 PK + PK
private String id2;   }
```
```
* IdClass 에서 사용되는 복합키 클래스 

public class ParentId implements Serializable{

private String id1; // Parent.id1 매핑
private String id2; // Parent.id2 매핑

public ParentId(){} // 기본 생성자 

public ParentId(String id1, String id2){
this.id1 = id1;
this.id2 = id2;

equlas & hashcode 구현 
}


JPA 에서 복합키를 사용하려면 따로 복합키 클래스를 만들고 엔티티에 @IdClass 애노테이션으로 지정해주면 된다.

식별자 클래스의 속성명과 엔티티에서 사용하는 식별자의 속성명이 같아야한다.
Serializable 인터페이스를 구현해야한다.
equals&hashcode 를 구현해야한다.
기본 생성자가 있어야 한다.
식별자 클래스는 public 을 사용한다. 
```
```
* 자식 클래스 연관 관계 매핑

@Entity 
public class Child{

@Id
private String id; // 자식 PK

@ManyToOne // 부모 FK + FK
@JoinColumns({
          @JoinColumn(name = "parent_id1",
            referencedColumnName = "parent_id1"),
          @JoinColumn(name = "parent_id2",
            referencedColumnName = "parent_id2")})
private Parent parent;
}
```
```
* 복합키를 저장하고 조회하기

Parent parent = new Parent("myId1", "myId2");
em.persist(parent);

ParentId parentId = new ParentId("myId1", "myId2");
em.findParent.clas, parentId);

매우 복잡하다!! 
```

@IdClass 를 사용해서 부모의 복합키를 자식의 **식별** 관계로 매핑해보기. (부모의 복합키 PK 를 자식 엔티티 PK + FK 로 만든다.)

```
@Entity
public class Parent{

@Id@Column(name = "parent_id") // PK 값으로 자식 클래스의 PK + FK 가 된다
private String id; } 
```
```
@Entity
@IdClass(ChildId.class) // 복합키 매핑 @IdClass
public class Child{

@Id
@ManyToOne
@JoinColumn(name = "parent_id")
public Parent parent;

@Id @Column(name = "child_id")
private String childId;}
```
```
복합키 매핑 클래스

public class ChildId implements Serializable{

private String parent; // Child.parent
private String childId; // Child.childId }
```

## OSIV
```
* spring.jpa.open-in-view: true(default)

OSIV 는 ture 가 디폴트 값이며 이 설정을 유지하면 최초 데이터베이스 커넥션 시작부터 API 응답이 끝날 때까지
영속성 컨텍스트와 데이터베이스 커넥션을 유지한다. 

(영속성 컨텍스트와 데이터베이스 커넥션을 유지해야 지연로딩이 가능)

이 기본 전략의 문제는 데이터베이스 커넥션 리소스를 오래 사용하기 때문에 실시간 트래픽이 중요한 애플리케이션에서는
커넥션이 모자랄 수 있다. 
```
```
* spring.jpa.open-in-view: false

OSIV 설정을 끄게되면 영속성 컨텍스트는 트랜잭션 내에서만 생존하게 되고 지연 로딩시 필요한 데이터를 트랜잭션 내에서
미리 가져와야 하지만 커넥션 낭비 문제를 해결할 수 있다.
```

### 커맨드와 쿼리를 분리해서 사용하기

데이터를 변경하는 커맨드와 쿼리를 분리하면 OSIV 를 트랜잭션 계층까지만 유지하면서 지연로딩을 깔끔하게 해결할 수 있다.

API 나 화면 계층에서 커맨드 및 쿼리에 대한 요청 데이터 모델을 파라미터 값으로 받고 커맨드 내에서 변경 작업을 수행하고

쿼리 내에서 필요한 조회 작업을 수행하면 트랜잭션 내에서 작업을 수행하고 필요한 값을 데이터 모델로 전달할 수 있다.

DDD 예제 애플리케이션 참고.


## @Version 698 p
```
@Version 애노테이션을 사용하면 버전 관리를 통해 데이터 정합성을 유지할 수 있다. 

버전은 엔티티의 값을 변경하면 증가한다. 조회후 수정을 플러시 하는 시점에서 버전 정보가 하나씩 증가하면서 저장된 버전과 
조회 후 수정한 버전을 서로 비교하여 버전 정보가 다르면 예외가 발생한다. 700 p

// 트랜잭션 1 이 데이터를 조회한다 (제목A, version=1)
Board board = em.find(Board.class, id);

// 트랜잭션 2 에서 조회 후 데이터를 수정하는 동안 동일 게시물을 수정한다. (version=2)

//트랜잭션 1 에서 데이터를 수정하고 플러시하면 버전 정보가 다르기 때문에 예외가 발생한다.
board.setTitle("제목B") 

save(board); tx.commit(); <- 버전 정보가 다르기 때문에 예외가 발생!
```
```
* 버전 사용시 주의할 점

버전은 JPA 가 자동으로 증가시키기 때문에 개발자가 직접 수정하면 안 된다.

벌크 연산을 사용한다면 버전 정보를 무시하게되는데 이 경우 버전 필드를 강제로 증가시켜야 한다.
update Member m m set m.name = "변경", m.version = m.version + 1
```
### JPA 락 사용

#### JPA 낙관적 락의 세 가지 옵션(NONE, OPTIMISTIC, OPTIMISTIC_FORCE_INCREMENT) 

참고로 @Version 필드만 있으면 낙관적 락이 적용된다.

```
* NONE

조회한 엔티티를 수정할 때까지 다른 트랜잭션에 의해서 수정되면 안 되는 제약 조건을 가진다. 엔티티를 수정할 때
버전 체크를 한다.
```
```
* Board board = em.find(Board.class, id, LockModeType.OPTIMISTIC);

이 옵션을 추가하면 수정할 때 뿐만 아닌 조회를 하는 경우에도 버전체크를 한다. 한번 조회한 엔티티는 다른 트랜잭션을
종료할 때까지 다른 트랜잭션에서 변경하지 않음을 보장한다.

NONE 과 다른 점은 수정 뿐만 아니라 조회만 해도 버전 정보가 다르면 예외가 발생한다는 점임

NONE 은 조회후 데이터를 수정해야 버전 체크를 하지만 이 경우 데이터를 여러 개 조회하고 일부만 변경해도 조회한 데이터의 버전 정보를
체크한다는 의미. 즉 조회만 한 데이터가 다른 트랜잭션에서 수정하면 예외가 발생한다는 뜻인듯? 
```
```
* Board board = em.find(Board.class, id, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

다대일 양방향 관계에서 사용할 수 있다. 예를들면 게시물(1) 과 파일(다) 의 관계에서 게시물은 수정되지 않았는데 파일만
추가될 경우 게시물의 버전 정보는 그대로 유지되는데 OPTIMISTIC_FORCE_INCREMENT 를 사용하면 

양방향 연관 관계의 게시물의 버전도 강제로 증가시킬 수 있다. (두 번의 쿼리를 실행함)

이 옵션은 DDD 애그리거트 개념에서 유용하게 사용될 수 있다. DDD 는 엔티티를 설계할 때 애그리거트 루트 엔티티를 통해서
값을 변경할 수 있는데 애그리거트 내의 다른 엔티티 값만 수정하는 경우 루트 엔티티가 수정되지 않기 때문에 버전 정보가 

증가하지 않는다 따라서 이 옵션을 사용하면 루트 엔티티의 버전 정보까지 강제로 증가시킬 수 있다.
```
#### JPA 비관적 락 

PESSIMISTIC LOCK 은 데이터베이스 트랜잭션 락 메커니즘에 의존하는 방법으로 주로 SQL 쿼리에 select for update 

(수정 중 read write 불가) 구문을 사용하며 버전 정보는 따로 사용하지 않는다. 
```
* PESSIMISTIC_WRITE (일반적인 비관적 락)

select for update 를 사용해서 락을 건다. (락이 없으면 read, write 불가능) NON-REPEATABLE READ
(유령 읽기) 를 방지할 수 있다.
```
```
* PESSIMISTIC_READ

MySQL 의 lock in share mode 를 의미한다 다른 트랜잭션에서 수정할 수 없지만 읽을 수는 있다.
(보통 잘 사용 안한다.)
```
```
* PESSIMISTIC_FORCE_INCREMENT

비관적 락 중 유일하게 버전 정보를 사용한다. nowait 을 지원하는 데이터베이스에 대해
for update nowait 옵션을 적용하고 nowait 을 지원하지 않으면 update 를 사용한다.

(for update nowait - 락을 획득하지 못하면 바로 업데이트를 실패한다. ORACLE) 
```
```
* 비관적 락과 타입 아웃

비관적 락을 사용하면 유령읽기 방지, 수정 불가, nowait 기능을 사용할 수 있다 하지만 락을 획득할 때까지
무한정 대기하는 문제가 발생한다. 이를 방지하기 위해 타임아웃 시간을 줄 수 있다.

ex)

  @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")
    })
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") MemberId memberId);

쿼리 힌트로 타임아웃 시간을 3 초 설정한다.
```
## List + @OrderColumn 615 p
```
@Entity
public class Board {
...
@OneToMany(mappedBy = "board")
@OrderColumn(name = "POSITION")
private List<Comment> comments = new ArrayList<Comment>();
...
}

@Entity
public class Comment {
...
@ManyToOne
@JoinColumn(name = "BOARD_ID")
private Board board;
...
}
```
```
List 컬렉션과 @OrderColumn 을 같이 사용하면 컬렉션의 값을 컬렉션 순서대로 보관한다.

COMMENT TABLE
1(COMMENT_ID PK) | 댓글1(COMMENT) | 1(BOARD_ID  FK) | 0(POSITION) 
2(COMMENT_ID PK) | 댓글2(COMMENT) | 1(BOARD_ID  FK) | 1(POSITION) 

테이블 연관관계의 주인인 Comment 는 FK 와 동시에 리스트 컬렉션의 위치 값을 보관하기 때문에 
편리하게 보일 수도 있지만 실무에서 사용하기에는 단점이 많다.
```
```
- 단점

COMMENT 에서 POSITION 값을 가져올 때 그리고 BOARD 컬렉션을 삭제할 때 추가 쿼리가 발생한다.
POSITION 중간에 값이 없으면 조회한 List 에 null 값이 보관돼서 NPE 가 발생할 수 있다 즉
COMMENT 데이터을 삭제할 때마다 POSITION 값을 각각 하나씩 줄이는 작업을 해줘야한다 .. 

https://www.nowwatersblog.com/jpa/ch14/14-1 참고 
```
### 해결책 @OrderBy 를 사용하라! 
```
@Entity
public class Team{

@Id @GenreatedValue
private Long id;
private String name;

@OneToMany(mappedBy = "team)
@OrderBy("username desc, id asc") // username 기준 내림차순으로 정렬, id 로 오름차순 정렬
private Set<Member> members = new HashSet<Member>();
...
}

@Entity
public class Member{

@Id @GenreatedValue
private Long id;

@Column(name = "MEMBER_NAME")
private String username;

@ManyToOne
private Team team;
...
}
```
```
select m * from member m 
where m.team_id = ?
order by m.member_name desc, m.id asc

특정 팀에 속한 멤버를 조회할 때 내림차순과 오름차순을 적용해서 값을 가져온다.
```







