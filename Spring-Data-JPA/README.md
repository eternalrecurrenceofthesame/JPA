# Spring-Data
```
스프링 데이터 프로젝트는 JPA, 몽고DB 등등 다양한 데이터 저장소에 대한 접근을 추상화 해서 개발자에 편의를 제공한다.  그중 
스프링 데이터 JPA 는 JPA 에 특화된 기능을 제공한다. 

스프링 데이터를 사용하면 반복적인 CRUD 를 작성하지 않고 데이터 접근 계층을 사용할 수 있다. 또한 데이터 접근 계층을 개발할 때 구현체 
없이 인터페이스만 작성해도 된다! 540 p
```
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

스프링 데이터 JPA 를 사용해서 엔티티를 생성, 변경할 때 변경한 사람과 시간 추적하기. 
```
@EnableJpaAuditing 을 부트스트랩 클래스에 설정한다.
@EntityListeners(AuditingEntityListener.class) 엔티티에 적용

* 사용 애노테이션

@CreatedDate
@LastModifiedDate
@CreatedBy
@LastModifiedBy
```
```
* 등록일 수정일 구현하기 

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseTimeEntitiy{

@CreatedDate
@Column(updateable = false)
private LocalDateTime createdDate;

@LastModifiedDate
private LocalDateTime lastModifiedDate;}
```
```
* 등록자 수정자 구현하기

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity extends BaseTimeEntity{

@CreatedBy
@Column(updatable = false)
private String createdBy;

@LastModifiedBy
private String lastModifiedBy;

보통 등록일 수정일은 필요하지만 등록자 수정자는 필요 없는 경우가 있기 때문에 따로 분리해서 만들고 필요한 것을 
상속해서 사용하면 된다. 
```
```
* 등록자 수정자를 처리하는 AuditorAware 를 스프링 빈으로 등록

@Bean
public AuditorAware<String> auditorProvider(){
return () -> Optional.of(UUID.randomUUID().toString());}

실무에서는 세션 정보나, 시큐리티 로그인 정보에서 ID 를 받는다. 
```
### Web 확장 도메인 컨버터 클래스
```
컨트롤러에서 파라미터로 넘어온 엔티티의 아이디 값을 사용해서 자동으로 리포지토리에 접근하고 필요한 값을 찾을 수 있다.

@GetMapping("/members/{id}")
public String findMember(@PathVariable("id") Member member){
return member.getUsername();}

이녀석은 일단 직관적이지 않아서 사용에 대한 의문이 있지만 사용한다면 단순 조회용으로만 써야한다.
(트랜잭션의 범위를 넘어섰기 때문에 엔티티를 변경해도 DB 에 반영되지 않는다)
```
### Web 확장 - 페이징과 정렬
```
* 요청 파라미터로 Pageable 받기

-> /members?page=0&size=3&sort=id,desc&sort=username,desc

@GetMapping("/members")
public Page<Member> list(Pageable pageable){
Page<Member> page = memberRepository.findAll(pageable);
return page; }

-> /members?member_page=0&order_page=1

public String list(
  @Qualifier("member") Pageable memberPageable,
  @Qualifier("order") Pageable orderPageable,...)

페이징 정보가 둘 이상이면 접두사로 구분한다.
```
```
* 스프링 부트 글로벌 설정
spring.data.web.pageable.default-page-size=20 기본 페이지 사이즈
spring.data.web.pageable.max-page-size=2000  최대 페이지 사이즈
```
```
* 개별 설정 (이게 요청시 좀 더 깔끔할듯?)

@RequestMapping(value = "/members_page", method = RequestMethod.GET)
public String list(@PageableDefault(size = 12, sort = "username", direction = Sort.Direction.DESC) Pageable pageable)
{...}
```
```
* Page 내용을 DTO 로 변환 하기

@GetMapping("/members")
public Page<MemberDto> list(Pageable pageable){
  return memberRepository.findAll(pageable).map(MemberDto::new);
```
## persist 와 merge 에 대해서
```
스프링 데이터 JPA 는 save() 메서드를 호출해서 영속화 시킬 때 새로운 엔티티의 경우 persist 를 수행하지만 새로운 엔티티가 아니라면
merge 를 호출해서 병합을 시도한다. 
(새로운 엔티티란? 저장할 때 식별자가 없는 엔티티를 의미한다.)

* save() 호출시

if(entityInformation.isNew(entity)) {
em.persist(entity);
return entity;       
}else {
return em.merge(entity);}
```
```
식별자를 할당할 때 Generatedvalue 를 쓰지 않고 직접 값을 세팅해준다면 persist 가 아닌 merge(병합) 가 발생한다.
병합이 발생하는 경우 JPA 는 DB 에 값이 있을 것이라 가정하고 select 쿼리를 이용해서 값을 찾는다.

값이 없다고 판단하면 직접 식별자를 세팅한 엔티티의 값을 저장한다.
```
```
* Persistable 인터페이스를 구현해서 변경 감지 피하기

@Entity 
@EntityListeners(AuditingEntityListener.class) // Auditing 사용시 세팅 
@NoArgsConstructor
public class Item implements Persistable<String> {

@Id
private String id;

@CreatedDate
private LocalDateTime createdDate;

@Override
public String getId() {
return id;}

@Override
public boolean isNew() {
return createdDate == null;}
}

Auditing 을 사용하면 등록일을 만들기(persist) 전에는 값이 없기 때문에 이것을 이용해서 null 체크를 하고 새로운 엔티티로 
인식시키면 식별자를 직접 할당해도 새로운 엔티티로 등록할 수 있다. 
```
## 변경 감지와 병합에 대해서 (중요!)
```
* 준영속 엔티티란?

영속성 컨텍스트에 있던 값을 컨텍스트가 더이상 관리하지 않는 엔티티를 말한다. 

영속성 상태로 관리되면 값만 바꿔도 JPA 가 커밋 시점에 변경된 내용을 알아서 변경해준다.(더티 체킹)
(데이터의 수정, 저장은 트랜잭션 내에서 발생해야 하고 트랜 잭션 내에서 값이 수정되면 더티체킹이 발생한다.)

하지만 조회한 엔티티가 HTML 에서 부분 수정되고 다시 애플리케이션으로 돌아올 경우 엔티티에 식별자 값이 있고 
데이터베이스에도 이미 똑같은 식별자 값이 있다. 이 상태를 준영속 상태라고 한다. 

정리하자면 준영속 상태는 컨텍스트를 벗어나서 식별자가 있는 엔티티를 의미한다. 이 경우 앞서 설명한 것처럼 식별자가 
있기 때문에 값을 수정한 경우 변경감지가 아닌 merge(병합) 가 발생한다. 
```
```
* 준영속 엔티티를 병합하면 생기는 문제점

@PostMapping(value = "/items/{itemId}/edit")
public String updateItem(@ModelAttribute("form") BookForm form) {
 
 Book book = new Book();
 book.setId(form.getId());
 book.setName(form.getName());
 book.setPrice(form.getPrice());
 
 return "redirect:/items";}

값을 변경하기 위해 폼에서 데이터를 가져와서 엔티티를 만드는 작업을 하는 경우 앞서 설명한 것처럼 준영속 상태가 된다.
이경우 영속성 컨텍스트가 관리하지 않기 때문에 변경감지가 발생한다.

변경감지가 발생하면 생기는 문제점은 모든 컬럼 값을 다 수정해버린다는 점이다. 즉 사용자가 실수로
수정 값을 채워넣지 않는경우에 데이터베이스에 **의도하지 않은 null 값이** 들어가버리게 된다!
```
### 준영속 상태의 값을 올바르게 수정하는 방법
```
준영속 상태의 엔티티를 수정하려면 서비스 계층에서 식별자 값으로 엔티티를 조회한 후 영속성 상태의 값을
수정해서 변경 감지가 발생하도록 해야한다.

* Service

@Transactional
public void updateItem(Long id, String name, int price) {

Item item = itemRepository.findOne(id);
findItem.change(price, name, stockQuantity);
 }

값을 수정할 때는 set 으로 수정하지 않고 도메인의 의미있는 비즈니스 로직을 만들어서 수정한다.(DDD)
```
### 결론

컨트롤러에서 엔티티를 생성하지 말고 식별자 값을 서비스 계층으로 넘긴 후 영속 상태의 엔티티를 수정해서 

변경 감지가 발생하도록 하자! 








