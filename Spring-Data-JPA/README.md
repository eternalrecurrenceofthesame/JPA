# Spring-Data

스프링 데이터 프로젝트는 JPA, 몽고DB 등등 다양한 데이터 저장소에 대한 접근을 추상화 해서 개발자에 편의를 제공한다.  그 중 스프링 데이터 JPA 는 JPA 에

특화된 기능을 제공한다. 스프링 데이터를 사용하면 반복적인 CRUD 를 작성하지 않고 데이터 접근 계층을 사용할 수 있다. 또한 데이터 접근 계층을 개발할 때 

구현체 없이 인터페이스만 작성해도 된다! 540 p
```
* 스프링 데이터 JPA 예시

public interface MemberRepository extends JpaRepository<Member, Long> // 엔티티 타입, PK 매핑 타입
```
## 쿼리 메소드 기능
```
쿼리 메소드는 스프링 데이터 JPA 가 제공하는 기능으로 메소드 이름만으로 쿼리를 생성하는 기능이다. 메소드 이름을 규칙에 따라 만들면 메소드 이름으로 
적절한 JPQL 쿼리를 생성해서 실행한다. 
```
```
* 쿼리 메소드 기능 예시

public interface MemberRepository extends Repository<Member, Long>{
List<Member> findByEmailAndName(String email, String name);}

그외 https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation 참고
```
## JPA NamedQuery

JPA 네임드 쿼리란 쿼리에 이름을 부여해서 사용하는 방법이다. 
```
* 네임드 쿼리 생성

@Entity
@NamedQuery(
  name = "Member.findByUsername",
  query = "select m from Member m where m.username = :username")
public class Member {...}  
```
```
* JPA 를 직접 사용해서 Named 쿼리 호출하기

public class MemberRepository{
public List<Member> findByUsername(String username){
...
List<Member> resultList = em.createNamedQuery("Member.findByUsername", Member.class) // 네임드 쿼리 호출
                            .setParameter("username", "회원1")
                            .getResultList();}}
```
```
* 스프링 데이터 JPA 로 Named 쿼리 호출하기

public interface MemberRepository extends JpaReposiory<Member, Long>{

List<Member> findByUsername(@Param("username") String username); }
스프링 데이터 JPA 는 선언한 도메인 클래스.메소드 이름으로 Named 쿼리를 찾아서 실행한다.
```

