package com.example.projectwork.repository

import com.example.projectwork.data.User
import com.example.projectwork.data.UserDao

class UserRepository(private val userDao: UserDao) {
    
    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }
    
    suspend fun register(username: String, password: String): Long? {
        // Check if username already exists
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser != null) {
            return null
        }
        
        // Create new user
        return userDao.insert(User(username = username, password = password))
    }
} 