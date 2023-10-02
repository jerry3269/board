package fastcampus.board.domain.projection;

import fastcampus.board.domain.Article;
import fastcampus.board.domain.ArticleHashtag;
import fastcampus.board.domain.Hashtag;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "withArticleAndHashtag", types = ArticleHashtag.class)
public interface ArticleHashtagProjection {
    Long getId();
    Article getArticle();
    Hashtag getHashtag();
}
