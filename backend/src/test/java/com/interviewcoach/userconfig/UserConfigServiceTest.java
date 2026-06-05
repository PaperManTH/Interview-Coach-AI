package com.interviewcoach.userconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户配置服务测试类。
 */
class UserConfigServiceTest {

    private UserConfigService service;
    private UserProviderConfigRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserProviderConfigRepository();
        service = new UserConfigService(repository);
    }

    @Test
    void testSaveAndGetConfig() {
        String userId = "test-user-001";
        
        UserProviderConfig config = new UserProviderConfig();
        config.setAsrType("iflytek");
        config.setAsrApiKey("test-api-key");
        config.setLlmType("openai");
        config.setLlmModel("gpt-4o-mini");
        
        UserProviderConfig saved = service.saveConfig(userId, config);
        
        assertNotNull(saved);
        assertEquals(userId, saved.getUserId());
        assertEquals("iflytek", saved.getAsrType());
        assertEquals("openai", saved.getLlmType());
        
        UserProviderConfig retrieved = service.getConfig(userId);
        assertNotNull(retrieved);
        assertEquals("iflytek", retrieved.getAsrType());
        assertEquals("gpt-4o-mini", retrieved.getLlmModel());
    }

    @Test
    void testConfigExists() {
        String userId = "test-user-002";
        
        assertFalse(service.hasConfig(userId));
        
        UserProviderConfig config = new UserProviderConfig();
        config.setAsrType("mock");
        service.saveConfig(userId, config);
        
        assertTrue(service.hasConfig(userId));
    }

    @Test
    void testDeleteConfig() {
        String userId = "test-user-003";
        
        UserProviderConfig config = new UserProviderConfig();
        config.setAsrType("mock");
        service.saveConfig(userId, config);
        
        assertTrue(service.hasConfig(userId));
        
        service.deleteConfig(userId);
        assertFalse(service.hasConfig(userId));
        assertNull(service.getConfig(userId));
    }

    @Test
    void testUpdateConfig() {
        String userId = "test-user-004";
        
        UserProviderConfig config = new UserProviderConfig();
        config.setAsrType("mock");
        service.saveConfig(userId, config);
        
        UserProviderConfig updated = new UserProviderConfig();
        updated.setAsrType("iflytek");
        updated.setAsrApiKey("new-key");
        service.saveConfig(userId, updated);
        
        UserProviderConfig retrieved = service.getConfig(userId);
        assertEquals("iflytek", retrieved.getAsrType());
        assertEquals("new-key", retrieved.getAsrApiKey());
    }
}
