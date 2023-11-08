package com.pabloagustin.demojwt.service;


import com.pabloagustin.demojwt.auth.UpdateRequest;
import com.pabloagustin.demojwt.user.Role;
import com.pabloagustin.demojwt.user.User;
import com.pabloagustin.demojwt.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final PasswordEncoder passwordEncoder;

	private final UserRepository userRepository;

	@Transactional
	public User update(Long userId, UpdateRequest updatedUser){
		Optional<User> optionalUser = userRepository.findById(userId);

		if (optionalUser.isPresent()) {
			User existingUser = optionalUser.get();
			// Actualizamos los campos
			existingUser.setFirstname(updatedUser.getFirstname());
			existingUser.setLastname(updatedUser.getLastname());
			existingUser.setCountry(updatedUser.getCountry());
			existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
			existingUser.setUsername(existingUser.getUsername());

			// Guardamos el usuario actualizado en la BD
			return userRepository.save(existingUser);
		} else {
			// Si el usuario no se encuentra
			throw new UsernameNotFoundException("Usuario no encontrado");
		}

	}
}
