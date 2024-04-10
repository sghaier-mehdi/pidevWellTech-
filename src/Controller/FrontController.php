<?php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\CouponRepository;
use App\Repository\ObjectiveRepository;

class FrontController extends AbstractController
{
    #[Route('/coupons-objectives', name: 'coupon_objective_list')]
    public function couponObjectiveList(CouponRepository $couponRepository, ObjectiveRepository $objectiveRepository): Response
    {
        $coupons = $couponRepository->findAll();
        $objectives = $objectiveRepository->findAll();

        return $this->render('front/coupon_objective_list.html.twig', [
            'coupons' => $coupons,
            'objectives' => $objectives,
        ]);
    }
}