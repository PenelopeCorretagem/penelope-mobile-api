package com.penelopec.penelopemobileapi.auth.application;

import java.time.LocalDate;

public record UserProfileResponse(String name, String email, LocalDate birthDate, String profileImage) {
	public UserProfileResponse(String name, String email, LocalDate birthDate) {
		this(name, email, birthDate, null);
	}
}