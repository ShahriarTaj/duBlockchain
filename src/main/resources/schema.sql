CREATE SCHEMA IF NOT EXISTS walletdb;
SET SCHEMA walletdb;
CREATE TABLE IF NOT EXISTS WALLET
    (ID VARCHAR(255) PRIMARY KEY,
    NAME VARCHAR(255),
    PASSWORD VARCHAR(255),
    FILEPATH VARCHAR (500));
CREATE TABLE IF NOT EXISTS EXCHANGE_LISTING
    (ContractId VARCHAR(255) PRIMARY KEY,
    StartDate DATE,
    EndDate DATE,
    Region VARCHAR(255),
    Premium NUMERIC ( 36 ),
    Indemnity NUMERIC ( 36 ),
    MinimumIndemnity NUMERIC ( 36 ),
    UpperTemperature NUMERIC ( 36 ),
    AverageTemperature NUMERIC ( 36 ),
    LowerTemperature NUMERIC ( 36 ),
    Status NUMERIC ( 1 ),
    investorAddress VARCHAR (255 )
    );