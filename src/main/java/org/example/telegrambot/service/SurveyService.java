package org.example.telegrambot.service;

import org.example.telegrambot.entity.SurveyResponse;
import org.example.telegrambot.entity.User;
import org.example.telegrambot.repository.SurveyResponseRepository;
import org.example.telegrambot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class SurveyService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User createUser(Long userId, String firstName, String lastName, String username) {
        User user = new User(userId, firstName, lastName, username);
        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Transactional
    public SurveyResponse saveSurveyResponse(User user, String name, String email, Integer rating) {
        SurveyResponse response = new SurveyResponse(user, name, email, rating);
        SurveyResponse savedResponse = surveyResponseRepository.save(response);

        // Reset user state after completing survey
        user.setCurrentState(null);
        userRepository.save(user);

        return savedResponse;
    }

    public void resetUserState(User user) {
        user.setCurrentState(null);
        userRepository.save(user);
    }
}
