<?php

namespace App\Form;

use App\Entity\User;
use App\Entity\Consultation;
use App\Repository\UserRepository;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType; 
use Symfony\Component\Form\Extension\Core\Type\TextareaType;


class ConsultationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('psychiatrist', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'email',
                'query_builder' => function (UserRepository $userRepository) {
                    return $userRepository->getUsersByRoleQueryBuilder('ROLE_PSYCHIATRE');
                },
            ])
            ->add('consultationDate', DateTimeType::class, [
                'label' => 'Consultation Date and Time',
                'widget' => 'single_text',
                'html5' => true, 
                'required' => true,
                'attr' => [
                    'class' => 'form-control', 
                    'min' => (new \DateTime())->format('Y-m-d\TH:i'), 
                ],
            ])            
            ->add('message', TextareaType::class, [
                'label' => 'Message to the Psychiatrist',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Write your message here...'
                ]
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Consultation::class,
        ]);
    }
}
