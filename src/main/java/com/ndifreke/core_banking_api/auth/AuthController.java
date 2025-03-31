package com.ndifreke.core_banking_api.auth;

import com.ndifreke.core_banking_api.notification.MailService;
import com.ndifreke.core_banking_api.security.UserRoleEnum;
import com.ndifreke.core_banking_api.user.CustomUserDetailsService;
import com.ndifreke.core_banking_api.user.User;
import com.ndifreke.core_banking_api.user.UserRepository;
import com.ndifreke.core_banking_api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.ndifreke.core_banking_api.user.UserRepository;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    @Value("${app.verification.url}")
    private String verificationUrl;


    @Operation(summary = "Authenticate user and generate JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Account not verified", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            User user = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow();

            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account not verified. Please verify your account.");
            }

            final String jwt = jwtUtil.generateToken(userDetails);

            // Send login email
            mailService.sendLoginEmail(user.getEmail(), user.getFirstName(), user.getLastName());

            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }
    }

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
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

        // Generate Verification Code
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);

        userRepository.save(user);

        // Send verification email
        mailService.sendVerificationEmail(user.getEmail(), verificationCode, verificationUrl);


        return ResponseEntity.ok("User registered successfully. Please check your email to verify your account.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestBody VerificationRequest verificationRequest) {
        User user = userRepository.findByUsername(verificationRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCode().equals(verificationRequest.getVerificationCode())) {
            user.setVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return ResponseEntity.ok("Account verified successfully. You can now log in.");
        } else {
            return ResponseEntity.badRequest().body("Invalid verification code.");
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
}