package fastcampus.board.repository.querydsl;

import fastcampus.board.domain.Article;
import fastcampus.board.domain.QArticle;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;

public class ArticleRepositoryCustomImpl extends QuerydslRepositorySupport implements ArticleRepositoryCustom {

    public ArticleRepositoryCustomImpl() {
        super(Article.class);
    }

    @Override
    public List<String> findAllDistinctHashtags() {
        QArticle article = QArticle.article;

        return from(article)
                .select(article.hashtag)
                .distinct()
                .where(article.hashtag.isNotNull())
                .fetch();
    }

}
