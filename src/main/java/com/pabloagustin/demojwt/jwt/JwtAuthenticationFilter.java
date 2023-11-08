package com.pabloagustin.demojwt.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain)
			throws ServletException, IOException {
		// Tenemos que obtener el token del REQUEST
		final String token = getTokenFromRequest(request);
		final String username;
		// Si el token es nulo, debemos devolver a la cadena de FILTROS el control
		if (token == null){
			filterChain.doFilter(request, response);
			return;
		}
		// Acceder al username si funciona
		username = jwtService.getUsernameFromToken(token);

		// Si no lo encontramos en el security context holder, lo buscamos en la BD
		if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			// Validamos el token mediate el servicio de jwt
			if (jwtService.isTokenValid(token, userDetails)){
				// Si es valido, actualizamos el security context holder
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
				);
				// Seteamos el details
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// Obtenemos el contexto del security context holder y seteamos la autenticacion
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		// Que continue con el filtro
		filterChain.doFilter(request, response);
	}

	// Implementacion del metodo para obtener el TOKEN de la request
	private String getTokenFromRequest(HttpServletRequest request){
		// Debemos obtener del header la cadena de autentication
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		// Verificar el token mediante la palabra "Bearer " para obtener el token
		if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")){
			// Extraemos el token y lo retornamos. Desde el 7 x el "Bearer " hasta el final = token;
			return authHeader.substring(7);
		}
		return null;
	}
}
