CREATE TABLE SIAPORGAO (
    C_UA                          CHAR(4),
    CODIGO                        VARCHAR(10) NOT NULL,
    CNPJ                          VARCHAR(14) NOT NULL,
    NOME                          VARCHAR(255) NOT NULL,
    SIGLA                         VARCHAR(32),
    DATACRIACAO                   DATE,
    DATAATOCRIACAO                DATE,
    ATOCRIACAO                    VARCHAR(32),
    VEICULOPUBLICACAOATOCRIACAO   VARCHAR(255),
    DATAEXTINCAO                  DATE,
    DATAATOEXTINCAO               DATE,
    ATOEXTINCAO                   VARCHAR(32),
    VEICULOPUBLICACAOATOEXTINCAO  VARCHAR(255),
    CODIGOORGAOPAI                VARCHAR(255),
    CARDUG                        CHAR(6),
    IDSIAFIC                      INTEGER
);