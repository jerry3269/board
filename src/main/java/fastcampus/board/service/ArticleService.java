package fastcampus.board.service;

import fastcampus.board.domain.Article;
import fastcampus.board.domain.Hashtag;
import fastcampus.board.domain.UserAccount;
import fastcampus.board.domain.constant.SearchType;
import fastcampus.board.dto.ArticleDto;
import fastcampus.board.dto.ArticleWithCommentsDto;
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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleService {
    private final ArticleHashtagRepository articleHashtagRepository;
    private final ArticleHashtagService articleHashtagService;
    private final HashtagService hashtagService;
    private final ArticleRepository articleRepository;
    private final UserAccountRepository userAccountRepository;

    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return articleRepository.findAll(pageable).map(this::createArticleDtoWithHashtagByArticleId);
        }

        return switch (searchType) {
            case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(this::createArticleDtoWithHashtagByArticleId);
            case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(this::createArticleDtoWithHashtagByArticleId);
            case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(this::createArticleDtoWithHashtagByArticleId);
            case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(this::createArticleDtoWithHashtagByArticleId);
            case HASHTAG -> createArticleDtoWithHashtagByArticleSelectDto(
                    articleHashtagRepository.findByHashtagNames(Arrays.stream(searchKeyword.split(" ")).toList(), pageable));
        };
    }

    public ArticleWithCommentsDto getArticleWithComments(Long articleId) {
        return articleRepository.findById(articleId)
                .map(this::createArticleWithCommentsDtoWithHashtagByArticleId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
    }

    public ArticleDto getArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .map(this::createArticleDtoWithHashtagByArticleId)
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

            if(article.getUserAccount().equals(userAccount)){
                if(dto.title() != null) {article.setTitle(dto.title());}
                if(dto.content() != null) {article.setContent(dto.content());}

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

        return createArticleDtoWithHashtagByArticleSelectDto(
                articleHashtagRepository.findByHashtagNames(List.of(hashtagName), pageable));

    }


    private Set<Hashtag> renewHashtagsFormContent(String content) {
        Set<String> hashtagNamesInContent = hashtagService.parseHashtagNames(content);
        Set<Hashtag> hashtags = hashtagService.findHashtagsByNames(hashtagNamesInContent);
        Set<String> existingHashtagNames = hashtags.stream()
                .map(Hashtag::getHashtagName)
                .collect(toUnmodifiableSet());

        hashtagNamesInContent.forEach(newHashtagNames -> {
            if(!existingHashtagNames.contains(newHashtagNames)){
                hashtags.add(Hashtag.of(newHashtagNames));
            }
        });

        return hashtags;
    }

    private ArticleDto createArticleDtoWithHashtagByArticleId(Article article) {

        return ArticleDto.from(article, articleHashtagService.getHashtagsByArticleId(article.getId()));
    }

    private ArticleWithCommentsDto createArticleWithCommentsDtoWithHashtagByArticleId(Article article) {
        return ArticleWithCommentsDto.from(article, articleHashtagService.getHashtagsByArticleId(article.getId()));
    }

    private Page<ArticleDto> createArticleDtoWithHashtagByArticleSelectDto(Page<ArticleSelectDto> selectDtos) {
        Map<Long, Set<String>> hashtagMap = createHashtagMap(selectDtos);

        List<ArticleDto> collect = selectDtos.stream().map(selectDto -> ArticleDto.from(selectDto, hashtagMap.get(selectDto.id())))
                .collect(toList());

        return new PageImpl<>(collect, selectDtos.getPageable(), selectDtos.getTotalElements());
    }

    private static Map<Long, Set<String>> createHashtagMap(Page<ArticleSelectDto> selectDtos) {
        return selectDtos.stream()
                .collect(Collectors.groupingBy(ArticleSelectDto::id,
                        Collectors.mapping(ArticleSelectDto::hashtagName, Collectors.toSet())));
    }
}
