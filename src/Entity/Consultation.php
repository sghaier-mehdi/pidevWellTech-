<?php
namespace App\Entity;

use App\Repository\ConsultationRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\DBAL\Types\Types;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: ConsultationRepository::class)]
#[ORM\HasLifecycleCallbacks]
class Consultation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class, inversedBy: 'consultations')]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $patient = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $psychiatrist = null;


    #[ORM\Column(type: 'datetime')]
    private ?\DateTimeInterface $createdAt = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    #[Assert\NotBlank(message: 'sélectionner une date de consultation.')]
    #[Assert\GreaterThanOrEqual("today", message: "La date de consultation doit être aujourd'hui ou une date future.")]
    private ?\DateTimeInterface $consultationDate = null;
    

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\NotBlank(message: 'Veuillez entrer un message.')]
    #[Assert\Length(min: 10, minMessage: 'Le message doit comporter au moins 10 caractères.')]
    private ?string $message = null;
    

    #[ORM\Column(type: 'string', length: 20)]
    #[Assert\Choice(choices: ['en attente', 'accepté', 'refusé'], message: 'statut Invalide.')]
    private string $statut = 'en attente';
    

    public function getStatut(): string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getPatient(): ?User
    {
        return $this->patient;
    }

    public function setPatient(?User $patient): self
    {
        $this->patient = $patient;

        return $this;
    }

public function getPsychiatrist(): ?User
{
    return $this->psychiatrist;
}

public function setPsychiatrist(?User $psychiatrist): self
{
    $this->psychiatrist = $psychiatrist;

    return $this;
}

public function setCreatedAt(?\DateTimeInterface $createdAt): self
{
    $this->createdAt = $createdAt;
    return $this;
}


public function getCreatedAt(): ?\DateTimeInterface
{
    return $this->createdAt;
}

public function getConsultationDate(): ?\DateTimeInterface
{
    return $this->consultationDate;
}

public function setConsultationDate(\DateTimeInterface $consultationDate): self
{
    $this->consultationDate = $consultationDate;
    return $this;
}
public function getId(): ?int
{
    return $this->id;
}

public function getMessage(): ?string
{
    return $this->message;
}

public function setMessage(?string $message): self
{
    $this->message = $message;
    return $this;
}

}