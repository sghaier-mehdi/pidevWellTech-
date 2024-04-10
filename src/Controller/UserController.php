<?php

namespace App\Controller;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class UserController extends AbstractController
{
    #[Route('/user/edit/{id}', name: 'user_edit')]
    public function edit(int $id, Request $request, EntityManagerInterface $entityManager): Response
    {
        $user = $entityManager->getRepository(User::class)->find($id);

        if (!$user) {
            throw $this->createNotFoundException('No user found for id ' . $id);
        }

        if ($request->isMethod('POST')) {
            $user->setEmail($request->request->get('email'));
            $roles = explode(',', $request->request->get('roles'));
            $user->setRoles($roles);

            $entityManager->flush();

            return $this->redirectToRoute('app_users');
        }

        return $this->render('back/user/edit.html.twig', ['user' => $user]);
    }

    #[Route('/user/delete/{id}', name: 'user_delete')]
    public function delete(int $id, EntityManagerInterface $entityManager): Response
    {
        $user = $entityManager->getRepository(User::class)->find($id);

        if (!$user) {
            throw $this->createNotFoundException('No user found for id ' . $id);
        }

        $entityManager->remove($user);
        $entityManager->flush();

        return $this->redirectToRoute('app_users');
    }
    #[Route('/users', name: 'app_users')]
    public function index(EntityManagerInterface $entityManager): Response
    {
        // Fetch all users from the database
        $users = $entityManager->getRepository(User::class)->findAll();

        return $this->render('/back/user/users.html.twig', ['users' => $users]);
    }
}
