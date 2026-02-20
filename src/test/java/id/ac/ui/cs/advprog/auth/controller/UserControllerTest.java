package id.ac.ui.cs.advprog.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.UserRequest;
import id.ac.ui.cs.advprog.auth.dto.UserResponse;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserRequest request;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        request = new UserRequest("andi", "andi@mail.com", "Andi", "pw", UserRole.Admin);
        response = new UserResponse(1L, "andi", "andi@mail.com", "Andi", UserRole.Admin);
    }

    @Test
    void createUserReturnsCreated() {
        when(userService.createUser(request)).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().id());
        verify(userService).createUser(request);
    }

    @Test
    void getAllUsersReturnsList() {
        when(userService.getAllUsers()).thenReturn(List.of(response));

        List<UserResponse> result = userController.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("andi", result.getFirst().username());
        verify(userService).getAllUsers();
    }

    @Test
    void getUserByIdReturnsData() {
        when(userService.getUserById(1L)).thenReturn(response);

        UserResponse result = userController.getUserById(1L);

        assertEquals("andi", result.username());
        verify(userService).getUserById(1L);
    }

    @Test
    void updateUserReturnsUpdatedData() {
        when(userService.updateUser(1L, request)).thenReturn(response);

        UserResponse result = userController.updateUser(1L, request);

        assertEquals("andi@mail.com", result.email());
        verify(userService).updateUser(1L, request);
    }

    @Test
    void deleteUserReturnsNoContent() {
        ResponseEntity<Void> result = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userService).deleteUser(1L);
    }
}
