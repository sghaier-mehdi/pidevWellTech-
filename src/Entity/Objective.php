<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: "objectives")]
class Objective
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Coupon::class)]
    #[ORM\JoinColumn(nullable: false, onDelete: "CASCADE")]
    private Coupon $coupon;

    #[ORM\Column(type: "integer")]
    private int $nbrConsultation;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false, onDelete: "CASCADE")]
    private User $user;

    #[ORM\Column(type: "string", length: 10, options: ["default" => "pending"])]
        private string $status = "pending";

    // Getters and setters...

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getCoupon(): Coupon
    {
        return $this->coupon;
    }

    public function setCoupon(Coupon $coupon): self
    {
        $this->coupon = $coupon;
        return $this;
    }

    public function getNbrConsultation(): int
    {
        return $this->nbrConsultation;
    }

    public function setNbrConsultation(int $nbrConsultation): self
    {
        $this->nbrConsultation = $nbrConsultation;
        return $this;
    }

    public function getUser(): User
    {
        return $this->user;
    }

    public function setUser(User $user): self
    {
        $this->user = $user;
        return $this;
    }

    public function getStatus(): string
    {
        return $this->status;
    }

    public function setStatus(string $status): self
    {
        $this->status = $status;
        return $this;
    }
}
