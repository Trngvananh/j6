package com.davisys.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.davisys.dao.UserDAO;
import com.davisys.entity.Users;
import com.davisys.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;;

@Configuration
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	/*
	 * @Override protected void doFilterInternal(HttpServletRequest request,
	 * HttpServletResponse response, FilterChain filterChain) throws
	 * ServletException, IOException { // TODO Auto-generated method stub
	 * 
	 * }
	 */

	@Value("${jwt.secret}")
	private String secret;
	
	@Autowired JwtTokenUtil jwtService;
	
	@Autowired UserDAO dao;

	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
    		throws ServletException, IOException{
        final String header = request.getHeader("Authorization");
        
        System.out.println("=============START FILTER=========");

        if (header != null && header.startsWith("Bearer ")){
            String authToken = header.substring(7);

            try {
            	Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
            	JWTVerifier jwtVerifier = JWT.require(algorithm).build();
				
				DecodedJWT decodedJWT = jwtVerifier.verify(authToken);
				
				String username = decodedJWT.getSubject();
                System.out.println("username: "+username);
//                Users u = dao.findEmailUser(username);
                
                String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                if (username != null){
                	Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                	Arrays.stream(roles).forEach(role -> {
                		authorities.add(new SimpleGrantedAuthority(role));
                	});
                	
                	//Xuất ra coi chơi
                	System.out.println("Xuất ra coi chơi");
                	authorities.forEach(authority -> System.out.println(authority.getAuthority()));

                	System.out.println("2");
    				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
    						username, authorities);
                	System.out.println("3");
    				
    				// Đưa thông tin xác thực vào SecurityContextHolder
    	            SecurityContextHolder.getContext().setAuthentication(authenticationToken);


                	System.out.println("4");
    				chain.doFilter(request, response);

                	System.out.println("5");
    				return;
                }
            }
            catch (Exception e){
                System.out.println("Unable to get JWT Token, possibly expired");
                System.out.println(e);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        chain.doFilter(request, response);
    }
	/*
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader(AUTHORIZATION);

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			System.out.println("\n\nok: "+authorizationHeader);
			try {
				String token = authorizationHeader.substring("Bearer ".length());
				System.out.println("token: "+token);
				
				Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
				System.out.println(1);
				
				JWTVerifier jwtVerifier = JWT.require(algorithm).build();
				System.out.println(2);
				
				DecodedJWT decodedJWT = jwtVerifier.verify(token);
				System.out.println(3);
				
				String username = decodedJWT.getSubject();
				System.out.println("uname:" + username);
				Claim role = decodedJWT.getClaim("role");
				
				//System.out.println(username);

				Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						username, role);
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);

				filterChain.doFilter(request, response);

			} catch (Exception e) {
				response.setHeader("error", e.getMessage());
				response.setStatus(FORBIDDEN.value());

				Map<String, String> err = new HashMap<>();
				err.put("error_message", e.getMessage());
				response.setContentType(APPLICATION_JSON_VALUE);

				new ObjectMapper().writeValue(response.getOutputStream(), e);
			}
		} else {
			filterChain.doFilter(request, response);
		}

	}
	*/

}
