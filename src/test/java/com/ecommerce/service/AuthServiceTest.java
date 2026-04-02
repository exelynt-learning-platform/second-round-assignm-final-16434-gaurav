package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;

class AuthServiceTest {

	@Test
	void register_shouldReturnToken() {

		UserRepository repo = Mockito.mock(UserRepository.class);
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		JwtUtil jwt = Mockito.mock(JwtUtil.class);

		AuthService service = new AuthService(repo, encoder, jwt);

		User user = new User();
		user.setUsername("test");
		user.setPassword("123");

		Mockito.when(repo.save(Mockito.any(User.class))).thenReturn(user);
		Mockito.when(jwt.generateToken("test")).thenReturn("token");

		String result = service.register(user);

		assertNotNull(result);
	}

	@Test
	void login_shouldReturnToken() {

		UserRepository repo = Mockito.mock(UserRepository.class);
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		JwtUtil jwt = Mockito.mock(JwtUtil.class);

		AuthService service = new AuthService(repo, encoder, jwt);

		User user = new User();
		user.setUsername("test");
		user.setPassword(encoder.encode("123"));

		Mockito.when(repo.findByUsername("test")).thenReturn(Optional.of(user));

		Mockito.when(jwt.generateToken("test")).thenReturn("token");

		String result = service.login("test", "123");

		assertEquals("token", result);
	}
}