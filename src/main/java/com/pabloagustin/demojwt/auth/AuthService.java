package com.pabloagustin.demojwt.auth;

import com.pabloagustin.demojwt.jwt.JwtService;
import com.pabloagustin.demojwt.user.Role;
import com.pabloagustin.demojwt.user.User;
import com.pabloagustin.demojwt.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	// Repositorio de usuarios para acceder a las operaciones como por ejemplo save()
	private final UserRepository userRepository;

	// Servicio de JWT que maneja todas las operaciones para el token JWT
	private final JwtService jwtService;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	// Implementaremos los metodos para las respuestas al login y al register del usuario
	public AuthResponse login(LoginRequest request){
		// Obtenemos el username y el password del usuario desde el AuthenticatioManager
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				request.getUsername(),
				request.getPassword()
		));
		// Si el usuario se autentico correctamente, debemos generar el TOKEN
		UserDetails user = userRepository.findByUsername(request.getUsername())
				.orElseThrow();
		// Generamos el token
		String token = jwtService.getToken(user);
		return AuthResponse.builder()
				.token(token)
				.build();
	}

	public AuthResponse register(RegisterRequest request) {
		User user = User.builder()
				.username(request.getUsername())
				.password(passwordEncoder.encode( request.getPassword()))
				.firstname(request.getFirstname())
				.lastname(request.lastname)
				.country(request.getCountry())
				.role(Role.USER)
				.build();

		userRepository.save(user);

		return AuthResponse.builder()
				.token(jwtService.getToken(user))
				.build();

	}
}
