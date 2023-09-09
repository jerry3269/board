package fastcampus.board.service;

import fastcampus.board.domain.Hashtag;
import fastcampus.board.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final ArticleHashtagService articleHashtagService;

    public Set<Hashtag> findHashtagsByNames(Set<String> hashtagNames) {
        return new HashSet<>(hashtagRepository.findByHashtagNameIn(hashtagNames));
    }

    @Transactional
    public Set<String> parseHashtagNames(String content) {

        if (content == null) {
            return Set.of();
        }

        Pattern pattern = Pattern.compile("#[\\w가-힣]+");
        Matcher matcher = pattern.matcher(content.strip());
        Set<String> result = new HashSet<>();

        while (matcher.find()) {
            result.add(matcher.group().replace("#", ""));
        }

        return result;
    }

    @Transactional
    public void deleteHashtagWithoutArticles(Long hashtagId) {
        if(!articleHashtagService.isExistForHashtagId(hashtagId)){
            hashtagRepository.deleteById(hashtagId);
        }
    }

    public List<String> getHashtags() {
        return hashtagRepository.findAllHashtagNames();
    }

}
