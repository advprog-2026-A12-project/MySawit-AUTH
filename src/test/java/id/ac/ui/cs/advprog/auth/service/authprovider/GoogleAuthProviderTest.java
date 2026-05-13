package id.ac.ui.cs.advprog.auth.service.authprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.auth.service.oauth.OAuthClient;
import id.ac.ui.cs.advprog.auth.service.utils.GoogleUserInfo;
import id.ac.ui.cs.advprog.auth.service.utils.UsernameGenerator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoogleAuthProviderTest {

    @Mock private UserRepository userRepository;
    @Mock private OAuthClient oauthClient;
    @Mock private UsernameGenerator usernameGenerator;

    private GoogleAuthProvider provider;

    @BeforeEach
    void setUp() {
        provider = new GoogleAuthProvider(userRepository, oauthClient, usernameGenerator);
    }

    @Test
    void createsDefaultBuruhWhenNoRoleProvided() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", null, null);

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-1", "u@example.com", "User Name"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-1"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(usernameGenerator.generateUniqueUsername(anyString())).thenReturn("gen-user");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(cap.capture())).thenAnswer(inv -> inv.getArgument(0));

        User saved = provider.authenticate(req);

        assertEquals("GOOGLE", saved.getOauthProvider());
        assertEquals("sub-1", saved.getOauthProviderId());
        assertEquals("gen-user", saved.getUsername());
        assertEquals("u@example.com", saved.getEmail());
        assertEquals(true, saved.isActive());
        assertEquals("BURUH", saved.getRole().name());
    }

    @Test
    void createsMandorWhenRequestedAndCertProvided() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", "MANDOR", "CERT-1");

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-2", "m@example.com", "Mandor"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-2"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("m@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(usernameGenerator.generateUniqueUsername(anyString())).thenReturn("mandor-user");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(cap.capture())).thenAnswer(inv -> inv.getArgument(0));

        provider.authenticate(req);

        assertEquals("MANDOR", cap.getValue().getRole().name());
        assertEquals("CERT-1", cap.getValue().getMandorCertificationNumber());
    }

    @Test
    void throwsIfRequestedRoleIsAdmin() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", "ADMIN", null);
        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-3", "a@example.com", "A"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-3"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(usernameGenerator.generateUniqueUsername(anyString())).thenReturn("admin-user");

        assertThrows(UnprocessableEntityException.class, () -> provider.authenticate(req));
    }

    @Test
    void throwsIfMandorRequestedWithoutCert() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", "MANDOR", null);
        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-4", "m2@example.com", "M2"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-4"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("m2@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(usernameGenerator.generateUniqueUsername(anyString())).thenReturn("m2-user");

        assertThrows(UnprocessableEntityException.class, () -> provider.authenticate(req));
    }

    @Test
    void linksExistingUserWhenEmailMatchesAndProviderEmpty() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", null, null);

        User existing = new User();
        existing.setEmail("ex@example.com");
        existing.setOauthProvider(null);
        existing.setOauthProviderId(null);

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-5", "ex@example.com", "Ex"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-5"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("ex@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        User u = provider.authenticate(req);

        assertEquals("GOOGLE", u.getOauthProvider());
        assertEquals("sub-5", u.getOauthProviderId());
    }

    @Test
    void throwsWhenEmailExistsButDifferentProviderLinked() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", null, null);

        User existing = new User();
        existing.setEmail("ex2@example.com");
        existing.setOauthProvider("GITHUB");
        existing.setOauthProviderId("gh-sub");

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-6", "ex2@example.com", "Ex2"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-6"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("ex2@example.com")).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class, () -> provider.authenticate(req));
    }

    @Test
    void usesExistingProviderUserIfFound() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", null, null);

        User existing = new User();
        existing.setEmail("ex3@example.com");
        existing.setOauthProvider("GOOGLE");
        existing.setOauthProviderId("sub-exist");

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-exist", "ex3@example.com", "Ex3"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-exist"))
                .thenReturn(Optional.of(existing));

        User u = provider.authenticate(req);

        verify(userRepository, never()).save(any(User.class));
        assertEquals(existing, u);
    }

    @Test
    void throwsWhenRequestedRoleIsInvalid() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", "FOO", null);

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-7", "inv@example.com", "Inv"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-7"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("inv@example.com")).thenReturn(Optional.empty());
        when(usernameGenerator.generateUniqueUsername(anyString())).thenReturn("inv-user");

        assertThrows(InvalidUserRequestException.class, () -> provider.authenticate(req));
    }

    @Test
    void throwsWhenExistingProviderUserIsInactive() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", null, null);

        User existing = new User();
        existing.setOauthProvider("GOOGLE");
        existing.setOauthProviderId("sub-inactive");
        existing.setActive(false);

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-inactive", "ia@example.com", "IA"));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-inactive"))
                .thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class, () -> provider.authenticate(req));
    }

    @Test
    void createsUserUsingEmailWhenNameMissing() {
        GoogleAuthRequest req = new GoogleAuthRequest("code", "postmessage", null, null);

        when(oauthClient.authenticate("code", "postmessage"))
                .thenReturn(new GoogleUserInfo("sub-8", "no-name@example.com", ""));
        when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "sub-8"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("no-name@example.com")).thenReturn(Optional.empty());
        when(usernameGenerator.generateUniqueUsername("no-name@example.com")).thenReturn("noname-user");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(cap.capture())).thenAnswer(inv -> inv.getArgument(0));

        User saved = provider.authenticate(req);

        assertEquals("no-name@example.com", cap.getValue().getName());
        assertEquals("noname-user", cap.getValue().getUsername());
        assertEquals("BURUH", cap.getValue().getRole().name());
    }
}
