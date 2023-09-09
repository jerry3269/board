package fastcampus.board.dto;

import fastcampus.board.domain.Article;
import fastcampus.board.domain.Hashtag;
import fastcampus.board.domain.UserAccount;
import fastcampus.board.dto.query.ArticleSelectDto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO for the {@link Article} entity
 */
public record ArticleDto(
        Long id,
        UserAccountDto userAccountDto,
        String title,
        String content,
        Set<HashtagDto> hashtagDtos,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public static ArticleDto of(UserAccountDto userAccountDto, String title, String content, Set<HashtagDto> hashtagDtos) {
        return new ArticleDto(null, userAccountDto, title, content, hashtagDtos, null, null, null, null);
    }
    public static ArticleDto of(Long id, UserAccountDto userAccountDto, String title, String content, Set<HashtagDto> hashtagDtos, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new ArticleDto(id, userAccountDto, title, content, hashtagDtos, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static ArticleDto from(Article entity, Set<Hashtag> hashtags) {
        return ArticleDto.of(
                entity.getId(),
                UserAccountDto.from(entity.getUserAccount()),
                entity.getTitle(),
                entity.getContent(),
                hashtags.stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet()),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy()
        );
    }

    public static ArticleDto from(ArticleSelectDto selectDto, Set<Hashtag> hashtags) {
        return ArticleDto.of(
                selectDto.id(),
                UserAccountDto.from(selectDto.userAccount()),
                selectDto.title(),
                selectDto.content(),
                hashtags.stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet()),
                selectDto.createdAt(),
                selectDto.createdBy(),
                selectDto.modifiedAt(),
                selectDto.modifiedBy()
        );
    }
    public Article toEntity(UserAccount userAccount) {
        return Article.of(
                userAccount,
                title,
                content
        );
    }
}