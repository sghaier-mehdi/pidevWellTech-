<?php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\CouponRepository;
use App\Repository\ObjectiveRepository;
use Endroid\QrCode\Builder\Builder;
use Endroid\QrCode\Encoding\Encoding;
use Endroid\QrCode\ErrorCorrectionLevel;
use Endroid\QrCode\Label\Label;
use Endroid\QrCode\RoundBlockSizeMode;
use Endroid\QrCode\Writer\PngWriter;
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
    #[Route('/qr-code-test', name: 'qr_code_test')]
    public function qrCodeTest(): Response
    {
        $result = Builder::create()
            ->writer(new PngWriter())
            ->data('Hello World!')
            ->encoding(new Encoding('UTF-8'))
            ->errorCorrectionLevel(ErrorCorrectionLevel::High)
            ->size(200)
            ->margin(10)
            ->roundBlockSizeMode(RoundBlockSizeMode::Margin)
            ->label(Label::create('Scan me'))
            ->build();

        return new Response($result->getString(), Response::HTTP_OK, ['Content-Type' => 'image/png']);
    }
}