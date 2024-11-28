# 학습 정리

## 목차
1. [Filter && interceptor](#Filter-&&-interceptor)
    - [공통점](#공통점)
    - [차이점](#차이점)
    - [Filter interface](#Filter-interface)
    - [HandlerInterceptor Interface](#HandlerInterceptor-Interface)
    - [CustomInterceptor](#CustomInterceptor)
   - [특정 URL에서만 작동하는 Filter & Interceptor 설정하는 방법](#특정-url에서만-작동하는-filter--interceptor-설정하는-방법)





## Filter && interceptor

<p align="center"><img src="https://velog.velcdn.com/images/snowspring/post/71fcaf16-43a7-40a8-9fd2-227876da41a7/image.png"  width="50%" height="50%"></p>

### 공통점
+ 특정 URL에 접근할 때 사용된다는 공통점이 있습니다.


### 차이점
#### 영역의 차이
+ Filter는 웹 애플리케이션의 영역 내에서 필요한 자원들을 활용합니다.
웹 애플리케이션 내에서 동작하므로, 스프링의 Context를 접근하기 어렵습니다.

+ Interceptor의 경우 스프링에서 관리되기 때문에 스프링 내의 모든 객체에 접근이 가능하다는 차이가 있습니다. 즉, 빈을 관리하는 스프링 컨텍스트 내에 있어서 생성된 빈들에 자유롭게 접근할 수 있습니다.

예를 들어, 추후에 살펴볼 HandlerInterceptor의 경우
스프링의 빈으로 등록된 컨트롤러나 서비스 객체들을 주입받아서 사용할 수 있기 때문에
기존의 구조를 그래도 활용하면서 추가적인 작업이 가능합니다.


#### 호출 시점의 차이
Filter는 DispatherServlet이 실행되기 전, Interceptor는 DispatherServlet이 실행된 후에 호출되며 Interceptor는 DispatherServlet이 실행되며 호출됩니다.


#### 용도의 차이
+ Filter

👉🏻 보안 관련 공통 작업

👉🏻 모든 요청에 대한 로깅 또는 감사
  
👉🏻 이미지/데이터 압축 및 문자열 인코딩

+ Interceptor

👉🏻 인증/인가 등과 같은 공통 작업
 
👉🏻 Controller로 넘겨주는 정보의 가공

👉🏻 API 호출에 대한 로깅 또는 감사


### Filter interface
``` java
public interface Filter {
    default void init(FilterConfig filterConfig) throws ServletException {
    }

    void doFilter(ServletRequest var1, ServletResponse var2, FilterChain var3) throws IOException, ServletException;

    default void destroy() {
    }
}

```
#### Filter interface의 각 메소드의 설명
+ init: 필터가 생성될 때 수행되는 메소드

+ doFilter: Request, Response가 필터를 거칠 때 수행되는 메소드

+ destroy: 필터가 소멸될 때 수행되는 메소드

#### 스스로 만들어 보는 custom filter

Filter를 만들기 위해서는 Filter interface를 상속받는 class를 생성한다.
간단한 log를 생성하는 코드를 작성했다.

``` java 
@Slf4j
public class CustomFilter implements Filter {

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        try{
            log.info("REQUEST URI: {}", requestURI);
            filterChain.doFilter(servletRequest, servletResponse);
        }
        catch (Exception e){
            throw new ServletException(e);

        }


    }

    @Override
    public void destroy() {
        log.info("CustomFilter가 사라집니다."); // 필터가 제거될 때 호출
    }

}

}

```


### HandlerInterceptor Interface

각 메소드가 default로 작성되있는 것을 확인할 수 있다.

``` java
public interface HandlerInterceptor {
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }
}

```

#### preHandle
+ 컨트롤러 호출 전에 호출된다.

#### postHandle
+ 컨트롤러가 요청을 처리한 이후에 호출됩니다.
+ 컨트롤러에서 예외가 발생하면 `postHandle` 은 호출되지 않는다.

#### afterCompletion
+ 예외가 발생하면 `postHandle()` 는 호출되지 않으므로 예외와 무관하게 공통 처리를 하려면 `afterCompletion()` 을 사용해야 한다.
+ 예외가 발생하면 `afterCompletion()` 에 예외 정보( `ex` )를 포함해서 호출된다.


### CustomInterceptor


``` java

@Slf4j
public class CustomInterceptor implements HandlerInterceptor {


    public static final String LOG_ID = "logId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String uuid = UUID.randomUUID().toString();
        request.setAttribute(LOG_ID, uuid);

        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
        }
        log.info("REQUEST  [{}][{}][{}]", uuid, requestURI, handler);
        return true;

    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse
            response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle [{}]", modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String logId = (String) request.getAttribute(LOG_ID);
        log.info("afterCompletion [{}][{}][{}]", logId, requestURI, handler);

        if (ex != null) {
            log.error("afterCompletion error!!", ex);
        }

    }
}



```



### 특정 URL에서만 작동하는 Filter & Interceptor 설정하는 방법


#### filter

``` java
FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
filterFilterRegistrationBean.addUrlPatterns("/**"); 
```
> addUrlPatterns에 원하는 URL을 입력하면 됩니다. 현재 /**로 입력되어있으므로 모든 경로에서 접근이 가능합니다.

#### interceptor

``` java
registry.addInterceptor(new CustomInterceptor()).addPathPatterns("/**");
```

> /** 값을 주었으므로 모든 경로의 URL에서 interceptor를 실행합니다.

#### config 전체 코드
``` java
@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new CustomFilter());
        filterFilterRegistrationBean.setOrder(1);
        filterFilterRegistrationBean.addUrlPatterns("/*");
        return filterFilterRegistrationBean;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CustomInterceptor()).addPathPatterns("/**");
    }

}
```


### 학습한 블로그 출처

[Spring Interceptor, 제대로 이해하기](https://gngsn.tistory.com/153)