<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250302224644 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE consultation DROP FOREIGN KEY FK_964685A616E8FB6D');
        $this->addSql('ALTER TABLE consultation DROP FOREIGN KEY FK_964685A66B899279');
        $this->addSql('ALTER TABLE consultation ADD CONSTRAINT FK_964685A616E8FB6D FOREIGN KEY (psychiatrist_id) REFERENCES `user` (id)');
        $this->addSql('ALTER TABLE consultation ADD CONSTRAINT FK_964685A66B899279 FOREIGN KEY (patient_id) REFERENCES `user` (id)');
        $this->addSql('ALTER TABLE edit_history DROP FOREIGN KEY FK_9D44CBF1A76ED395');
        $this->addSql('ALTER TABLE edit_history ADD CONSTRAINT FK_9D44CBF1A76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id)');
        $this->addSql('ALTER TABLE like_dislike DROP FOREIGN KEY FK_ADB6A6897294869C');
        $this->addSql('ALTER TABLE like_dislike ADD user_id INT NOT NULL, DROP user_identifier');
        $this->addSql('ALTER TABLE like_dislike ADD CONSTRAINT FK_ADB6A689A76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id)');
        $this->addSql('ALTER TABLE like_dislike ADD CONSTRAINT FK_ADB6A6897294869C FOREIGN KEY (article_id) REFERENCES article (id)');
        $this->addSql('CREATE INDEX IDX_ADB6A689A76ED395 ON like_dislike (user_id)');
        $this->addSql('ALTER TABLE response DROP FOREIGN KEY FK_3E7B0BFBA76ED395');
        $this->addSql('ALTER TABLE response ADD CONSTRAINT FK_3E7B0BFBA76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE consultation DROP FOREIGN KEY FK_964685A66B899279');
        $this->addSql('ALTER TABLE consultation DROP FOREIGN KEY FK_964685A616E8FB6D');
        $this->addSql('ALTER TABLE consultation ADD CONSTRAINT FK_964685A66B899279 FOREIGN KEY (patient_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE CASCADE');
        $this->addSql('ALTER TABLE consultation ADD CONSTRAINT FK_964685A616E8FB6D FOREIGN KEY (psychiatrist_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE CASCADE');
        $this->addSql('ALTER TABLE response DROP FOREIGN KEY FK_3E7B0BFBA76ED395');
        $this->addSql('ALTER TABLE response ADD CONSTRAINT FK_3E7B0BFBA76ED395 FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE CASCADE');
        $this->addSql('ALTER TABLE like_dislike DROP FOREIGN KEY FK_ADB6A689A76ED395');
        $this->addSql('ALTER TABLE like_dislike DROP FOREIGN KEY FK_ADB6A6897294869C');
        $this->addSql('DROP INDEX IDX_ADB6A689A76ED395 ON like_dislike');
        $this->addSql('ALTER TABLE like_dislike ADD user_identifier VARCHAR(255) NOT NULL, DROP user_id');
        $this->addSql('ALTER TABLE like_dislike ADD CONSTRAINT FK_ADB6A6897294869C FOREIGN KEY (article_id) REFERENCES article (id) ON UPDATE NO ACTION ON DELETE CASCADE');
        $this->addSql('ALTER TABLE edit_history DROP FOREIGN KEY FK_9D44CBF1A76ED395');
        $this->addSql('ALTER TABLE edit_history ADD CONSTRAINT FK_9D44CBF1A76ED395 FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE CASCADE');
    }
}
