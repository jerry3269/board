package fastcampus.board.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import fastcampus.board.domain.Article;
import fastcampus.board.domain.UserAccount;
import lombok.Getter;

import java.time.LocalDateTime;

public record ArticleSelectDto (
        @Getter long id,
        UserAccount userAccount,
        @Getter String title,
        String content,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
){

    @QueryProjection
    public ArticleSelectDto {
    }

    public static ArticleSelectDto of(long id, UserAccount userAccount, String title, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new ArticleSelectDto(id, userAccount, title, content, createdAt, createdBy, modifiedAt, modifiedBy);
    }
}
