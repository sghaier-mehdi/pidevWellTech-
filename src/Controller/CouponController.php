<?php

namespace App\Controller;

use App\Entity\Coupon;
use App\Form\CouponType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Dompdf\Dompdf;
use Dompdf\Options;

#[Route('/coupon')]
final class CouponController extends AbstractController
{
    #[Route(name: 'app_coupon_index', methods: ['GET'])]
    public function index(EntityManagerInterface $entityManager): Response
    {
        $coupons = $entityManager
            ->getRepository(Coupon::class)
            ->findAll();

        return $this->render('coupon/index.html.twig', [
            'coupons' => $coupons,
        ]);
    }

    #[Route('/new', name: 'app_coupon_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $coupon = new Coupon();
        $form = $this->createForm(CouponType::class, $coupon);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($coupon);
            $entityManager->flush();

            return $this->redirectToRoute('app_coupon_show', ['id' => $coupon->getId()]);
        }

        return $this->render('coupon/new.html.twig', [
            'coupon' => $coupon,
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'app_coupon_show', methods: ['GET'])]
    public function show(Coupon $coupon): Response
    {
        return $this->render('coupon/show.html.twig', [
            'coupon' => $coupon,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_coupon_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Coupon $coupon, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(CouponType::class, $coupon);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_coupon_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('coupon/edit.html.twig', [
            'coupon' => $coupon,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_coupon_delete', methods: ['POST'])]
    public function delete(Request $request, Coupon $coupon, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$coupon->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($coupon);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_coupon_index', [], Response::HTTP_SEE_OTHER);
    }

    #[Route('/{id}/pdf', name: 'app_coupon_pdf', methods: ['GET'])]
    public function pdf(Coupon $coupon): Response
    {
        $html = $this->renderView('coupon/pdf.html.twig', [
            'coupon' => $coupon,
        ]);

        $options = new Options();
        $options->set('defaultFont', 'Arial');
        $dompdf = new Dompdf($options);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->render();

        $pdfContent = $dompdf->output();

        return new Response(
            $pdfContent,
            200,
            [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="coupon_' . $coupon->getId() . '.pdf"',
            ]
        );
    }
}
