<?php

namespace App\Controller;

use App\Entity\Objective;
use App\Form\ObjectiveType;
use App\Repository\ObjectiveRepository;
use App\Repository\UserRepository;

use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\JsonResponse;

#[Route('/objective')]
class ObjectiveController extends AbstractController
{
    #[Route('/', name: 'objective_index', methods: ['GET'])]
    public function index(ObjectiveRepository $objectiveRepository, UserRepository $userRepository): Response
    {
        // Fetch objectives from the database
        $objectives = $objectiveRepository->findAll();

        // Prepare data for the chart
        $chartData = [];
        $users = $userRepository->findAll();
        foreach ($users as $user) {
            $objectiveCount = count($objectiveRepository->findBy(['user' => $user]));
            $chartData[] = [
                'username' => $user->getName(),
                'objectiveCount' => $objectiveCount,
            ];
        }

        return $this->render('objective/index.html.twig', [
            'objectives' => $objectives,
            'chartData' => json_encode($chartData),
        ]);
    }

    #[Route('/new', name: 'objective_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $objective = new Objective();
        $form = $this->createForm(ObjectiveType::class, $objective);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($objective);
            $entityManager->flush();

            return $this->redirectToRoute('objective_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('objective/new.html.twig', [
            'objective' => $objective,
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'objective_show', methods: ['GET'])]
    public function show(Objective $objective): Response
    {
        return $this->render('objective/show.html.twig', [
            'objective' => $objective,
        ]);
    }

    #[Route('/{id}/edit', name: 'objective_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Objective $objective, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(ObjectiveType::class, $objective);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('objective_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('objective/edit.html.twig', [
            'objective' => $objective,
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'objective_delete', methods: ['POST'])]
    public function delete(Request $request, Objective $objective, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$objective->getId(), $request->request->get('_token'))) {
            $entityManager->remove($objective);
            $entityManager->flush();
        }

        return $this->redirectToRoute('objective_index', [], Response::HTTP_SEE_OTHER);
    }
}
