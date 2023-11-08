package com.pabloagustin.demojwt.auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequest {

	String username;
	String password;
	String firstname;
	String lastname;
	String country;
}
