package com.neo4j.field.auth;

import com.neo4j.server.security.enterprise.auth.plugin.api.AuthProviderOperations;
import com.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthorizationInfo;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthorizationPlugin;

import java.util.Collection;
import java.util.Collections;

public class GraphAuthPlugin extends AuthorizationPlugin.Adapter {

    private AuthProviderOperations api;

    @Override
    public String name() {
        return "graph-auth";
    }

    @Override
    public AuthorizationInfo authorize(Collection<PrincipalAndProvider> principals) {
        for (PrincipalAndProvider pap : principals) {
            api.log().info(String.format("processing pap {provider: %s, principal: %s}", pap.provider(), pap.principal()));
        }
        return AuthorizationInfo.of(Collections.singleton(PredefinedRoles.ADMIN));
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
