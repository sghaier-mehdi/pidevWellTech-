<?php

namespace App\Form;

use App\Entity\Orders;
use App\Entity\Products;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class OrdersType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('user_id', IntegerType::class, [
                'required' => true,
            ])
            ->add('quantity', IntegerType::class, [
                'required' => true,
            ])
            ->add('order_date', DateTimeType::class, [
                'widget' => 'single_text',
                'required' => true,
            ])
            ->add('total_price', NumberType::class, [
                'scale' => 2,
                'html5' => true,
                'attr' => ['step' => '0.01'],
                'required' => true,
            ])
            ->add('status', ChoiceType::class, [
                'choices' => [
                    'Pending' => 'pending',
                    'Completed' => 'completed',
                    'Cancelled' => 'cancelled',
                ],
                'required' => true,
            ])
            ->add('product', EntityType::class, [
                'class' => Products::class,
                'choice_label' => 'product_name',
                'placeholder' => 'Choose a product',
                'required' => true,
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Orders::class,
        ]);
    }
}
