# 📖 DI 컨테이너 구현하기

## 학습 목표

 * 스프링 프레임워크의 핵심 기술인 DI 컨테이너를 구현한다.
 * DI 컨테이너를 직접 구현하면서 스프링 DI에 대한 이해도를 높인다.

## 객체지향적인 코드?

`static` 키워드를 활용한 메서드를 의존하는 것은 강한 의존성을 만들기 때문에 변화에 유연하지 못하다.
`변경에 유연하지 못한 코드`는 객체지향적이지 못하다.

## Stage0Test
```java
class Stage0Test {
    @Test
    void stage0() {
        final var user = new User(1L, "gugu");
        
        final var actual = UserService.join(user);

        assertThat(actual.getAccount()).isEqualTo("gugu");
    }
}
```

```java
class UserService {

    public static User join(User user) {
        UserDao.insert(user); // 강한 의존성
        return UserDao.findById(user.getId());
    }
}
```

```java
class UserDao {

    private static final Map<Long, User> users = new HashMap<>();

    public static void insert(User user) {
        users.put(user.getId(), user);
    }

    public static User findById(long id) {
        return users.get(id);
    }
}
```

위 코드는 아래와 같은 문제점을 가지고 있다.

 * `UserService`는 `UserDao`와 밀접하게 결합되어 있다. 즉 직접 제어할 수 없는 영역을 포함하고 있기 때문에 테스트하기 어려운 구조를 만든다.
 * 만약 테스트를 위한 DB를 사용해야 할 경우 `UserService`가 의존하고 있는 `UserDao`에 직접 접근하여 로직을 수정해야 한다.

## Stage1Test

우리는 의존하는 객체를 외부에서 주입하여 객체의 필요에 따라 교체하도록 개선하고 싶다. 
제어할 수 없는 영역에 존재하는 외부 객체를 우리가 제어할 수 있는 영역에서 생성한 뒤 주입하는 것이다.

```java
class Stage1Test {

    @Test
    void stage1() {
        final var user = new User(1L, "gugu");

        final var userDao = new UserDao();

        final var userService = new UserService(userDao);

        final var actual = userService.join(user);

        assertThat(actual.getAccount()).isEqualTo("gugu");
    }
}
```

```java
class UserService {

    private final UserDao userDao;

    // 생성자를 통한 주입
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User join(User user) {
        userDao.insert(user);
        return userDao.findById(user.getId());
    }
}
```

```java
// 변경에 유연하지 못한 구현 클래스
class UserDao {

    private static final Map<Long, User> users = new HashMap<>();

    // DB 직접 의존
    private final JdbcDataSource dataSource;

    public UserDao() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        this.dataSource = jdbcDataSource;
    }

    public void insert(User user) {
        try (final var connection = dataSource.getConnection()) {
            users.put(user.getId(), user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User findById(long id) {
        try (final var connection = dataSource.getConnection()) {
            return users.get(id);
        } catch (SQLException e) {
            return null;
        }
    }
}
```

클래스 내부에서 직접 객체를 생성하지 않고 외부에서 생성하여 전달한다.
하지만 아직 모든 문제가 해결된 것은 아니다. 만약 다른 DB로 변경해야 하는 요구사항이 추가되면 어떻게 될까?
DB에 직접 접근하고 있는 UserDao의 구현을 수정해야 한다. 

## Stage2Test

위 문제는 인터페이스를 통해 약한 결합도를 만들 수 있다.

```java
class Stage2Test {

    @Test
    void stage2() {
        final var user = new User(1L, "gugu");

        final UserDao userDao = new InMemoryUserDao();
        final var userService = new UserService(userDao);

        final var actual = userService.join(user);

        assertThat(actual.getAccount()).isEqualTo("gugu");
    }

    @Test
    void testAnonymousClass() {
        // given
        final var userDao = new UserDao() {
            private User user;

            @Override
            public void insert(User user) {
                this.user = user;
            }

            @Override
            public User findById(long id) {
                return user;
            }
        };
        final var userService = new UserService(userDao);
        final var user = new User(1L, "gugu");

        // when
        final var actual = userService.join(user);

        // then
        assertThat(actual.getAccount()).isEqualTo("gugu");
    }
}
```

```java
class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User join(User user) {
        userDao.insert(user);
        return userDao.findById(user.getId());
    }
}
```

```java
interface UserDao {

    void insert(User user);

    User findById(long id);
}
```

```java
class InMemoryUserDao implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(InMemoryUserDao.class);

    private static final Map<Long, User> users = new HashMap<>();

    private final JdbcDataSource dataSource;

    public InMemoryUserDao() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        this.dataSource = jdbcDataSource;
    }

    public void insert(User user) {
        try (final var connection = dataSource.getConnection()) {
            users.put(user.getId(), user);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public User findById(long id) {
        try (final var connection = dataSource.getConnection()) {
            return users.get(id);
        } catch (SQLException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
```

인터페이스를 활용하면 결합도를 낮출 수 있다. 하지만 아직도 문제가 남아있다.
결국 외부에서 생성한 뒤 주입해야 한다는 것이다. 즉 상위 객체에서 의존성을 가지는 것은 여전하다.
객체를 생성하고 연결해주기 위한 목적을 가진 객체가 필요하다.


