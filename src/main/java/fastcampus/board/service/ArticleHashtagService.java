package fastcampus.board.service;

import fastcampus.board.domain.Article;
import fastcampus.board.domain.ArticleHashtag;
import fastcampus.board.domain.Hashtag;
import fastcampus.board.repository.ArticleHashtagRepository;
import fastcampus.board.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleHashtagService {
    private final ArticleHashtagRepository articleHashtagRepository;
    private final HashtagRepository hashtagRepository;

    @Transactional
    public void saveArticleHashtag(Article article, Hashtag hashtag) {
        articleHashtagRepository.save(ArticleHashtag.of(article, hashtag));
    }

    @Transactional
    public void saveArticleHashtags(Article article, Collection<Hashtag> hashtags) {
        for (Hashtag hashtag : hashtags) {
            if (hashtagRepository.findByHashtagName(hashtag.getHashtagName()).isEmpty()) {
                hashtagRepository.save(hashtag);
            }
            articleHashtagRepository.save(ArticleHashtag.of(article, hashtag));
        }
    }

    @Transactional
    public void deleteArticleHashtagsByArticleId(Long articleId) {
        articleHashtagRepository.deleteArticleHashtagsByArticle_Id(articleId);
    }

    public Set<Long> getHashtagIdsByArticleId(Long articleId) {
        Set<ArticleHashtag> articleHashtags = articleHashtagRepository.findByArticleId(articleId);
        return articleHashtags.stream()
                .map(ArticleHashtag::getHashtagId)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Hashtag> getHashtagsByArticleId(Long articleId) {
        Set<ArticleHashtag> articleHashtags = articleHashtagRepository.findByArticleId(articleId);
        return articleHashtags.stream()
                .map(ArticleHashtag::getHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isExistForHashtagId(Long hashtagId) {
        Set<ArticleHashtag> articleHashtags = articleHashtagRepository.findByHashtag_Id(hashtagId);
        if (articleHashtags.isEmpty()) {
            return false;
        }
        return true;
    }
 }
