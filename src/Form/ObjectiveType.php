<?php

namespace App\Form;

use App\Entity\Coupon;
use App\Entity\Objective;
use App\Entity\User;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ObjectiveType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nbrConsultation', IntegerType::class, [
                'label' => 'Number of Consultations',
            ])
            ->add('status', ChoiceType::class, [
                'choices' => [
                    'Pending' => 'pending',
                    'Completed' => 'completed',
                ],
                'label' => 'Status',
            ])
            ->add('coupon', EntityType::class, [
                'class' => Coupon::class,
                'choice_label' => 'name',
                'label' => 'Coupon',
            ])
            ->add('user', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'email',
                'label' => 'User',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Objective::class,
        ]);
    }
}
