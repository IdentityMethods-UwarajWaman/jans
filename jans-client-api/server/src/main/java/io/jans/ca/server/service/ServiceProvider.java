package io.jans.ca.server.service;

import io.jans.ca.server.service.auth.ConfigurationService;

public class ServiceProvider {

    private ValidationService validationService;
    private ConfigurationService configurationService;
    private HttpService httpService;
    private RpSyncService rpSyncService;
    private DiscoveryService discoveryService;
    private IntrospectionService introspectionService;
    private RpService rpService;

    public ValidationService getValidationService() {
        return validationService;
    }

    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public RpSyncService getRpSyncService() {
        return rpSyncService;
    }

    public void setRpSyncService(RpSyncService rpSyncService) {
        this.rpSyncService = rpSyncService;
    }

    public DiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public void setDiscoveryService(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public RpService getRpService() {
        return rpService;
    }

    public void setRpService(RpService rpService) {
        this.rpService = rpService;
    }

    public IntrospectionService getIntrospectionService() {
        return introspectionService;
    }

    public void setIntrospectionService(IntrospectionService introspectionService) {
        this.introspectionService = introspectionService;
    }
}
