package com.onlinebookstore.user_service.repository;

import com.onlinebookstore.user_service.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}