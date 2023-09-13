package fastcampus.board.dto.query;

import com.querydsl.core.annotations.QueryProjection;

public record ArticleHashtagDto(
        Long articleId,
        String hashtagName
) {
    @QueryProjection
    public ArticleHashtagDto {
    }

}
