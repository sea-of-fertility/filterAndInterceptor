package com.example.filterandinterceptor.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


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
