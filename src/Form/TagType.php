<?php

// src/Form/TagType.php

namespace App\Form;

use App\Entity\Tag;
use App\Entity\Article; // Make sure to import the Article entity
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class TagType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('name', TextType::class, [
                'label' => 'Tag Name',
                'attr' => [
                    'placeholder' => 'Enter a tag name',
                    'class' => 'form-control',
                ],
            ])
            ->add('articles', EntityType::class, [
                'class' => Article::class, // Replace with your Article entity
                'choice_label' => 'title', // Display article titles in the dropdown
                'multiple' => true, // Allow multiple articles to be selected
                'expanded' => false, // Render as a dropdown (not checkboxes)
                'attr' => [
                    'class' => 'form-control', // Add Bootstrap class
                ],
                'label' => 'Related Articles',
                'required' => false, // Make this field optional
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Tag::class,
        ]);
    }
}