package id.ac.ui.cs.advprog.auth.repository;

import id.ac.ui.cs.advprog.auth.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthProviderAndOauthProviderId(String oauthProvider, String oauthProviderId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, UUID id);
    boolean existsByMandorCertificationNumber(String mandorCertificationNumber);
    List<User> findAllByIsActiveTrueOrderByCreatedAtDesc();
}
