<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\LikeDislike;
use App\Repository\LikeDislikeRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Core\Exception\AccessDeniedException;

#[Route('/blog')]
class LikeDislikeController extends AbstractController
{
    #[Route('/{id}/like', name: 'blog_like', methods: ['POST'])]
    public function like(Article $article, EntityManagerInterface $entityManager, LikeDislikeRepository $likeDislikeRepository): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'User not authenticated'], JsonResponse::HTTP_UNAUTHORIZED);
        }

        return $this->handleLikeDislike($article, true, $entityManager, $likeDislikeRepository, $user);
    }

    #[Route('/{id}/dislike', name: 'blog_dislike', methods: ['POST'])]
    public function dislike(Article $article, EntityManagerInterface $entityManager, LikeDislikeRepository $likeDislikeRepository): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'User not authenticated'], JsonResponse::HTTP_UNAUTHORIZED);
        }

        return $this->handleLikeDislike($article, false, $entityManager, $likeDislikeRepository, $user);
    }

    private function handleLikeDislike(Article $article, bool $isLike, EntityManagerInterface $entityManager, LikeDislikeRepository $likeDislikeRepository, $user): JsonResponse
    {
        // Check if the user has already reacted to the article
        $existingReaction = $likeDislikeRepository->findOneBy([
            'article' => $article,
            'user' => $user
        ]);

        if ($existingReaction) {
            // If the user clicks the same reaction again, remove it (toggle off)
            if ($existingReaction->getIsLike() === $isLike) {
                $entityManager->remove($existingReaction);
            } else {
                // Otherwise, update the reaction (switch between like/dislike)
                $existingReaction->setIsLike($isLike);
                $entityManager->persist($existingReaction);
            }
        } else {
            // User is reacting for the first time
            $newReaction = new LikeDislike();
            $newReaction->setArticle($article);
            $newReaction->setUser($user);
            $newReaction->setIsLike($isLike);
            $entityManager->persist($newReaction);
        }

        $entityManager->flush();

        // Get updated counts after modification
        $likes = $likeDislikeRepository->count(['article' => $article, 'isLike' => true]);
        $dislikes = $likeDislikeRepository->count(['article' => $article, 'isLike' => false]);

        return new JsonResponse([
            'likes' => $likes,
            'dislikes' => $dislikes
        ]);
    }
}