package fastcampus.board.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import fastcampus.board.domain.UserAccount;
import lombok.Getter;

import java.time.LocalDateTime;

public record ArticleSelectDto (
        long id,
        UserAccount userAccount,
        String title,
        String content,
        String hashtagName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
){

    @QueryProjection
    public ArticleSelectDto {
    }

}
