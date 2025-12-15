package com.example.recipe.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import com.example.recipe.model.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(String name);

}
