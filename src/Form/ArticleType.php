<?php

namespace App\Form;

use App\Entity\Article;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use App\Entity\Tag;

class ArticleType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
        ->add('title', TextType::class, [
            'label' => 'Titre',
            'attr' => ['maxlength' => 255, 'minlength' => 5, 'required' => true],
        ])
        ->add('content', TextareaType::class, [
            'label' => 'Contenu',
            'attr' => ['minlength' => 10, 'required' => true],
        ])
        ->add('media', FileType::class, [
            'label' => 'Média (image, vidéo, audio)',
            'required' => false,
            'mapped' => false, // Indique qu'on ne le mappe pas directement à l'entité
        ])
        ->add('type', ChoiceType::class, [
            'label' => 'Type de média',
            'choices' => [
                'Image' => 'image',
                'Vidéo' => 'video',
                'Audio' => 'audio',
            ],
            'placeholder' => 'Sélectionnez un type',
        ])
        ->add('tags', EntityType::class, [
            'class' => Tag::class,
            'multiple' => true,
            'expanded' => true,
        ])
        ->add('submit', SubmitType::class, [
            'label' => 'Enregistrer',
            'attr' => ['class' => 'btn btn-primary'],
        ]);
        
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Article::class,
        ]);
    }
}
