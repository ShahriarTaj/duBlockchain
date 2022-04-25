package edu.duke.online.tenki;

import edu.duke.online.tenki.model.DukeWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;

import java.util.Map;


@RestController
@RequestMapping("api/wallet")
public class WalletController {

    @Autowired
    Web3j web3j;

    @Autowired
    Services services;


    @PostMapping("wallet")
    public DukeWallet  createWallet(@RequestParam("nickName") String nickName,
                                    @RequestParam("password") String password)  {

        return services.createWallet(nickName, password);
    }

    @PostMapping("info/{publicAddress}")
    public Map<String, Object> getInfo(@PathVariable("publicAddress") String publicAddress,
                                       @RequestBody String password){

        return services.getLocalAccountInfo(publicAddress, password);
    }

    @GetMapping("populateH2")
    public Map<String, Object> populateH2(@RequestParam("directory") String directory){
        return services.populateH2(directory);
    }


}
