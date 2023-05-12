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

