package edu.duke.online.tenki.model;

import lombok.Data;

@Data
public class DukeWallet {
    String publicAddress;
    String walletName;
    String password;
    String privateKey;
    String publicKey;
}
