package com.gerolori.fasteat.web.auth;

import com.gerolori.fasteat.domain.entity.RoleName;
import java.util.Set;
import java.util.UUID;

public record PrincipalSummary(UUID userId, Set<RoleName> roles) {
}
