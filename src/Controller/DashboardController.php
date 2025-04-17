<?php
namespace App\Controller;

use App\Repository\LikeDislikeRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin')]
class DashboardController extends AbstractController
{
    #[Route('/dashboard', name: 'admin_dashboard')]
    public function index(LikeDislikeRepository $likeDislikeRepository): Response
    {   
        $totalLikes = $likeDislikeRepository->getTotalLikes();
        $totalDislikes = $likeDislikeRepository->getTotalDislikes();
        $articleStats = $likeDislikeRepository->getLikeDislikeStats();

        return $this->render('admin/dashboard.html.twig', [
            'totalLikes' => $totalLikes,
            'totalDislikes' => $totalDislikes,
            'articleStats' => $articleStats
        ]);
    }
}
