package com.pabloagustin.demojwt.auth;

import lombok.*;

// Anotaciones de lombok para codigo limpio
// Evitando creacion de constructores, getters, setters.. etc.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

	 String username;
	 String password;
}
