// UserMapper.java - CORRIGIDO
package com.example.azure_sql_demo.mapper;

import com.example.azure_sql_demo.dto.UserDTO;
import com.example.azure_sql_demo.dto.UserRegistrationRequest;
import com.example.azure_sql_demo.model.Role;
import com.example.azure_sql_demo.model.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    /**
     * Convert User entity to UserDTO
     */
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringSet")
    UserDTO toDTO(User user);

    /**
     * Convert list of User entities to list of UserDTOs
     */
    List<UserDTO> toDTOList(List<User> users);

    /**
     * Convert UserRegistrationRequest to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Encoded by service
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "isAccountNonExpired", constant = "true")
    @Mapping(target = "isAccountNonLocked", constant = "true")
    @Mapping(target = "isCredentialsNonExpired", constant = "true")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "roles", ignore = true) // Set by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    // confirmPassword não existe na entidade User, então não precisa ignorar
    User toEntity(UserRegistrationRequest request);

    /**
     * Convert UserRegistrationRequest to UserDTO (for response)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "roles", expression = "java(getDefaultUserRoles())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    UserDTO requestToDTO(UserRegistrationRequest request);

    /**
     * Convert Set<Role> to Set<String>
     */
    @Named("rolesToStringSet")
    default Set<String> rolesToStringSet(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Get default user roles for new registrations
     */
    default Set<String> getDefaultUserRoles() {
        return Set.of("USER");
    }

    /**
     * Create minimal UserDTO for listing
     */
    @Named("toMinimalDTO")
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringSet")
    UserDTO toMinimalDTO(User user);
}