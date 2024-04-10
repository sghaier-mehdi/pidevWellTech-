<?php
namespace App\Controller;

use App\Entity\Response;
use App\Form\ResponseType;
use App\Entity\Reclamation;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response as HttpResponse;
use Symfony\Component\Routing\Annotation\Route;

class ResponseController extends AbstractController
{
    #[Route('/reclamations', name: 'reclamations_index')]
    public function reclamation(EntityManagerInterface $entityManager): HttpResponse
    {
        $reclamations = $entityManager->getRepository(Reclamation::class)->findAll();

        return $this->render('response/index.html.twig', [
            'reclamations' => $reclamations,
        ]);
    }
    #[Route('/reclamation/{id}/response', name: 'response_new')]
    public function new(Request $request, EntityManagerInterface $entityManager, Reclamation $reclamation): HttpResponse
    {
        $response = new Response();
        $form = $this->createForm(ResponseType::class, $response);

        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $response->setUser($this->getUser());
            $response->setCreatedAt(new \DateTime());
            $response->setReclamation($reclamation);

            $entityManager->persist($response);
            $entityManager->flush();

            return $this->redirectToRoute('reclamation_index');
        }

        return $this->render('response/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }
}