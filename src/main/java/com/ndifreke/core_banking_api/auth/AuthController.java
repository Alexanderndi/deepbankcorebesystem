package com.ndifreke.core_banking_api.auth;

import com.ndifreke.core_banking_api.auth.dto.AuthenticationRequest;
import com.ndifreke.core_banking_api.auth.dto.RegisterRequest;
import com.ndifreke.core_banking_api.dto.AuthenticationResponse;
import com.ndifreke.core_banking_api.service.notification.MailService;
import com.ndifreke.core_banking_api.entity.enums.user.UserRoleEnum;
import com.ndifreke.core_banking_api.user.CustomUserDetailsService;
import com.ndifreke.core_banking_api.entity.User;
import com.ndifreke.core_banking_api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ndifreke.core_banking_api.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Auth controller.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    /**
     * Create authentication token response entity.
     *
     * @param authenticationRequest the authentication request
     * @return the response entity
     * @throws Exception the exception
     */
    @Operation(summary = "Authenticate user and generate JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

            final String jwt = jwtUtil.generateToken(userDetails);

            User user = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow();
            // Send login email
            mailService.sendLoginEmail(user.getEmail(), user.getFirstName(), user.getLastName());

            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }
    }

    /**
     * Register user response entity.
     *
     * @param registerRequest the register request
     * @return the response entity
     */
    @Operation(summary = "Authenticate user and generate JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful", content = @Content),
            @ApiResponse(responseCode = "401", description = "Registration failed", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());

        List<UserRoleEnum> roles = registerRequest.getRoles().stream()
                .map(UserRoleEnum::valueOf)
                .collect(Collectors.toList());

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}