package fastcampus.board.domain;

import lombok.*;
import org.springframework.beans.Mergeable;

import javax.persistence.*;

import static javax.persistence.CascadeType.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"articleId", "hashtagId"}))
@ToString(callSuper = true)
@Entity
public class ArticleHashtag extends AuditingFields{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO: Hal Explorer(api)를 사용하기 위해 fetch방식을 EAGER로 변경. 추후 api 제공방식을 변경하여 지연로딩으로 변경하자.
    @ManyToOne(fetch = FetchType.EAGER, cascade = {PERSIST, MERGE})
    @JoinColumn(name = "articleId")
    private Article article;

    //TODO: Hal Explorer(api)를 사용하기 위해 fetch방식을 EAGER로 변경. 추후 api 제공방식을 변경하여 지연로딩으로 변경하자.
    @ManyToOne(fetch = FetchType.EAGER,  cascade = {PERSIST, MERGE})
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
    public String getHashtagName() { return this.getHashtag().getHashtagName();}

}

