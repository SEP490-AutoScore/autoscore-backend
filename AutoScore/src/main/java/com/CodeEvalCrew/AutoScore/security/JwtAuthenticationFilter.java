// package com.CodeEvalCrew.AutoScore.security;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.util.StringUtils;
// import org.springframework.web.filter.OncePerRequestFilter;

// import com.CodeEvalCrew.AutoScore.services.account_service.UserDetailsImpl;
// import com.CodeEvalCrew.AutoScore.services.account_service.UserDetailsServiceImpl;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// import java.io.IOException;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;

// @Component
// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
//     private final JwtTokenProvider tokenProvider;
//     private final UserDetailsServiceImpl userDetailsService;

//     public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
//         this.tokenProvider = tokenProvider;
//         this.userDetailsService = userDetailsService;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//             throws ServletException, IOException {
//         String requestURI = request.getRequestURI();
//         if (requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/swagger-ui")) {
//             filterChain.doFilter(request, response);
//             return; // Bỏ qua JWT Authentication cho Swagger
//         }

//         // Phần xử lý JWT Authentication như hiện tại...
//         try {
//             String jwt = getJwtFromRequest(request);
//             if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
//                 String email = tokenProvider.getEmailFromJWT(jwt);
//                 UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);

//                 UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                         userDetails, null, userDetails.getAuthorities());
//                 authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

//                 SecurityContextHolder.getContext().setAuthentication(authentication);
//             }
//         } catch (UsernameNotFoundException ex) {
//             logger.error("User not found with provided token", ex);
//         }

//         filterChain.doFilter(request, response);
//     }

//     private String getJwtFromRequest(HttpServletRequest request) {
//         String bearerToken = request.getHeader("Authorization");
//         return (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer "))
//                 ? bearerToken.substring(7)
//                 : null;
//     }
// }
