package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.UpdateMyProfileRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.DeletedUserResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UpdatedMyProfileResponseData;
import java.util.UUID;

public interface UserService {

    UserPageResponseData getUsers(int page, int size, String sort, String name, String email, String role);

    UserPageResponseData getDeletedUsers(int page, int size, String sort, String name, String email, String role);

    UserDetailResponseData getUserById(UUID userId);

    DeletedUserResponseData deleteUser(UUID userId, UUID authenticatedAdminId);

    UpdatedMyProfileResponseData updateMyProfile(UUID userId, UpdateMyProfileRequest request);
}