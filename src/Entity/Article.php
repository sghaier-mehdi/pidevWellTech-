<?php

namespace App\Entity;

use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Vich\UploaderBundle\Mapping\Annotation as Vich;
use App\Repository\LikeDislikeRepository;

#[ORM\Entity()]
#[Vich\Uploadable]
class Article
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private $id;

    #[ORM\Column(type: "string", length: 255)]
    #[Assert\NotBlank(message: "Le titre ne peut pas être vide.")]
    private $title;

    #[ORM\Column(type: "text")]
    #[Assert\NotBlank(message: "Le contenu ne peut pas être vide.")]
    private $content;

    #[ORM\Column(type: "string", length: 255, nullable: true)]
    private $media;

    #[Vich\UploadableField(mapping: "articles", fileNameProperty: "media")]
    private $mediaFile;

    #[ORM\Column(type: "string", length: 50)]
    #[Assert\NotBlank(message: "Le type de média est requis.")]
    private $type;

    #[ORM\Column(type: "datetime")]
    private $createdAt;

    #[ORM\OneToMany(targetEntity: Comment::class, mappedBy: "article", cascade: ["remove"], orphanRemoval: true)]
    private $comments;

    #[ORM\ManyToMany(targetEntity: Tag::class, inversedBy: "articles")]
    #[ORM\JoinTable(name: "article_tags")]
    private $tags;
    #[ORM\OneToMany(targetEntity: LikeDislike::class, mappedBy: "article", cascade: ["remove"], orphanRemoval: true)]
    private Collection $likesDislikes;

    public function __construct()
    {
        $this->comments = new ArrayCollection();
        $this->tags = new ArrayCollection();
        $this->createdAt = new \DateTime();
        $this->likesDislikes = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTitle(): ?string
    {
        return $this->title;
    }

    public function setTitle(string $title): self
    {
        $this->title = $title;
        return $this;
    }

    public function getContent(): ?string
    {
        return $this->content;
    }

    public function setContent(string $content): self
    {
        $this->content = $content;
        return $this;
    }

    public function getMedia(): ?string
    {
        return $this->media;
    }

    public function setMedia(?string $media): self
    {
        $this->media = $media;
        return $this;
    }

    public function getMediaFile()
    {
        return $this->mediaFile;
    }

    public function setMediaFile($mediaFile): self
    {
        $this->mediaFile = $mediaFile;
        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(string $type): self
    {
        $this->type = $type;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeInterface
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeInterface $createdAt): self
    {
        $this->createdAt = $createdAt;
        return $this;
    }

    public function getComments(): Collection
    {
        return $this->comments;
    }

    public function addComment(Comment $comment): self
    {
        if (!$this->comments->contains($comment)) {
            $this->comments[] = $comment;
            $comment->setArticle($this);
        }
        return $this;
    }

    public function removeComment(Comment $comment): self
    {
        if ($this->comments->removeElement($comment)) {
            if ($comment->getArticle() === $this) {
                $comment->setArticle(null);
            }
        }
        return $this;
    }

    public function getTags(): Collection
    {
        return $this->tags;
    }

    public function addTag(Tag $tag): self
    {
        if (!$this->tags->contains($tag)) {
            $this->tags[] = $tag;
            $tag->getArticles()->add($this);
        }
        return $this;
    }

    public function removeTag(Tag $tag): self
    {
        if ($this->tags->removeElement($tag)) {
            $tag->getArticles()->removeElement($this);
        }
        return $this;
    }

    public function __toString(): string
    {
        return $this->title ?? 'Article';
    }
    public function getLikesCount(): int
    {
        return $this->likesDislikes->filter(function (LikeDislike $likeDislike) {
            return $likeDislike->getIsLike() === true;
        })->count();
    }
    
    public function getDislikesCount(): int
    {
        return $this->likesDislikes->filter(function (LikeDislike $likeDislike) {
            return $likeDislike->getIsLike() === false;
        })->count();
    }
    
}
