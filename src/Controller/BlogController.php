<?php

namespace App\Controller;

use App\Repository\ArticleRepository;
use App\Entity\Article;
use App\Entity\Comment;
use App\Entity\LikeDislike;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;
use App\Form\CommentType;

#[Route('/blog')]
class BlogController extends AbstractController
{
    #[Route('/', name: 'app_blog_index', methods: ['GET'])]
    public function index(Request $request, ArticleRepository $articleRepository): Response
    {
        $query = $request->query->get('q', ''); // Get the search query from the URL

        if ($query) {
            $articles = $articleRepository->createQueryBuilder('a')
                ->where('a.title LIKE :query OR a.content LIKE :query')
                ->setParameter('query', '%' . $query . '%')
                ->orderBy('a.createdAt', 'DESC')
                ->getQuery()
                ->getResult();
        } else {
            $articles = $articleRepository->findBy([], ['createdAt' => 'DESC']);
        }

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

    #[Route('/{id}/comment', name: 'app_blog_comment', methods: ['POST'])]
    public function addComment(Request $request, Article $article, EntityManagerInterface $entityManager): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $content = $data['content'] ?? null;

        if (!$content) {
            return $this->json(['success' => false, 'error' => 'Comment content cannot be empty'], 400);
        }

        $comment = new Comment();
        $comment->setContent($content);
        $comment->setCreatedAt(new \DateTimeImmutable());
        $comment->setArticle($article);

        // Optionally, set the user if your app has authentication
        // $comment->setUser($this->getUser());

        $entityManager->persist($comment);
        $entityManager->flush();

        return $this->json([
            'success' => true,
            'comment' => [
                'content' => $comment->getContent(),
                'createdAt' => $comment->getCreatedAt()->format('d/m/Y H:i'),
            ]
        ]);
    }
}   