package fastcampus.board.dto;

import fastcampus.board.domain.Article;
import fastcampus.board.domain.Hashtag;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for {@link fastcampus.board.domain.Hashtag}
 */
public record HashtagWithArticlesDto(
        Long id,
        Set<ArticleDto> articleDtos,
        String hashtagName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {

    public static HashtagWithArticlesDto of(Set<ArticleDto> articleDtos, String hashtagName) {
        return new HashtagWithArticlesDto(null, articleDtos, hashtagName, null, null, null, null);
    }

    public static HashtagWithArticlesDto of(Long id, Set<ArticleDto> articleDtos, String hashtagName, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new HashtagWithArticlesDto(id, articleDtos, hashtagName, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static HashtagWithArticlesDto from(Hashtag entity, Set<Article> articles) {
        return HashtagWithArticlesDto.of(
                entity.getId(),
                articles.stream()
                        .map(article -> ArticleDto.from(article, Set.of(entity.getHashtagName())))
                        .collect(Collectors.toUnmodifiableSet()),
                entity.getHashtagName(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy()
        );
    }

    public Hashtag toEntity() {
        return Hashtag.of(hashtagName);
    }
}
