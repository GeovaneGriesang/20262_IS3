-- Aula 09: foto de perfil do usuário.
-- O banco guarda só o nome do arquivo (ex.: "7.png"); a foto em si fica
-- na pasta fotos_usuarios/, fora do controle de versão, com o nome do
-- arquivo sendo o id do usuário mais a extensão original (ver
-- FotoUsuarioUtil). NULL significa "usuário ainda sem foto".
ALTER TABLE usuarios ADD COLUMN foto VARCHAR(150) NULL;
