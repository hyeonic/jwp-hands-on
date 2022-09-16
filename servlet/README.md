# 📖 서블릿 구현하기

## 학습 목표

 * 서블릿, 필터가 무엇인지 직접 경험해본다.
 * 서블릿을 사용할 때 주의할 점을 학습한다.

## 실습 요구 사항

 * ServletTest와 FilterTest 클래스의 모든 테스트를 통과시킨다.

## 1단계 - 서블릿 학습 테스트

 * `SharedCounterServlet`, `LocalCounterServlet` 클래스를 열어보고 어떤 차이점이 있는지 확인한다.
 * `ServletTest`를 통과시킨다.
 * `init`, `service`, `destroy` 메서드가 각각 언제 실행되는지 콘솔 로그에서 확인한다.
 * 왜 이런 결과가 나왔는지 다른 크루와 이야기해보자.
 * 직접 톰캣 서버를 띄워보고 싶다면 `ServletApplication` 클래스의 main 메서드를 실행한다.
   * 웹 브라우저에서 localhost:8080/shared-counter 경로에 접근 가능한지 확인한다.

### SharedCounterServlet

Servlet 인터페이스의 service 메서드는 HTTP 요청 및 응답 처리를 위해 사용된다. 
개발자는 Servlet 인터페이스의 `service()` 메서드를 `override`하여 비즈니스 로직을 처리한다. 

서블릿 컨테이너는 멀티 스레드로 서블릿을 관리하기 때문에 `인스턴스 변수`가 다른 스레드와 `공유`되는 것은 매우 위험하다. 아래 간단한 예시를 보자.

```java
@WebServlet(name = "sharedCounterServlet", urlPatterns = "/shared-counter")
public class SharedCounterServlet extends HttpServlet {

    private Integer sharedCounter;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        getServletContext().log("init() 호출");
        sharedCounter = 0;
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        getServletContext().log("service() 호출");
        sharedCounter++;
        response.getWriter().write(String.valueOf(sharedCounter));
    }

    @Override
    public void destroy() {
        getServletContext().log("destroy() 호출");
    }
}
```

위 코드는 인스턴스 변수 `sharedCounter`를 가지고 있다. 즉 동시에 요청이 올 경우 다수의 스레드는 해당 객체를 공유해서 사용할 수 있다.

> 컨테이너는 서블릿 클래스를 기반으로 서블릿 객체를 오직 하나만 만든다. 서블릿 객체는 컨테이너에 한 개씩만 인스턴스로 존재하게 된다.
> 하지만 이것은 싱글톤 객체는 아닌다. 싱글톤 객체는 객체를 한 번만 만들도록 내부에 private 생성자를 활용한다. 
> 하지만 서블릿은 싱글톤 처럼 구현되어 있지 않다. 컨테이너가 처음 실행될 때 구조상 서블릿을 한 번만 생성할 뿐이다.

또 다른 예시를 살펴보자.

```java
@WebServlet(name = "localCounterServlet", urlPatterns = "/local-counter")
public class LocalCounterServlet extends HttpServlet {

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        getServletContext().log("init() 호출");
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        getServletContext().log("service() 호출");
        response.addHeader("Content-Type", "text/html; charset=utf-8");
        int localCounter = 0;
        localCounter++;
        response.getWriter().write(String.valueOf(localCounter));
    }

    @Override
    public void destroy() {
        getServletContext().log("destroy() 호출");
    }
}
```

앞선 예시 코드와는 다르게 지역 변수 `localCounter`를 사용하고 있다. 각각의 스레드는 고유한 `Stack 영역`을 가지고 있다.
이러한 지역 변수는 `Stack 영역`에서 개별적으로 관리되기 때문에 공유되지 않는다. 즉 동시성 문제를 고려하지 않아도 된다.

