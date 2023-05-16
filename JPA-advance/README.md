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
```
데이터베이스 트랜잭션 락 메커니즘에 의존하는 방법으로 버전 정보는 따로 사용하지 않는다.
(지금까지 사용해본적이 없기 때문에 따로 설명하지는 않겠음. 필요시 참고 706 p)
```











