package com.adisaputera.savingapp.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class RefreshTokenBlacklistService {

    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());

    public void blacklistToken(String jti) {
        blacklistedTokens.add(jti);
    }

    public boolean isBlacklisted(String jti) {
        return blacklistedTokens.contains(jti);
    }
}