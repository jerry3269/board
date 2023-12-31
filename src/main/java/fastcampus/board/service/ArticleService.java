package fastcampus.board.service;

import fastcampus.board.domain.Article;
import fastcampus.board.domain.Hashtag;
import fastcampus.board.domain.UserAccount;
import fastcampus.board.domain.constant.SearchType;
import fastcampus.board.dto.ArticleDto;
import fastcampus.board.dto.ArticleWithCommentsDto;
import fastcampus.board.dto.query.ArticleHashtagDto;
import fastcampus.board.dto.query.ArticleSelectDto;
import fastcampus.board.repository.ArticleHashtagRepository;
import fastcampus.board.repository.ArticleRepository;
import fastcampus.board.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleService {
    private final ArticleHashtagService articleHashtagService;
    private final HashtagService hashtagService;
    private final ArticleRepository articleRepository;
    private final UserAccountRepository userAccountRepository;
    private final ArticleHashtagRepository articleHashtagRepository;

    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return createArticleDtoWithHashtagByArticlePage(articleRepository.findAll(pageable));
        }

        return switch (searchType) {
            case TITLE ->
                    createArticleDtoWithHashtagByArticlePage(articleRepository.findByTitleContaining(searchKeyword, pageable));
            case CONTENT ->
                    createArticleDtoWithHashtagByArticlePage(articleRepository.findByContentContaining(searchKeyword, pageable));
            case ID ->
                    createArticleDtoWithHashtagByArticlePage(articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable));
            case NICKNAME ->
                    createArticleDtoWithHashtagByArticlePage(articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable));
            case HASHTAG ->
                    createArticleDtoWithHashtagByArticleSelectDtoPage(articleHashtagRepository.findByHashtagNames(Arrays.stream(searchKeyword.split(" ")).toList(), pageable));
        };
    }

    public ArticleWithCommentsDto getArticleWithComments(Long articleId) {
        return articleRepository.findById(articleId)
                .map(this::createArticleWithCommentsDtoWithHashtagByArticle)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
    }

    public ArticleDto getArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .map(article ->
                        ArticleDto.from(article, articleHashtagService.getHashtagNamesByArticleId(articleId)))
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
    }

    @Transactional
    public void saveArticle(ArticleDto dto) {
        UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
        Set<Hashtag> hashtags = renewHashtagsFormContent(dto.content());

        Article article = dto.toEntity(userAccount);
        articleRepository.save(article);
        articleHashtagService.saveArticleHashtags(article, hashtags);
    }

    @Transactional
    public void updateArticle(Long articleId, ArticleDto dto) {
        try {
            Article article = articleRepository.getReferenceById(articleId);
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());

            if (article.getUserAccount().equals(userAccount)) {
                if (dto.title() != null) {
                    article.setTitle(dto.title());
                }
                if (dto.content() != null) {
                    article.setContent(dto.content());
                }
                Set<Long> hashtagIds = articleHashtagService.getHashtagIdsByArticleId(articleId);
                articleHashtagService.deleteArticleHashtagsByArticleId(articleId);
                articleRepository.flush();

                hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);

                Set<Hashtag> hashtags = renewHashtagsFormContent(dto.content());
                articleHashtagService.saveArticleHashtags(article, hashtags);
            }
        } catch (EntityNotFoundException e) {
            log.warn("게시글 업데이트 실패! 게시글 수정에 필요한 정보를 찾을 수 없습니다. ㅡ {}", e.getLocalizedMessage());
        }
    }

    @Transactional
    public void deleteArticle(long articleId, String userId) {
        Article article = articleRepository.getReferenceById(articleId);
        if (!article.getUserAccount().getUserId().equals(userId)) {
            throw new SecurityException("접근 권한이 없는 User 입니다. 현재 userID: " + article.getUserAccount().getUserId());
        }
        Set<Long> hashtagIds = articleHashtagService.getHashtagIdsByArticleId(articleId);
        articleHashtagService.deleteArticleHashtagsByArticleId(articleId);
        articleRepository.deleteByIdAndUserAccount_UserId(articleId, userId);
        articleRepository.flush();

        hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);
    }

    public long getArticleCount() {
        return articleRepository.count();
    }

    public Page<ArticleDto> searchArticlesViaHashtag(String hashtagName, Pageable pageable) {
        if (hashtagName == null || hashtagName.isBlank()) {
            return Page.empty(pageable);
        }

        return createArticleDtoWithHashtagByArticleSelectDtoPage(articleHashtagRepository.findByHashtagNames(List.of(hashtagName), pageable));
    }

    private Set<Hashtag> renewHashtagsFormContent(String content) {
        Set<String> hashtagNamesInContent = hashtagService.parseHashtagNames(content);
        Set<Hashtag> hashtags = hashtagService.findHashtagsByNames(hashtagNamesInContent);
        Set<String> existingHashtagNames = hashtags.stream()
                .map(Hashtag::getHashtagName)
                .collect(toUnmodifiableSet());

        hashtagNamesInContent.forEach(newHashtagNames -> {
            if (!existingHashtagNames.contains(newHashtagNames)) {
                hashtags.add(Hashtag.of(newHashtagNames));
            }
        });

        return hashtags;
    }

    private ArticleWithCommentsDto createArticleWithCommentsDtoWithHashtagByArticle(Article article) {
        return ArticleWithCommentsDto.from(article, articleHashtagService.getHashtagNamesByArticleId(article.getId()));
    }
    
    private Page<ArticleDto> createArticleDtoWithHashtagByArticlePage(Page<Article> articlePage) {
        Map<Long, Set<String>> articleHashtagsMap = getArticleHashtagMapFromArticlePage(articlePage.stream().map(Article::getId));

        List<ArticleDto> collect = articlePage.stream()
                .map(article -> ArticleDto.from(article, articleHashtagsMap.get(article.getId())))
                .toList();

        return new PageImpl<>(collect, articlePage.getPageable(), articlePage.getTotalElements());
    }

    private Page<ArticleDto> createArticleDtoWithHashtagByArticleSelectDtoPage(Page<ArticleSelectDto> selectDtoPage) {
        Map<Long, Set<String>> articleHashtagsMap = getArticleHashtagMapFromArticlePage(selectDtoPage.stream().map(ArticleSelectDto::id));

        List<ArticleDto> collect = selectDtoPage.stream()
                .map(selectDto -> ArticleDto.from(selectDto, articleHashtagsMap.get(selectDto.id())))
                .toList();

        return new PageImpl<>(collect, selectDtoPage.getPageable(), selectDtoPage.getTotalElements());
    }

    private Map<Long, Set<String>> getArticleHashtagMapFromArticlePage(Stream<Long> articlePage) {
        Set<Long> articledIds = articlePage.collect(Collectors.toUnmodifiableSet());

        Map<Long, Set<String>> articleHashtagsMap = articledIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> new HashSet<>()));

        Set<ArticleHashtagDto> articleHashtagDtos =
                articleHashtagRepository.findDtoByArticleIds(articledIds);

        articleHashtagDtos
                .forEach(articleHashtagDto ->
                        articleHashtagsMap.get(articleHashtagDto.articleId()).add(articleHashtagDto.hashtagName()));

        return articleHashtagsMap;
    }

}

