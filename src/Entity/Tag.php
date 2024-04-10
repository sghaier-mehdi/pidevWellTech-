<?php

namespace App\Entity;

use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
class Tag
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private $id;

    #[ORM\Column(type: "string", length: 50, unique: true)]
    #[Assert\NotBlank(message: "Le nom du tag est requis.")]
    private $name;

    #[ORM\ManyToMany(targetEntity: Article::class, mappedBy: "tags")]
    private $articles;

    public function __construct()
    {
        $this->articles = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getName(): ?string
    {
        return $this->name;
    }

    public function setName(string $name): self
    {
        $this->name = $name;
        return $this;
    }

    public function getArticles(): Collection
    {
        return $this->articles;
    }

    public function __toString(): string
    {
        return $this->name; // Assurez-vous que 'name' existe dans votre entité
    }
}