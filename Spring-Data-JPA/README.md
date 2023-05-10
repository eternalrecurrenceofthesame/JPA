# Spring-Data

스프링 데이터 프로젝트는 JPA, 몽고DB 등등 다양한 데이터 저장소에 대한 접근을 추상화 해서 개발자에 편의를 제공한다.  그 중 

스프링 데이터 JPA 는 JPA 에 특화된 기능을 제공한다. 

스프링 데이터를 사용하면 반복적인 CRUD 를 작성하지 않고 데이터 접근 계층을 사용할 수 있다. 또한 데이터 접근 계층을 개발할 때 

구현체 없이 인터페이스만 작성해도 된다! 540 p
```
* 스프링 데이터 JPA 예시

public interface MemberRepository extends JpaRepository<Member, Long> // 엔티티 타입, PK 매핑 타입
```
## 쿼리 메소드 기능

### 메소드 이름으로 쿼리 생성
```
쿼리 메소드는 스프링 데이터 JPA 가 제공하는 기능으로 메소드 이름만으로 쿼리를 생성하는 기능이다. 메소드 이름을 규칙에 따라서 
만들면 메소드 이름으로 적절한 JPQL 쿼리를 생성해서 실행한다. 
```
```
public interface MemberRepository extends Repository<Member, Long>{
List<Member> findByEmailAndName(String email, String name);}

그외 https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation 참고
```
### JPA NamedQuery

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
### @Query 사용하기
```
* JPQL 사용하기

스프링 데이터 JPA 에서 쿼리 메소드 대신 @Query 를 사용하면 손쉽게 JPQL 을 사용할 수 있다.

@Query("select m from Member m where m.username = :username)
Member findByUsername(@Param(username) String username); // @Param 을 이용해서 파라미터 바인딩 적용.
```
```
* 네이티브 SQL 사용하기

@Query(value = "select * from member where username = :username",
     nativeQuery = true)
Member findByUsername(String username);
```
### 벌크성 수정 쿼리
```
* 스프링 데이터를 사용한 벌크성 쿼리 예시

@Modifying(clearAutomatically = true)
@Query("update Product p set p.price = p.price * 1.1 where p.stockAmount < :stockAmount")
int bulkPriceUp(@Param("stockAmount") String stockAmount);

clearAutomatically = ture // 벌크성 쿼리 실행 후 컨텍스트를 초기화 할 때 사용한다 기본값은 false 
```
```
참고로 벌크 연산은 영속성 컨텍스트를 무시하고 실행하기 떄문에, 영속성 컨텍스트에 있는 엔티티의 상태와 DB 의 엔티티 상태가 달라질 수 있다.

영속성 컨텍스트에 엔티티가 없는 상태로 벌크 연산을 먼저 실행하든지, 컨텍스트에 엔티티가 있다면 벌크 연산 
직후 영속성 컨텍스트를초기화 한다.
```
### 반환 타입
```
스프링 데이터는 단건 조회시 값이 없으면 NPE 를 발생시키지 않고 null 을 반환한다. 551 p 
```
### 페이징과 정렬
```
스프링 데이터 JPA 는 페이징과 정렬 기능을 제공한다

Sort: 정렬 기능
Pageable: 페이징 기능(내부에 Sort 를 포함한다)
```
```
// Page 를 반환 타입으로 받으면 전체 count 쿼리를 실행한다 (무거움)
Page<Member> findByName(String name, Pageable pageable);

List<Member> findByName(String name, Pageable pageable);
List<Member> findByName(String name, Sort sort);
```
```
* 페이징 예시(페이징은 0 부터 시작한다.)

PageRequest pageRequest = new PageRequest(0, 10, new Sort(Direction.DESC, "name"));
Page<Member> result = memberRepository.findByNameStartingWith("김", pageRequest);

List<Member> members = result.getContent(); // 조회된 데이터 꺼내기 553 p 
```
```
* 기타 페이징 예시

// 카운트 쿼리를 분리할 수 있다
@Query(value = "select m from Member m", countQuery = "select count(m.username) from Member m")
Page<Member> findMemberAllCountBy(Pageable pagable); 

Top, First 사용 참고
https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result

// 페이지를 유지하면서 엔티티를 DTO 로 변환하기
Page<Member> page = memberRepository.findByAge(10, pageReuqest);
Page<MemberDto> dtoPage = page.map(m -> new MemberDto());
```
### JPA 구현체에 제공하는 힌트
```
@QueryHints(value = @{QueryHint(name = "org.hibernate.readOnly", value = "true")}
, forCounting = true)
Page<Member> findByName(String name, Pageable pageable);
```
## 확장 기능

### 사용자 정의 리포지토리 구현하기
```
스프링 데이터 JPA 를 사용하면 인터페이스만 정의하고 구현체는 필요 없다. 

인터페이스를 커스텀 리포지토리로 만들려면 인터페이스에서 필요없는 부분까지 구현해야 하는데 스프링 데이터 JPA 는
이런 문제를 해결할 수 있는 방법을 제공한다.
```
```
public interface MemberRepositoryCustom{
public List<Member> findMemberCustom(); } // 사용자 정의 인터페이스 생성

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom{
// 사용자 정의 인터페이스를 구현, 명명 규칙으로 사용자 정의 인터페이스 명에 + Impl 을 붙여준다.
}

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryImplCustom{}
// 사용자 정의 리포지토리를 상속받아서 사용한다. 
```
### Auditing

엔티티를 생성, 변경할 때 변경한 사람과 시간 추적하기. 
```
@EnableJpaAuditing 을 부트스트랩 클래스에 설정한다.
@EntityListeners(AuditingEntityListener.class) 엔티티에 적용
```
