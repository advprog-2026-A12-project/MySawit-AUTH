package id.ac.ui.cs.advprog.auth.mapper;

import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailAssembler {

    private final BuruhMandorAssignmentRepository buruhMandorAssignmentRepository;

    public UserDetailResponseData toDetailResponse(User user) {
        Map<String, Object> roleSpecificData = buildRoleSpecificData(user);

        return UserDetailResponseData.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .mandorCertificationNumber(user.getMandorCertificationNumber())
                .isActive(user.isActive())
                .oauthProvider(user.getOauthProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roleSpecificData(roleSpecificData)
                .build();
    }

    private Map<String, Object> buildRoleSpecificData(User user) {
        if (user.getRole() == UserRole.BURUH) {
            return buruhMandorAssignmentRepository.findByBuruhIdAndIsActiveTrue(user.getId())
                    .map(assignment -> {
                        Map<String, Object> assignedMandor = new HashMap<>();
                        assignedMandor.put("id", assignment.getMandor().getId());
                        assignedMandor.put("name", assignment.getMandor().getName());
                        assignedMandor.put("email", assignment.getMandor().getEmail());

                        Map<String, Object> roleSpecificData = new HashMap<>();
                        roleSpecificData.put("assignedMandor", assignedMandor);
                        roleSpecificData.put("assignedAt", assignment.getAssignedAt());
                        return roleSpecificData;
                    })
                    .orElseGet(Map::of);
        }

        if (user.getRole() == UserRole.MANDOR) {
            List<Map<String, Object>> assignedBuruhs = buruhMandorAssignmentRepository
                    .findAllByMandor_IdAndIsActiveTrue(user.getId(), Sort.by(Sort.Direction.DESC, "assignedAt"))
                    .stream()
                    .map(assignment -> {
                        Map<String, Object> assignedBuruh = new HashMap<>();
                        assignedBuruh.put("id", assignment.getBuruh().getId());
                        assignedBuruh.put("name", assignment.getBuruh().getName());
                        assignedBuruh.put("email", assignment.getBuruh().getEmail());
                        assignedBuruh.put("assignedAt", assignment.getAssignedAt());
                        return assignedBuruh;
                    })
                    .toList();

            Map<String, Object> roleSpecificData = new HashMap<>();
            roleSpecificData.put("assignedBuruhs", assignedBuruhs);
            roleSpecificData.put("totalAssignedBuruhs", assignedBuruhs.size());
            return roleSpecificData;
        }

        return Map.of();
    }
}
