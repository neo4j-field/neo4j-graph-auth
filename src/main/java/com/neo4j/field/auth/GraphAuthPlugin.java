package com.neo4j.field.auth;

import com.neo4j.server.security.enterprise.auth.plugin.api.AuthProviderOperations;
import com.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthorizationInfo;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthorizationPlugin;

import java.security.AuthProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphAuthPlugin extends AuthorizationPlugin.Adapter {

    private AuthProviderOperations api;

    private static final String ROLE_LOOKUP =
            "MATCH (:User {name: $name})-[*]->(r:Role) RETURN r.name AS role";

    @Override
    public String name() {
        return "graph-auth";
    }

    @Override
    public AuthorizationInfo authorize(Collection<PrincipalAndProvider> principals) {
        try {
            for (PrincipalAndProvider pap : principals) {
                if (pap.provider() == "native") {
                    final String principal = pap.principal().toString();
                    final String dbName = GraphAuthExtensionFactory.SECURITY_DATABASE_NAME;

                    return AuthorizationInfo.of(
                            GraphAuthExtensionFactory.execute(
                                    dbName, ROLE_LOOKUP, Collections.singletonMap("name", principal))
                            .stream()
                            .map(row -> row.get("role").toString())
                            .collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            api.log().error(e.getMessage());
            e.printStackTrace(System.err);
        }
        return AuthorizationInfo.of(Collections.singleton(PredefinedRoles.PUBLIC));
    }

    @Override
    public void initialize(AuthProviderOperations authProviderOperations) {
        api = authProviderOperations;
        api.log().info("initialized " + this.getClass().getCanonicalName());
    }

    @Override
    public void start() {
        if (api != null) {
            api.log().info("starting " + this.getClass().getCanonicalName());
        }
    }

    @Override
    public void stop() {
        if (api != null) {
            api.log().info("stopping " + this.getClass().getCanonicalName());
        }
    }

    @Override
    public void shutdown() {
        if (api != null) {
            api.log().info("shutting down " + this.getClass().getCanonicalName());
        }
    }
}
