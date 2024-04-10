<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250224103540 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE article (id INT AUTO_INCREMENT NOT NULL, title VARCHAR(255) NOT NULL, content LONGTEXT NOT NULL, media VARCHAR(255) DEFAULT NULL, type VARCHAR(50) NOT NULL, created_at DATETIME NOT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE article_tags (article_id INT NOT NULL, tag_id INT NOT NULL, INDEX IDX_DFFE13277294869C (article_id), INDEX IDX_DFFE1327BAD26311 (tag_id), PRIMARY KEY(article_id, tag_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE comment (id INT AUTO_INCREMENT NOT NULL, article_id INT NOT NULL, content LONGTEXT NOT NULL, created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', INDEX IDX_9474526C7294869C (article_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE consultation (id INT AUTO_INCREMENT NOT NULL, patient_id INT NOT NULL, psychiatrist_id INT NOT NULL, created_at DATETIME NOT NULL, consultation_date DATETIME DEFAULT NULL, message LONGTEXT DEFAULT NULL, statut VARCHAR(20) NOT NULL, INDEX IDX_964685A66B899279 (patient_id), INDEX IDX_964685A616E8FB6D (psychiatrist_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE coupons (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(255) NOT NULL, discount_percentage INT NOT NULL, code VARCHAR(50) NOT NULL, is_active TINYINT(1) DEFAULT 1 NOT NULL, expiration_date DATETIME DEFAULT NULL, usage_count INT DEFAULT 0 NOT NULL, UNIQUE INDEX UNIQ_F564111877153098 (code), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE objectives (id INT AUTO_INCREMENT NOT NULL, coupon_id INT NOT NULL, user_id INT NOT NULL, nbr_consultation INT NOT NULL, status VARCHAR(10) DEFAULT \'pending\' NOT NULL, INDEX IDX_6CB0696C66C5951B (coupon_id), INDEX IDX_6CB0696CA76ED395 (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE reclamation (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, title VARCHAR(255) NOT NULL, description LONGTEXT NOT NULL, created_at DATETIME NOT NULL, INDEX IDX_CE606404A76ED395 (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE response (id INT AUTO_INCREMENT NOT NULL, reclamation_id INT NOT NULL, user_id INT NOT NULL, content LONGTEXT NOT NULL, created_at DATETIME NOT NULL, INDEX IDX_3E7B0BFB2D6BA2D9 (reclamation_id), INDEX IDX_3E7B0BFBA76ED395 (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE tag (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(50) NOT NULL, UNIQUE INDEX UNIQ_389B7835E237E06 (name), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE `user` (id INT AUTO_INCREMENT NOT NULL, email VARCHAR(180) NOT NULL, roles JSON NOT NULL, password VARCHAR(255) NOT NULL, UNIQUE INDEX UNIQ_IDENTIFIER_EMAIL (email), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', available_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', delivered_at DATETIME DEFAULT NULL COMMENT \'(DC2Type:datetime_immutable)\', INDEX IDX_75EA56E0FB7336F0 (queue_name), INDEX IDX_75EA56E0E3BD61CE (available_at), INDEX IDX_75EA56E016BA31DB (delivered_at), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE article_tags ADD CONSTRAINT FK_DFFE13277294869C FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE article_tags ADD CONSTRAINT FK_DFFE1327BAD26311 FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE comment ADD CONSTRAINT FK_9474526C7294869C FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE consultation ADD CONSTRAINT FK_964685A66B899279 FOREIGN KEY (patient_id) REFERENCES `user` (id)');
        $this->addSql('ALTER TABLE consultation ADD CONSTRAINT FK_964685A616E8FB6D FOREIGN KEY (psychiatrist_id) REFERENCES `user` (id)');
        $this->addSql('ALTER TABLE objectives ADD CONSTRAINT FK_6CB0696C66C5951B FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE objectives ADD CONSTRAINT FK_6CB0696CA76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE reclamation ADD CONSTRAINT FK_CE606404A76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id)');
        $this->addSql('ALTER TABLE response ADD CONSTRAINT FK_3E7B0BFB2D6BA2D9 FOREIGN KEY (reclamation_id) REFERENCES reclamation (id)');
        $this->addSql('ALTER TABLE response ADD CONSTRAINT FK_3E7B0BFBA76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE article_tags DROP FOREIGN KEY FK_DFFE13277294869C');
        $this->addSql('ALTER TABLE article_tags DROP FOREIGN KEY FK_DFFE1327BAD26311');
        $this->addSql('ALTER TABLE comment DROP FOREIGN KEY FK_9474526C7294869C');
        $this->addSql('ALTER TABLE consultation DROP FOREIGN KEY FK_964685A66B899279');
        $this->addSql('ALTER TABLE consultation DROP FOREIGN KEY FK_964685A616E8FB6D');
        $this->addSql('ALTER TABLE objectives DROP FOREIGN KEY FK_6CB0696C66C5951B');
        $this->addSql('ALTER TABLE objectives DROP FOREIGN KEY FK_6CB0696CA76ED395');
        $this->addSql('ALTER TABLE reclamation DROP FOREIGN KEY FK_CE606404A76ED395');
        $this->addSql('ALTER TABLE response DROP FOREIGN KEY FK_3E7B0BFB2D6BA2D9');
        $this->addSql('ALTER TABLE response DROP FOREIGN KEY FK_3E7B0BFBA76ED395');
        $this->addSql('DROP TABLE article');
        $this->addSql('DROP TABLE article_tags');
        $this->addSql('DROP TABLE comment');
        $this->addSql('DROP TABLE consultation');
        $this->addSql('DROP TABLE coupons');
        $this->addSql('DROP TABLE objectives');
        $this->addSql('DROP TABLE reclamation');
        $this->addSql('DROP TABLE response');
        $this->addSql('DROP TABLE tag');
        $this->addSql('DROP TABLE `user`');
        $this->addSql('DROP TABLE messenger_messages');
    }
}
