<?php

namespace App\Controller;

use App\Entity\User;
use App\Entity\LoginAttempt;
use App\Entity\EditHistory;
use App\Repository\UserRepository;
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
            $oldName = $user->getName();
            $oldTel = $user->getTel();
            $oldEmail = $user->getEmail();
            $oldRoles = $user->getRoles();

            $user->setName($request->request->get('name'));
            $user->setTel($request->request->get('tel'));
            $user->setEmail($request->request->get('email'));
            $roles = explode(',', $request->request->get('roles'));
            $user->setRoles($roles);

            $entityManager->flush();

            $this->saveEditHistory($entityManager, $user, 'name', $oldName, $user->getName());
            $this->saveEditHistory($entityManager, $user, 'tel', $oldTel, $user->getTel());
            $this->saveEditHistory($entityManager, $user, 'email', $oldEmail, $user->getEmail());
            $this->saveEditHistory($entityManager, $user, 'roles', implode(',', $oldRoles), implode(',', $user->getRoles()));

            return $this->redirectToRoute('app_users');
        }

        return $this->render('back/user/edit.html.twig', ['user' => $user]);
    }

    private function saveEditHistory(EntityManagerInterface $entityManager, User $user, string $field, ?string $oldValue, ?string $newValue): void
    {
        if ($oldValue !== $newValue) {
            $editHistory = new EditHistory();
            $editHistory->setUser($user);
            $editHistory->setDate(new \DateTime());
            $editHistory->setField($field);
            $editHistory->setOldValue($oldValue);
            $editHistory->setNewValue($newValue);

            $entityManager->persist($editHistory);
            $entityManager->flush();
        }
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

    #[Route('/user/{id}/logs', name: 'user_logs', methods: ['GET'])]
    public function logs(User $user, EntityManagerInterface $entityManager): Response
    {
        $loginAttempts = $entityManager->getRepository(LoginAttempt::class)->findBy(['user' => $user]);
        $editHistory = $entityManager->getRepository(EditHistory::class)->findBy(['user' => $user]);

        return $this->render('user/logs.html.twig', [
            'user' => $user,
            'loginAttempts' => $loginAttempts,
            'editHistory' => $editHistory,
        ]);
    }
}
