package com.akshay.service;

import com.akshay.dto.resource.ResourceRequest;
import com.akshay.dto.resource.ResourceResponse;

import java.util.List;

public interface ResourceService {
    ResourceResponse createResource(ResourceRequest request);
    ResourceResponse getResourceById(Long id);
    List<ResourceResponse> getAllResources(Boolean availableOnly);
    ResourceResponse updateResource(Long id, ResourceRequest request);
    void deleteResource(Long id);
}
