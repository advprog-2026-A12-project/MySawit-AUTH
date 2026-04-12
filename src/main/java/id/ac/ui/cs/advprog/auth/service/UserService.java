package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;

public interface UserService {

    UserPageResponseData getUsers(int page, int size, String sort, String name, String email, String role);
}