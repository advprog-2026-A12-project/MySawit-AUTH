package id.ac.ui.cs.advprog.auth.service.authprovider;

import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DefaultAuthProviderFactory implements AuthProviderFactory {

    private final Map<AuthProviderType, AuthProvider> providersByType;

    public DefaultAuthProviderFactory(List<AuthProvider> providers) {
        EnumMap<AuthProviderType, AuthProvider> map = new EnumMap<>(AuthProviderType.class);
        for (AuthProvider provider : providers) {
            map.put(provider.getType(), provider);
        }
        this.providersByType = Collections.unmodifiableMap(map);
    }

    @Override
    public AuthProvider create(AuthProviderType type) {
        AuthProvider provider = providersByType.get(type);
        if (provider == null) {
            throw new UnprocessableEntityException("Unsupported auth provider");
        }
        return provider;
    }
}
