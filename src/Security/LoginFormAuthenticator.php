<?php

namespace App\Security;

use App\Entity\LoginAttempt;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Symfony\Component\Mime\Address;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Symfony\Component\Security\Core\Security;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Http\Authenticator\AbstractLoginFormAuthenticator;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\CsrfTokenBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\RememberMeBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\UserBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\Credentials\PasswordCredentials;
use Symfony\Component\Security\Http\Authenticator\Passport\Passport;
use Symfony\Component\Security\Http\SecurityRequestAttributes;
use Symfony\Component\Security\Http\Util\TargetPathTrait;

class LoginFormAuthenticator extends AbstractLoginFormAuthenticator
{
    use TargetPathTrait;

    public const LOGIN_ROUTE = 'app_login';

    private $entityManager;
    private $mailer;

    public function __construct(private UrlGeneratorInterface $urlGenerator, EntityManagerInterface $entityManager, MailerInterface $mailer)
    {
        $this->entityManager = $entityManager;
        $this->mailer = $mailer;
    }

    public function authenticate(Request $request): Passport
    {
        $email = $request->get('email');
        $password = $request->get('password');

        $request->getSession()->set(SecurityRequestAttributes::LAST_USERNAME, $email);

        return new Passport(
            new UserBadge($email),
            new PasswordCredentials($password),
            [
                new CsrfTokenBadge('authenticate', $request->get('_csrf_token')),
                new RememberMeBadge(),
            ]
        );
    }

    public function onAuthenticationSuccess(Request $request, TokenInterface $token, string $firewallName): ?Response
    {
        $user = $token->getUser();
        $this->saveLoginAttempt($user, 'success', $request->getClientIp());

        // Send email after successful login
        $email = (new Email())
            ->from(new Address('azizchehata47@gmail.com', 'nadim'))
            ->to($user->getEmail())
            ->subject('Successful Login Notification')
            ->text('You have successfully logged in.');

        $this->mailer->send($email);

        if (in_array('ROLE_ADMIN', $user->getRoles())) {
            return new RedirectResponse($this->urlGenerator->generate('index'));
        } elseif (in_array('ROLE_PATIENT', $user->getRoles())) {
            return new RedirectResponse($this->urlGenerator->generate('index_front'));
        }

        // Default redirection if no role matches
        return new RedirectResponse($this->urlGenerator->generate('index'));
    }

    public function onAuthenticationFailure(Request $request, AuthenticationException $exception): Response
    {
        $email = $request->get('email');
        $user = $this->entityManager->getRepository(User::class)->findOneBy(['email' => $email]);
        $this->saveLoginAttempt($user, 'failure', $request->getClientIp());

        // Implement your logic for handling authentication failure
        return new Response('Authentication Failed', Response::HTTP_UNAUTHORIZED);
    }

    private function saveLoginAttempt(?UserInterface $user, string $status, ?string $ipAddress): void
    {
        if ($user) {
            $loginAttempt = new LoginAttempt();
            $loginAttempt->setUser($user);
            $loginAttempt->setDate(new \DateTime());
            $loginAttempt->setStatus($status);
            $loginAttempt->setIpAddress($ipAddress);

            $this->entityManager->persist($loginAttempt);
            $this->entityManager->flush();
        }
    }

    protected function getLoginUrl(Request $request): string
    {
        return $this->urlGenerator->generate(self::LOGIN_ROUTE);
    }
}
