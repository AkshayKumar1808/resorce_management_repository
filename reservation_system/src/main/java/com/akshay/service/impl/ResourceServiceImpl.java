package com.akshay.service.impl;

import com.akshay.dto.resource.ResourceRequest;
import com.akshay.dto.resource.ResourceResponse;
import com.akshay.entity.Resource;
import com.akshay.exception.ResourceNotFoundException;
import com.akshay.repository.ResourceRepository;
import com.akshay.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        Resource resource = Resource.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();

        Resource savedResource = resourceRepository.save(resource);
        return mapToResourceResponse(savedResource);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return mapToResourceResponse(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources(Boolean availableOnly) {
        List<Resource> resources;
        if (Boolean.TRUE.equals(availableOnly)) {
            resources = resourceRepository.findByAvailable(true);
        } else {
            resources = resourceRepository.findAll();
        }

        return resources.stream()
                .map(this::mapToResourceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        resource.setName(request.getName().trim());
        resource.setDescription(request.getDescription());
        resource.setBasePrice(request.getBasePrice());
        if (request.getAvailable() != null) {
            resource.setAvailable(request.getAvailable());
        }

        Resource updatedResource = resourceRepository.save(resource);
        return mapToResourceResponse(updatedResource);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
    }

    public ResourceResponse mapToResourceResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .basePrice(resource.getBasePrice())
                .available(resource.getAvailable())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
