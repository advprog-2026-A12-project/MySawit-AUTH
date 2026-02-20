package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.UserRequest;
import id.ac.ui.cs.advprog.auth.dto.UserResponse;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        validateRequest(request);

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("username");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("email");
        }

        User user = mapRequestToEntity(request, new User());
        return mapEntityToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapEntityToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return mapEntityToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        validateRequest(request);

        User existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!existingUser.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("username");
        }
        if (!existingUser.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("email");
        }

        User updatedUser = mapRequestToEntity(request, existingUser);
        return mapEntityToResponse(userRepository.save(updatedUser));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }

    private void validateRequest(UserRequest request) {
        if (request == null) {
            throw new InvalidUserRequestException("request body is required");
        }
        validateText(request.username(), "username");
        validateText(request.email(), "email");
        validateText(request.name(), "name");
        validateText(request.password(), "password");
        if (request.role() == null) {
            throw new InvalidUserRequestException("role is required");
        }
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserRequestException(fieldName + " is required");
        }
    }

    private User mapRequestToEntity(UserRequest request, User user) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(request.password());
        user.setRole(request.role());
        return user;
    }

    private UserResponse mapEntityToResponse(User user) {
        UserRole role = user.getRole();
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getName(), role);
    }
}
