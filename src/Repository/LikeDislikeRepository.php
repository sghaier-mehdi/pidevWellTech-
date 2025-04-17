<?php

namespace App\Repository;

use App\Entity\Article;
use App\Entity\LikeDislike;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class LikeDislikeRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, LikeDislike::class);
    }

    // Count likes for a specific article
    public function countLikes(Article $article): int
    {
        return $this->count(['article' => $article, 'isLike' => true]);
    }

    // Count dislikes for a specific article
    public function countDislikes(Article $article): int
    {
        return $this->count(['article' => $article, 'isLike' => false]);
    }

    // Get total likes for all articles
    public function getTotalLikes(): int
    {
        return $this->createQueryBuilder('l')
            ->select('COUNT(l.id)')
            ->where('l.isLike = :isLike')
            ->setParameter('isLike', true)
            ->getQuery()
            ->getSingleScalarResult();
    }

    // Get total dislikes for all articles
    public function getTotalDislikes(): int
    {
        return $this->createQueryBuilder('l')
            ->select('COUNT(l.id)')
            ->where('l.isLike = :isLike')
            ->setParameter('isLike', false)
            ->getQuery()
            ->getSingleScalarResult();
    }

    // Get like/dislike counts for all articles (grouped)
    public function getLikeDislikeStats(): array
    {
        return $this->createQueryBuilder('l')
            ->select('a.id AS article_id, a.title AS article_title, 
                      SUM(CASE WHEN l.isLike = 1 THEN 1 ELSE 0 END) AS total_likes, 
                      SUM(CASE WHEN l.isLike = 0 THEN 1 ELSE 0 END) AS total_dislikes')
            ->join('l.article', 'a')
            ->groupBy('a.id')
            ->getQuery()
            ->getResult();
    }
}
