package fastcampus.board.service;

import fastcampus.board.domain.UserAccount;
import fastcampus.board.dto.UserAccountDto;
import fastcampus.board.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public Optional<UserAccountDto> searchUser(String username) {
        return userAccountRepository.findById(username)
                .map(UserAccountDto::from);
    }

    @Transactional
    public UserAccountDto saveUser(String username, String userPassword, String email, String nickname, String memo) {
        return UserAccountDto.from(userAccountRepository.save(
                UserAccount.of(
                        username,
                        userPassword,
                        email,
                        nickname,
                        memo,
                        username
                )));
    }
}
