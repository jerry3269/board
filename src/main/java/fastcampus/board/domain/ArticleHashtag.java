package fastcampus.board.domain;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"articleId", "hashtagId"}))
@ToString(callSuper = true)
@Entity
public class ArticleHashtag extends AuditingFields{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleId")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtagId")
    private Hashtag hashtag;

    private ArticleHashtag(Article article, Hashtag hashtag) {
        this.article = article;
        this.hashtag = hashtag;
    }

    public static ArticleHashtag of(Article article, Hashtag hashtag) {
        return new ArticleHashtag(article, hashtag);
    }

    public Long getHashtagId() {
        return this.getHashtag().getId();
    }

    public Long getArticleId() {
        return this.getArticle().getId();
    }
}

