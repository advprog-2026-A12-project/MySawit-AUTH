package id.ac.ui.cs.advprog.auth.mapper;

import id.ac.ui.cs.advprog.auth.dto.response.management.DeletedUserResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UpdatedMyProfileResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.model.User;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapper {

    public UserSummaryResponseData toSummaryResponse(User user) {
        return UserSummaryResponseData.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .mandorCertificationNumber(user.getMandorCertificationNumber())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserPageResponseData toPageResponse(Page<User> usersPage, List<UserSummaryResponseData> content) {
        return UserPageResponseData.builder()
                .content(content)
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .first(usersPage.isFirst())
                .last(usersPage.isLast())
                .build();
    }

    public DeletedUserResponseData toDeletedUserResponse(User user, Instant deletedAt) {
        return DeletedUserResponseData.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .deletedAt(deletedAt)
                .build();
    }

    public UpdatedMyProfileResponseData toUpdatedProfileResponse(User user) {
        return UpdatedMyProfileResponseData.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
