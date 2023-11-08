package com.pabloagustin.demojwt.demo;

import com.pabloagustin.demojwt.auth.AuthResponse;
import com.pabloagustin.demojwt.auth.RegisterRequest;
import com.pabloagustin.demojwt.auth.UpdateRequest;
import com.pabloagustin.demojwt.jwt.JwtService;
import com.pabloagustin.demojwt.service.UserService;
import com.pabloagustin.demojwt.user.User;
import com.pabloagustin.demojwt.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DemoController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	private final UserService userService;

	private final JwtService jwtService;
	// Metodo protegido
	@PostMapping("/demo")
	public String welcome(){
		return "Welcome from secure endpoint";
	}

	@PostMapping("/update/{userId}")
	public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody UpdateRequest updatedUser) {
		User userUpdating = userService.update(userId, updatedUser);
		return new ResponseEntity<>(userUpdating, HttpStatus.OK);
	}
}
