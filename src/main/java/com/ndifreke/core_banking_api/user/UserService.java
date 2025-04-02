package com.ndifreke.core_banking_api.user;

import com.ndifreke.core_banking_api.entity.User;
import com.ndifreke.core_banking_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

/**
 * The type User service.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Register user user.
     *
     * @param user the user
     * @return the user
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Gets user by id.
     *
     * @param userId the user id
     * @return the user by id
     */
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Update user user.
     *
     * @param updatedUser the updated user
     * @return the user
     */
    public User updateUser(User updatedUser) {
        Optional<User> existingUser = userRepository.findById(updatedUser.getUserId());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update fields as needed
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()){
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            return userRepository.save(user);
        }
        return null; // Or throw an exception
    }

    /**
     * Delete user.
     *
     * @param userId the user id
     */
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}