package fastcampus.board.dto.request;

import fastcampus.board.domain.UserAccount;
import fastcampus.board.dto.ArticleCommentDto;
import fastcampus.board.dto.UserAccountDto;

/**
 * DTO for {@link fastcampus.board.domain.ArticleComment}
 */
public record ArticleCommentRequest(
        Long articleId,
        String content
) {

    public static ArticleCommentRequest of(Long articleId, String content) {
        return new ArticleCommentRequest(articleId, content);
    }

    public ArticleCommentDto toDto(UserAccountDto userAccountDto) {
        return ArticleCommentDto.of(
                articleId,
                userAccountDto,
                content
        );
    }

}
