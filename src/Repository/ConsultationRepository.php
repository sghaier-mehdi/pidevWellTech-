<?php

namespace App\Repository;

use App\Entity\Consultation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;
use App\Entity\User;    

/**
 * @extends ServiceEntityRepository<Consultation>
 *
 * @method Consultation|null find($id, $lockMode = null, $lockVersion = null)
 * @method Consultation|null findOneBy(array $criteria, array $orderBy = null)
 * @method Consultation[]    findAll()
 * @method Consultation[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class ConsultationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Consultation::class);
    }

//    /**
//     * @return Consultation[] Returns an array of Consultation objects
//     */
//    public function findByExampleField($value): array
//    {
//        return $this->createQueryBuilder('c')
//            ->andWhere('c.exampleField = :val')
//            ->setParameter('val', $value)
//            ->orderBy('c.id', 'ASC')
//            ->setMaxResults(10)
//            ->getQuery()
//            ->getResult()
//        ;
//    }

//    public function findOneBySomeField($value): ?Consultation
//    {
//        return $this->createQueryBuilder('c')
//            ->andWhere('c.exampleField = :val')
//            ->setParameter('val', $value)
//            ->getQuery()
//            ->getOneOrNullResult()
//        ;
//    }
public function getPendingConsultationsForPsychiatrist(User $psychiatrist)
{
    return $this->createQueryBuilder('c')
        ->where('c.psychiatrist = :psychiatrist')
        ->andWhere('c.statut = :statut')
        ->setParameter('psychiatrist', $psychiatrist)
        ->setParameter('statut', 'en attente')
        ->orderBy('c.createdAt', 'DESC') 
        ->getQuery()
        ->getResult();
}

public function getConsultationsByStatus(User $psychiatrist, string $status)
{
    return $this->createQueryBuilder('c')
        ->where('c.psychiatrist = :psychiatrist')
        ->andWhere('c.statut = :statut')
        ->setParameter('psychiatrist', $psychiatrist)
        ->setParameter('statut', $status)
        ->orderBy('c.createdAt', 'DESC')
        ->getQuery()
        ->getResult();
}

}