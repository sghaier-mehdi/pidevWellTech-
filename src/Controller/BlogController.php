<?php

namespace App\Controller;

use App\Repository\ArticleRepository;
use App\Entity\Article;
use Symfony\Component\HttpFoundation\Request;
use App\Entity\Comment;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Form\CommentType;

class BlogController extends AbstractController
{
    #[Route('/blog', name: 'app_blog_index', methods: ['GET'])]
    public function index(ArticleRepository $articleRepo): Response
    {
        $articles = $articleRepo->findBy([], ['createdAt' => 'DESC']);

        return $this->render('blog/index.html.twig', [
            'articles' => $articles,
        ]);
    }

    #[Route('/{id<\d+>}', name: 'app_blog_show', methods: ['GET', 'POST'])]
    public function show(Article $article, Request $request, EntityManagerInterface $entityManager): Response
    {
        // Formulaire de commentaire
        $comment = new Comment();
        $commentForm = $this->createForm(CommentType::class, $comment);
        $commentForm->handleRequest($request);

        if ($commentForm->isSubmitted() && $commentForm->isValid()) {
            $comment->setArticle($article);
            $entityManager->persist($comment);
            $entityManager->flush();

            $this->addFlash('success', 'Commentaire ajouté avec succès !');
            return $this->redirectToRoute('app_blog_show', ['id' => $article->getId()]);
        }

        return $this->render('blog/show.html.twig', [
            'article' => $article,
            'commentForm' => $commentForm->createView(),
        ]);
    }

    #[Route('/home', name: 'app_home', methods: ['GET'])]
    public function home(): Response
    {
        return $this->render('blog/home.html.twig');
    }
}