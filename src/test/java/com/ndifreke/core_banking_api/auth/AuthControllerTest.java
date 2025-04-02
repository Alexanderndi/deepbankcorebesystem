package com.ndifreke.core_banking_api.auth;

import com.ndifreke.core_banking_api.auth.dto.AuthenticationRequest;
import com.ndifreke.core_banking_api.auth.dto.AuthenticationResponse;
import com.ndifreke.core_banking_api.auth.dto.RegisterRequest;
import com.ndifreke.core_banking_api.entity.User;
import com.ndifreke.core_banking_api.entity.enums.user.UserRoleEnum;
import com.ndifreke.core_banking_api.exception.UserRegistrationException;
import com.ndifreke.core_banking_api.repository.UserRepository;
import com.ndifreke.core_banking_api.service.notification.MailService;
import com.ndifreke.core_banking_api.user.CustomUserDetailsService;
import com.ndifreke.core_banking_api.util.JwtUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @Mock
    private Validator validator; // Add Validator mock

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Collections.singletonList(UserRoleEnum.USER));

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setRoles(Collections.singletonList("USER"));

        authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername("testuser");
        authenticationRequest.setPassword("password123");
    }

    // Tests for registerUser endpoint
    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet()); // Mock validator to return no violations
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully.", response.getBody());
        verify(userRepository, times(1)).save(any(User.class));
        verify(validator, times(1)).validate(any(User.class));
    }

    @Test
    void registerUser_UsernameExists_Failure() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already exists.", response.getBody());
        verify(userRepository, never()).save(any(User.class));
        verify(validator, never()).validate(any(User.class));
    }

    @Test
    void registerUser_EmailExists_Failure() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already exists.", response.getBody());
        verify(userRepository, never()).save(any(User.class));
        verify(validator, never()).validate(any(User.class));
    }

    @Test
    void registerUser_ValidationFailure() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        // Simulate validation failure
        ConstraintViolation<User> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("Invalid email format");
        when(validator.validate(any(User.class))).thenReturn(Set.of(violation));

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof com.ndifreke.core_banking_api.exception.dto.ErrorResponse);
        assertEquals("Entity validation failed: email: Invalid email format",
                ((com.ndifreke.core_banking_api.exception.dto.ErrorResponse) response.getBody()).getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_UnexpectedError_Failure() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<?> response = authController.registerUser(registerRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred during registration.", response.getBody());
        verify(userRepository, never()).save(any(User.class));
        verify(validator, never()).validate(any(User.class));
    }

    // Tests for createAuthenticationToken endpoint
    @Test
    void createAuthenticationToken_Success() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwtToken");
        when(userRepository.findByUsername(authenticationRequest.getUsername())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(authenticationRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthenticationResponse authResponse = (AuthenticationResponse) response.getBody();
        assertEquals("jwtToken", authResponse.getJwt());
        verify(mailService, times(1)).sendLoginEmail(testUser.getEmail(), testUser.getFirstName(), testUser.getLastName());
    }

    @Test
    void createAuthenticationToken_BadCredentials_Failure() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(authenticationRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof com.ndifreke.core_banking_api.exception.dto.ErrorResponse);
        assertEquals("Incorrect username or password",
                ((com.ndifreke.core_banking_api.exception.dto.ErrorResponse) response.getBody()).getMessage());
        verify(mailService, never()).sendLoginEmail(anyString(), anyString(), anyString());
    }

    @Test
    void createAuthenticationToken_UserNotFound_Failure() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwtToken");
        when(userRepository.findByUsername(authenticationRequest.getUsername())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(authenticationRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof com.ndifreke.core_banking_api.exception.dto.ErrorResponse);
        assertEquals("An unexpected error occurred during login.",
                ((com.ndifreke.core_banking_api.exception.dto.ErrorResponse) response.getBody()).getMessage());
    }
}