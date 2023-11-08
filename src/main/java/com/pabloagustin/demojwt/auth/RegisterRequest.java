package com.pabloagustin.demojwt.auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	 String username;
	 String password;
	 String firstname;
	 String lastname;
	 String country;
}
