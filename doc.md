# How to Use Bearer Token from Auth Container in Other Containers

## Overview

The Auth container issues **JWT access tokens** signed with HMAC-SHA256. Any other container that shares the same `JWT_SECRET` can verify and extract user information from the token **without calling the Auth service**.

---

## 1. Shared Secret

All containers must use the **same `JWT_SECRET`** environment variable.

In your deployment config (e.g. `docker-compose.yml`):

```yaml
services:
  auth:
    environment:
      JWT_SECRET: <your-base64-encoded-secret>
  
  order-service:
    environment:
      JWT_SECRET: <same-base64-encoded-secret>

  payment-service:
    environment:
      JWT_SECRET: <same-base64-encoded-secret>
```

> The secret must be a **Base64-encoded** string of at least 32 bytes (256 bits) for HS256.

---

## 2. Dependencies

Add JJWT to your service's `build.gradle.kts`:

```kotlin
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
```

---

## 3. Configuration

Add to your `application.properties`:

```properties
jwt.secret=${JWT_SECRET}
```

---

## 4. JWT Claims Reference

The Auth container embeds these claims in the access token:

| Claim   | Type   | Description                       | Example                                |
|---------|--------|-----------------------------------|----------------------------------------|
| `sub`   | String | User UUID (primary key)           | `550e8400-e29b-41d4-a716-446655440000` |
| `email` | String | User email address                | `ahmad@example.com`                    |
| `name`  | String | User full name                    | `Ahmad Buruh`                          |
| `role`  | String | User role                         | `BURUH`, `MANDOR`, `SUPIR_TRUK`, `ADMIN` |
| `iat`   | Long   | Issued at (epoch seconds)         | `1709542800`                           |
| `exp`   | Long   | Expiration (epoch seconds)        | `1709543700` (iat + 900s)              |

---

## 5. Token Verification Code

### Option A: Minimal Utility Class (No Spring Security)

```java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretKey signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String getEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Option B: Servlet Filter (for all endpoints)

```java
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing token\"}");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            response.setStatus(401);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid token\"}");
            return;
        }

        Claims claims = jwtUtil.extractClaims(token);

        // Make user info available to controllers
        request.setAttribute("userId", claims.getSubject());
        request.setAttribute("userRole", claims.get("role", String.class));
        request.setAttribute("userEmail", claims.get("email", String.class));
        request.setAttribute("userName", claims.get("name", String.class));

        chain.doFilter(request, response);
    }
}
```

### Using in Controllers

```java
@GetMapping("/orders")
public ResponseEntity<?> getOrders(HttpServletRequest request) {
    String userId = (String) request.getAttribute("userId");
    String role = (String) request.getAttribute("userRole");

    // Use userId and role for business logic
}
```

---

## 6. Client-Side Flow

The JWT is **NOT** automatically stored or sent. The client (frontend) must handle it:

```
1. POST /api/v1/auth/login  →  { accessToken, refreshToken }
2. Store tokens (localStorage, sessionStorage, or in-memory)
3. Attach to every request:
     Authorization: Bearer <accessToken>
4. When accessToken expires (15 min):
     POST /api/v1/auth/refresh  { refreshToken }  →  new tokens
5. When refreshToken expires (7 days):
     User must login again
```

### JavaScript Example

```js
// Login
const res = await fetch("http://auth-service:8001/api/v1/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ email: "user@example.com", password: "pass" })
});
const { accessToken, refreshToken } = (await res.json()).data;

// Call another service with the token
const orders = await fetch("http://order-service:8002/api/v1/orders", {
  headers: { "Authorization": `Bearer ${accessToken}` }
});
```

---

## 7. Role-Based Access Example

```java
String role = (String) request.getAttribute("userRole");

if (!"MANDOR".equals(role)) {
    return ResponseEntity.status(403).body("Mandor access only");
}
```

Available roles: `ADMIN`, `BURUH`, `MANDOR`, `SUPIR_TRUK`

---

## 8. Architecture Diagram

```
┌──────────┐     login/register      ┌──────────────┐
│  Client   │ ──────────────────────► │ Auth Service  │
│ (Frontend)│ ◄────────────────────── │  :8001        │
│           │   { accessToken, ...}   └──────────────┘
│           │                          (signs JWT with JWT_SECRET)
│           │
│           │   Authorization: Bearer <token>
│           │ ──────────────────────► ┌──────────────┐
│           │ ◄────────────────────── │ Other Service │
└──────────┘                          │  :800x        │
                                      └──────────────┘
                                       (verifies JWT with same JWT_SECRET)
```
