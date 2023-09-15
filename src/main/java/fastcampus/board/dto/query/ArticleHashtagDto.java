package fastcampus.board.dto.query;

import com.querydsl.core.annotations.QueryProjection;

public record ArticleHashtagDto(
        long articleId,
        String hashtagName
) {
    @QueryProjection
    public ArticleHashtagDto {
    }

    public static ArticleHashtagDto of(long articleId, String hashtagName) {
        return new ArticleHashtagDto(articleId, hashtagName);
    }
}
