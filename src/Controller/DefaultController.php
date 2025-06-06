<?php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class DefaultController extends AbstractController
{
    #[Route('/', name: 'index')]
    public function index(): Response
    {
        return $this->render('back/index.html.twig');
    }
    #[Route('/front', name: 'index_front')]
    public function index_front(): Response
    {
        return $this->render('front/index.html.twig');
    }
}
