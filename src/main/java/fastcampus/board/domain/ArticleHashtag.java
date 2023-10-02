package fastcampus.board.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"articleId", "hashtagId"}))
@ToString(callSuper = true)
@Entity
public class ArticleHashtag extends AuditingFields{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {PERSIST, MERGE})
    @JoinColumn(name = "articleId")
    @ToString.Exclude
    private Article article;

    @ManyToOne(cascade = {PERSIST, MERGE})
    @JoinColumn(name = "hashtagId")
    @ToString.Exclude
    private Hashtag hashtag;

    public void setHashtag(Hashtag hashtag) {
        this.hashtag = hashtag;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

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
    public String getHashtagName() { return this.getHashtag().getHashtagName();}

}

