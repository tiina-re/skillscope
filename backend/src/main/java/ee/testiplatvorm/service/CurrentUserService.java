package ee.testiplatvorm.service;

import ee.testiplatvorm.infrastructure.exception.ForbiddenException;
import ee.testiplatvorm.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.testiplatvorm.persistence.user.User;
import ee.testiplatvorm.persistence.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static ee.testiplatvorm.Error.NO_PERMISSION_TO_CREATE_QUESTIONS;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final HttpServletRequest request;
    private final UserRepository userRepository;

    public Integer getUserId() {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return (Integer) session.getAttribute("userId");
    }

    // Küsimusi (ka AI-ga) tohivad luua ainult ADMIN ja HALDUR
    public Integer getValidQuestionAuthorId() {
        Integer userId = getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
        String roleName = user.getRole().getName();

        boolean isAllowedToCreateQuestions = roleName.equals("ADMIN") || roleName.equals("HALDUR");
        if (!isAllowedToCreateQuestions) {
            throw new ForbiddenException(NO_PERMISSION_TO_CREATE_QUESTIONS.getMessage(), NO_PERMISSION_TO_CREATE_QUESTIONS.name());
        }
        return userId;
    }
}
